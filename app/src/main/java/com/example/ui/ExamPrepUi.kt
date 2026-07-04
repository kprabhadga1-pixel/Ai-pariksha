package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.data.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.Question
import com.example.utils.VoiceHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamPrepMainUi(viewModel: ExamPrepViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ViewModel States
    val userProfile by viewModel.userProfile.collectAsState()
    val currentQuestions by viewModel.currentQuestions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsState()
    val isAnswerSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val isBookmarked by viewModel.isCurrentQuestionSaved.collectAsState()
    val chatLog by viewModel.chatLog.collectAsState()
    val aiSpeechState by viewModel.aiSpeechState.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val practiceHistory by viewModel.practiceHistory.collectAsState()

    // Navigation State for Tabs
    var activeTab by remember { mutableStateOf("practice") } // "practice", "tutor", "analytics", "profile"

    // Speech-to-Text runtime permission launcher
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Microphone Access Granted! Speak now.", Toast.LENGTH_SHORT).show()
            viewModel.startListeningToStudent()
        } else {
            Toast.makeText(context, "Microphone permission is required to ask doubt by voice.", Toast.LENGTH_LONG).show()
        }
    }

    // Main Scaffold Layout
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Abhyas AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${userProfile.board} • Class ${userProfile.selectedClass}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Daily Streak Badge
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userProfile.dailyStreak} Days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab == "practice",
                    onClick = { activeTab = "practice" },
                    icon = { Icon(Icons.Filled.Book, contentDescription = "Practice") },
                    label = { Text("Practice") },
                    modifier = Modifier.testTag("nav_practice")
                )
                NavigationBarItem(
                    selected = activeTab == "tutor",
                    onClick = { activeTab = "tutor" },
                    icon = {
                        BadgedBox(badge = {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(8.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }) {
                            Icon(Icons.Filled.SupportAgent, contentDescription = "AI Guru Ji")
                        }
                    },
                    label = { Text("Guru Ji") },
                    modifier = Modifier.testTag("nav_tutor")
                )
                NavigationBarItem(
                    selected = activeTab == "analytics",
                    onClick = { activeTab = "analytics" },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "History") },
                    label = { Text("Stats") },
                    modifier = Modifier.testTag("nav_analytics")
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { activeTab = "profile" },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                "practice" -> PracticeTab(
                    viewModel = viewModel,
                    currentQuestions = currentQuestions,
                    currentQuestionIndex = currentQuestionIndex,
                    selectedOptionIndex = selectedOptionIndex,
                    isAnswerSubmitted = isAnswerSubmitted,
                    isBookmarked = isBookmarked,
                    aiSpeechState = aiSpeechState,
                    isAiLoading = isAiLoading,
                    onNavigateToTutor = { activeTab = "tutor" }
                )
                "tutor" -> TutorTab(
                    viewModel = viewModel,
                    chatLog = chatLog,
                    aiSpeechState = aiSpeechState,
                    isAiLoading = isAiLoading,
                    hasMicPermission = hasMicPermission,
                    onRequestMicPermission = {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
                "analytics" -> AnalyticsTab(
                    userProfile = userProfile,
                    practiceHistory = practiceHistory
                )
                "profile" -> SettingsTab(
                    viewModel = viewModel,
                    userProfile = userProfile
                )
            }
        }
    }
}

// --- TAB 1: PRACTICE ARENA ---

