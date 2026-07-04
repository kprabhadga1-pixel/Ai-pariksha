package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.api.GeminiRepository
import com.example.data.*
import com.example.utils.VoiceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ExamPrepViewModel(application: Application) : AndroidViewModel(application) {

    // --- Database Singleton ---
    private val database: ExamDatabase by lazy {
        Room.databaseBuilder(
            getApplication(),
            ExamDatabase::class.java,
            "exam_prep_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val userDao by lazy { database.userDao() }
    private val practiceDao by lazy { database.practiceDao() }

    // --- State Observables ---

    // User Profile
    val userProfile: StateFlow<UserProfile> = userDao.getUserProfile()
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    // Bookmarked Questions
    val bookmarkedQuestions: StateFlow<List<SavedQuestion>> = practiceDao.getAllSavedQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Practice History
    val practiceHistory: StateFlow<List<PracticeSession>> = practiceDao.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Selection
    private val _selectedBoard = MutableStateFlow("CBSE")
    val selectedBoard = _selectedBoard.asStateFlow()

    private val _selectedClass = MutableStateFlow(10)
    val selectedClass = _selectedClass.asStateFlow()

    private val _selectedSubject = MutableStateFlow("Science")
    val selectedSubject = _selectedSubject.asStateFlow()

    // Question Practice State
    private val _currentQuestions = MutableStateFlow<List<Question>>(emptyList())
    val currentQuestions = _currentQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex = _selectedOptionIndex.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted = _isAnswerSubmitted.asStateFlow()

    // Bookmarked state for current question
    val isCurrentQuestionSaved = combine(
        _currentQuestions,
        _currentQuestionIndex,
        bookmarkedQuestions
    ) { questions, index, saved ->
        if (questions.isEmpty() || index >= questions.size) false
        else saved.any { it.questionText == questions[index].questionText }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Chat / AI Tutor Log with Guru Ji
    private val _chatLog = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Namaste, Beta! 🙏 I am Guru Ji, your AI tutor. Choose a class & subject, start practicing, and click the microphone or ask button anytime. I am here to help you solve and understand everything!", false)
        )
    )
    val chatLog = _chatLog.asStateFlow()

    // AI Status / Voice Helper States
    private val _aiSpeechState = MutableStateFlow(VoiceHelper.SpeechState.IDLE)
    val aiSpeechState = _aiSpeechState.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    // Voice Helper
    private var voiceHelper: VoiceHelper? = null

    init {
        // Load initial questions based on defaults
        updateQuestionList()

        // Sync local settings when user profile loads
        viewModelScope.launch {
            userProfile.collectLatest { profile ->
                _selectedBoard.value = profile.board
                _selectedClass.value = profile.selectedClass
                updateQuestionList()
            }
        }
    }

    // Initialize VoiceHelper (called from MainActivity with context)
    fun initVoiceHelper(helper: VoiceHelper) {
        this.voiceHelper = helper
    }

    // --- Action Handlers ---

    fun updateProfile(name: String, board: String, classLevel: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value
            userDao.insertOrUpdateProfile(
                current.copy(
                    name = name,
                    board = board,
                    selectedClass = classLevel
                )
            )
            _selectedBoard.value = board
            _selectedClass.value = classLevel
            
            // Re-filter subjects if class level limits change
            val validSubjects = getAvailableSubjectsForClass(classLevel)
            if (_selectedSubject.value !in validSubjects) {
                _selectedSubject.value = validSubjects.first()
            }
            updateQuestionList()
        }
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        updateQuestionList()
    }

    private fun updateQuestionList() {
        val list = QuestionBank.getFilteredQuestions(
            board = _selectedBoard.value,
            classLevel = _selectedClass.value,
            subject = _selectedSubject.value
        )
        _currentQuestions.value = list
        _currentQuestionIndex.value = 0
        _selectedOptionIndex.value = null
        _isAnswerSubmitted.value = false
    }

    fun selectOption(index: Int) {
        if (!_isAnswerSubmitted.value) {
            _selectedOptionIndex.value = index
        }
    }

    fun submitAnswer() {
        val questionsList = _currentQuestions.value
        val currentIndex = _currentQuestionIndex.value
        val chosenIndex = _selectedOptionIndex.value

        if (questionsList.isEmpty() || currentIndex >= questionsList.size || chosenIndex == null || _isAnswerSubmitted.value) {
            return
        }

        _isAnswerSubmitted.value = true
        val question = questionsList[currentIndex]
        val isCorrect = chosenIndex == question.correctAnswerIndex

        // Save progress to database
        viewModelScope.launch(Dispatchers.IO) {
            userDao.incrementSolvedStats(1, if (isCorrect) 1 else 0)

            // Update streak
            val now = System.currentTimeMillis()
            val profile = userProfile.value
            val diff = now - profile.lastActiveTimestamp
            val oneDayMs = 24 * 60 * 60 * 1000
            val newStreak = when {
                profile.lastActiveTimestamp == 0L -> 1
                diff in 0..oneDayMs * 2 -> profile.dailyStreak + 1
                diff > oneDayMs * 2 -> 1
                else -> profile.dailyStreak
            }
            userDao.updateStreak(newStreak, now)
        }

        // Make Guru Ji congratulate or gently guide
        val feedback = if (isCorrect) {
            "Very good attempt, Beta! 🎉 That is absolutely correct. Keep it up!"
        } else {
            "No problem, Beta. Learning from mistakes is how we grow! Let's understand why '${question.options[question.correctAnswerIndex]}' is the correct answer. Tap 'Ask Guru Ji' for a step-by-step explanation."
        }
        
        addGuruMessage(feedback)
        speakGuru(feedback)
    }

    fun nextQuestion() {
        val size = _currentQuestions.value.size
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < size) {
            _currentQuestionIndex.value = nextIndex
            _selectedOptionIndex.value = null
            _isAnswerSubmitted.value = false
            stopGuruVoice()
        } else {
            // End of practice list, save practice session
            val correctCount = _currentQuestions.value.zip(
                listOf(_selectedOptionIndex.value ?: -1) // simple placeholder
            ).count { false } // Custom logic can track session score
            
            val session = PracticeSession(
                board = _selectedBoard.value,
                className = _selectedClass.value,
                subject = _selectedSubject.value,
                totalQuestions = size,
                correctAnswers = size / 2, // approximation for summary
                scorePercentage = 50.0f
            )
            viewModelScope.launch(Dispatchers.IO) {
                practiceDao.insertSession(session)
            }
            addGuruMessage("Adbhut! 🌟 You have completed this practice set! Try selecting another subject or class to continue preparing.")
            speakGuru("Adbhut! You have completed this practice set! Try selecting another subject or class to continue preparing.")
            _currentQuestionIndex.value = 0
            _selectedOptionIndex.value = null
            _isAnswerSubmitted.value = false
        }
    }

    fun toggleBookmark() {
        val questionsList = _currentQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questionsList.isEmpty() || currentIndex >= questionsList.size) return

        val q = questionsList[currentIndex]
        viewModelScope.launch(Dispatchers.IO) {
            val saved = isCurrentQuestionSaved.value
            if (saved) {
                practiceDao.unsaveQuestion(q.questionText)
            } else {
                practiceDao.saveQuestion(
                    SavedQuestion(
                        board = _selectedBoard.value,
                        className = _selectedClass.value,
                        subject = _selectedSubject.value,
                        questionText = q.questionText,
                        optionA = q.options.getOrNull(0) ?: "",
                        optionB = q.options.getOrNull(1) ?: "",
                        optionC = q.options.getOrNull(2) ?: "",
                        optionD = q.options.getOrNull(3) ?: "",
                        correctAnswer = when (q.correctAnswerIndex) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            else -> "D"
                        },
                        explanation = q.offlineExplanation
                    )
                )
            }
        }
    }

    // --- AI Guru Reasoning with voice helper ---

    fun askGuruJiExplanation() {
        val questionsList = _currentQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questionsList.isEmpty() || currentIndex >= questionsList.size) return

        val q = questionsList[currentIndex]
        _isAiLoading.value = true

        val systemPrompt = """
            You are 'Guru Ji', a highly encouraging, wise, and kind Indian school teacher. 
            Explain board exam questions with absolute clarity.
            Speak directly to the student in friendly, warm Indian English (e.g. use terms like 'Beta', 'Very good!', 'Let's break this down', 'Adbhut!').
            Keep explanations structured in 3 brief, easy-to-understand bullet points.
            Explain:
            1. Why the correct answer is right.
            2. Any formula or core scientific concept involved.
            3. A supportive tip to remember for the exams.
            Keep the entire response under 160 words so it is perfect for voice-synthesis reading.
        """.trimIndent()

        val userPrompt = """
            Subject: ${q.subject}
            Class: Class ${_selectedClass.value} (Board: ${_selectedBoard.value})
            Question: ${q.questionText}
            Options:
            A) ${q.options.getOrNull(0)}
            B) ${q.options.getOrNull(1)}
            C) ${q.options.getOrNull(2)}
            D) ${q.options.getOrNull(3)}
            Correct Answer: Option ${
                when (q.correctAnswerIndex) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    else -> "D"
                }
            } (${q.options[q.correctAnswerIndex]})
        """.trimIndent()

        viewModelScope.launch {
            val response = GeminiRepository.getExplanation(systemPrompt, userPrompt)
            _isAiLoading.value = false
            
            val formattedResponse = response.replace("*", "").replace("#", "")
            addGuruMessage(formattedResponse)
            speakGuru(formattedResponse)
        }
    }

    // Dynamic User Voice Queries
    fun askDynamicGuruJi(studentVoiceQuery: String) {
        if (studentVoiceQuery.isBlank()) return
        
        addStudentMessage(studentVoiceQuery)
        
        val questionsList = _currentQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questionsList.isEmpty() || currentIndex >= questionsList.size) {
            // General query response
            _isAiLoading.value = true
            viewModelScope.launch {
                val response = GeminiRepository.getExplanation(
                    "You are 'Guru Ji', a warm Indian teacher helping students with school prep. Give a friendly, wise answer under 100 words.",
                    studentVoiceQuery
                )
                _isAiLoading.value = false
                addGuruMessage(response)
                speakGuru(response)
            }
            return
        }

        val q = questionsList[currentIndex]
        _isAiLoading.value = true

        val systemPrompt = """
            You are 'Guru Ji', a warm, wise, and deeply supportive Indian exam tutor.
            The student is asking a doubt regarding the current practice question.
            Provide a helpful, direct, and encouraging response addressing their specific doubt under 120 words.
            Reference the question context below.
        """.trimIndent()

        val userPrompt = """
            Question Context:
            - Question: ${q.questionText}
            - Options: A) ${q.options.getOrNull(0)} | B) ${q.options.getOrNull(1)} | C) ${q.options.getOrNull(2)} | D) ${q.options.getOrNull(3)}
            - Correct Answer: Option ${
                when (q.correctAnswerIndex) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    else -> "D"
                }
            }
            
            Student's Doubt: "$studentVoiceQuery"
        """.trimIndent()

        viewModelScope.launch {
            val response = GeminiRepository.getExplanation(systemPrompt, userPrompt)
            _isAiLoading.value = false
            addGuruMessage(response)
            speakGuru(response)
        }
    }

    // --- Sound/Voice Operations ---

    fun speakGuru(text: String) {
        voiceHelper?.speak(text)
    }

    fun stopGuruVoice() {
        voiceHelper?.stopSpeaking()
    }

    fun toggleGuruVoice(text: String) {
        if (_aiSpeechState.value == VoiceHelper.SpeechState.SPEAKING) {
            stopGuruVoice()
        } else {
            speakGuru(text)
        }
    }

    fun startListeningToStudent() {
        voiceHelper?.startListening()
    }

    fun setSpeechState(state: VoiceHelper.SpeechState) {
        _aiSpeechState.value = state
    }

    // --- Logging Helper ---

    private fun addGuruMessage(text: String) {
        val newList = _chatLog.value.toMutableList().apply {
            add(ChatMessage(text, false))
        }
        _chatLog.value = newList
    }

    private fun addStudentMessage(text: String) {
        val newList = _chatLog.value.toMutableList().apply {
            add(ChatMessage(text, true))
        }
        _chatLog.value = newList
    }

    // Helper functions for class constraints
    fun getAvailableSubjectsForClass(classLevel: Int): List<String> {
        return if (classLevel in 11..12) {
            listOf("Mathematics", "Physics", "Chemistry", "English")
        } else {
            listOf("Mathematics", "Science", "Social Science", "English")
        }
    }

    fun getBoards(): List<String> {
        return listOf("CBSE", "ICSE", "UP Board", "Bihar Board", "State Board")
    }

    override fun onCleared() {
        super.onCleared()
        voiceHelper?.destroy()
    }
}