@Composable
fun PracticeTab(
    viewModel: ExamPrepViewModel,
    currentQuestions: List<Question>,
    currentQuestionIndex: Int,
    selectedOptionIndex: Int?,
    isAnswerSubmitted: Boolean,
    isBookmarked: Boolean,
    aiSpeechState: VoiceHelper.SpeechState,
    isAiLoading: Boolean,
    onNavigateToTutor: () -> Unit
) {
    if (currentQuestions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.School,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No practice questions found.", fontWeight = FontWeight.SemiBold)
                Text("Try changing subject or class standard in Settings.", fontSize = 12.sp, color = Color.Gray)
            }
        }
        return
    }

    val question = currentQuestions[currentQuestionIndex]
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card Header with AI Image
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.exam_prep_hero),
                        contentDescription = "Indian Board Exam Preparation",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Semi-transparent overlay for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${question.subject} Board Practice",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Question ${currentQuestionIndex + 1} of ${currentQuestions.size}",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Question display block
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(question.subject, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        )
                        
                        Row {
                            // Bookmark Button
                            IconButton(
                                onClick = { viewModel.toggleBookmark() },
                                modifier = Modifier.testTag("bookmark_button")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark Question",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }

                            // Read Aloud Question Button
                            IconButton(
                                onClick = { viewModel.toggleGuruVoice(question.questionText) },
                                modifier = Modifier.testTag("read_question_button")
                            ) {
                                Icon(
                                    imageVector = if (aiSpeechState == VoiceHelper.SpeechState.SPEAKING) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                                    contentDescription = "Speak Question",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = question.questionText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Multiple choice options
        items(question.options.size) { index ->
            val optionLetter = when (index) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                else -> "D"
            }
            val optionText = question.options[index]

            // Color scheme depending on state
            val isSelected = selectedOptionIndex == index
            val isCorrectIndex = question.correctAnswerIndex == index

            val backgroundColor = when {
                isAnswerSubmitted && isCorrectIndex -> Color(0xFFE8F5E9) // soft green for correct
                isAnswerSubmitted && isSelected && !isCorrectIndex -> Color(0xFFFFEBEE) // soft red for wrong selection
                isSelected -> MaterialTheme.colorScheme.primaryContainer // highlighted
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                isAnswerSubmitted && isCorrectIndex -> Color(0xFF4CAF50)
                isAnswerSubmitted && isSelected && !isCorrectIndex -> Color(0xFFF44336)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }

            val icon = when {
                isAnswerSubmitted && isCorrectIndex -> Icons.Filled.CheckCircle
                isAnswerSubmitted && isSelected && !isCorrectIndex -> Icons.Filled.Cancel
                isSelected -> Icons.Filled.RadioButtonChecked
                else -> Icons.Filled.RadioButtonUnchecked
            }

            val iconColor = when {
                isAnswerSubmitted && isCorrectIndex -> Color(0xFF4CAF50)
                isAnswerSubmitted && isSelected && !isCorrectIndex -> Color(0xFFF44336)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> Color.Gray
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .background(backgroundColor, RoundedCornerShape(12.dp))
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !isAnswerSubmitted) {
                        viewModel.selectOption(index)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("option_$optionLetter"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Option $optionLetter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Text(
                        text = optionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action panel (Submit / Next)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isAnswerSubmitted) {
                    Button(
                        onClick = { viewModel.submitAnswer() },
                        enabled = selectedOptionIndex != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Quick Explain Button
                        Button(
                            onClick = {
                                viewModel.askGuruJiExplanation()
                                onNavigateToTutor()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("explain_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Filled.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask Guru Ji", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Next Button
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("next_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next Question", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Fallback explanation card shown immediately after submit for study completeness
        if (isAnswerSubmitted) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Basic Offline Explanation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = question.offlineExplanation,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 2: AI VOICE TUTOR (GURU JI) ---

@Composable
fun TutorTab(
    viewModel: ExamPrepViewModel,
    chatLog: List<ChatMessage>,
    aiSpeechState: VoiceHelper.SpeechState,
    isAiLoading: Boolean,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll chat log to bottom when messages update
    LaunchedEffect(chatLog.size) {
        if (chatLog.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatLog.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Guru Ji Top Console Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Guru Ji circular Avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.primary
                                )
                            ),
                            CircleShape
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Face,
                        contentDescription = "Guru Ji",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Guru Ji (AI Voice Assistant)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = when (aiSpeechState) {
                            VoiceHelper.SpeechState.SPEAKING -> "Speaking to you... 🔊"
                            VoiceHelper.SpeechState.LISTENING -> "Guru Ji is listening carefully... 🎙️"
                            VoiceHelper.SpeechState.PROCESSING -> "Guru Ji is thinking... 🧠"
                            else -> "Ask your doubt by tapping the Mic!"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Waveform animation when speaking or listening
                if (aiSpeechState == VoiceHelper.SpeechState.SPEAKING || aiSpeechState == VoiceHelper.SpeechState.LISTENING) {
                    SoundWaveformVisualizer()
                }
            }
        }

        // Chat messages scroll column
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatLog) { msg ->
                val bubbleAlign = if (msg.isUser) Alignment.End else Alignment.Start
                val bubbleColor = if (msg.isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                val textColor = if (msg.isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = bubbleAlign
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                            .background(
                                bubbleColor,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                    bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (msg.isUser) "You" else "Guru Ji",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                color = textColor
                            )
                            
                            // Audio play button for Guru's message
                            if (!msg.isUser) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { viewModel.toggleGuruVoice(msg.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (aiSpeechState == VoiceHelper.SpeechState.SPEAKING) Icons.Filled.StopCircle else Icons.Filled.PlayCircle,
                                            contentDescription = "Speak/Stop",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Guru Ji is compiling the explanation...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Bottom Voice & Text input pad
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var textInputState by remember { mutableStateOf("") }

                // Text Field input
                TextField(
                    value = textInputState,
                    onValueChange = { textInputState = it },
                    placeholder = { Text("Ask Guru Ji your doubt...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .testTag("chat_text_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 2
                )

                if (textInputState.isNotBlank()) {
                    // Send text button
                    FloatingActionButton(
                        onClick = {
                            viewModel.askDynamicGuruJi(textInputState)
                            textInputState = ""
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("send_text_button"),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    }
                } else {
                    // Microphone Hold button
                    val isListening = aiSpeechState == VoiceHelper.SpeechState.LISTENING
                    
                    FloatingActionButton(
                        onClick = {
                            if (isListening) {
                                viewModel.stopGuruVoice()
                            } else {
                                if (hasMicPermission) {
                                    viewModel.startListeningToStudent()
                                } else {
                                    onRequestMicPermission()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("mic_voice_button"),
                        shape = CircleShape,
                        containerColor = if (isListening) Color.Red else MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = "Hold and Speak",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// SoundWave visualizer representation
@Composable
fun SoundWaveformVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val heights = List(5) { index ->
        infiniteTransition.animateFloat(
            initialValue = 4f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 300 + index * 100, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = Modifier.height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { animVal ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animVal.value.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

// --- TAB 3: STATS & ANALYTICS ---

@Composable
fun AnalyticsTab(
    userProfile: UserProfile,
    practiceHistory: List<com.example.data.PracticeSession>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Progress Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Track your board preparation metrics here", fontSize = 12.sp, color = Color.Gray)
        }

        // Stats card grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Practiced", fontSize = 12.sp, color = Color.Gray)
                        Text("${userProfile.totalSolved}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("practice questions", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Accuracy Rate", fontSize = 12.sp, color = Color.Gray)
                        val rate = if (userProfile.totalSolved > 0) {
                            (userProfile.correctSolved.toFloat() / userProfile.totalSolved * 100).toInt()
                        } else 0
                        Text("$rate%", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${userProfile.correctSolved} correct solves", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        // Board preparedness meter
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Class Standard Preparation Level", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val accuracy = if (userProfile.totalSolved > 0) {
                        userProfile.correctSolved.toFloat() / userProfile.totalSolved
                    } else 0f
                    
                    LinearProgressIndicator(
                        progress = { accuracy },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Novice", fontSize = 11.sp, color = Color.Gray)
                        Text("Ready for Exam!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Recent session list
        item {
            Text("Practice Milestones", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (practiceHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No historic practice records. Start your first session!", fontSize = 13.sp, color = Color.Gray)
                }
            }
        } else {
            items(practiceHistory) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(session.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Class ${session.className} • ${session.board}", fontSize = 11.sp, color = Color.Gray)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${session.correctAnswers}/${session.totalQuestions}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 4: SETTINGS & STANDARD CUSTOMIZATION ---

@Composable
fun SettingsTab(viewModel: ExamPrepViewModel, userProfile: UserProfile) {
    var nameInput by remember { mutableStateOf(userProfile.name) }
    var selectedBoard by remember { mutableStateOf(userProfile.board) }
    var selectedClass by remember { mutableStateOf(userProfile.selectedClass) }

    val boards = viewModel.getBoards()
    val standards = (5..12).toList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Student Profile Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Customize your education board and class for accurate preparation", fontSize = 12.sp, color = Color.Gray)
        }

        // Student name card
        item {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Your Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_setting_input"),
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true
            )
        }

        // School Board Selector
        item {
            Column {
                Text("Select Education Board", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    boards.take(3).forEach { board ->
                        val isSelected = selectedBoard == board
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBoard = board },
                            label = { Text(board, fontSize = 12.sp) },
                            modifier = Modifier.testTag("board_$board")
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    boards.drop(3).forEach { board ->
                        val isSelected = selectedBoard == board
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBoard = board },
                            label = { Text(board, fontSize = 12.sp) },
                            modifier = Modifier.testTag("board_$board")
                        )
                    }
                }
            }
        }

        // Class selector standard chips
        item {
            Column {
                Text("Select Class / Standard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                // Grid of 5th to 12th standard
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val rows = standards.chunked(4)
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { standard ->
                                val isSelected = selectedClass == standard
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedClass = standard },
                                    label = { Text("Class $standard", fontSize = 11.sp) },
                                    modifier = Modifier.testTag("class_$standard")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Available subjects preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Subjects in standard:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    val subjects = viewModel.getAvailableSubjectsForClass(selectedClass)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { sub ->
                            SuggestionChip(
                                onClick = { viewModel.selectSubject(sub) },
                                label = { Text(sub, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    viewModel.updateProfile(nameInput, selectedBoard, selectedClass)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save and Apply Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}
