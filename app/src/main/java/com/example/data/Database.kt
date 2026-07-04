package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton profile
    val name: String = "Student",
    val board: String = "CBSE",
    val selectedClass: Int = 10, // Default to class 10
    val totalSolved: Int = 0,
    val correctSolved: Int = 0,
    val dailyStreak: Int = 0,
    val lastActiveTimestamp: Long = 0L
)

@Entity(tableName = "saved_questions")
data class SavedQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val board: String,
    val className: Int,
    val subject: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String = "",
    val bookmarkedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val board: String,
    val className: Int,
    val subject: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Float,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET totalSolved = totalSolved + :count, correctSolved = correctSolved + :correct WHERE id = 1")
    suspend fun incrementSolvedStats(count: Int, correct: Int)

    @Query("UPDATE user_profile SET dailyStreak = :streak, lastActiveTimestamp = :timestamp WHERE id = 1")
    suspend fun updateStreak(streak: Int, timestamp: Long)
}

@Dao
interface PracticeDao {
    // Bookmarks
    @Query("SELECT * FROM saved_questions ORDER BY bookmarkedAt DESC")
    fun getAllSavedQuestions(): Flow<List<SavedQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuestion(question: SavedQuestion)

    @Query("DELETE FROM saved_questions WHERE questionText = :text")
    suspend fun unsaveQuestion(text: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_questions WHERE questionText = :text)")
    fun isQuestionSaved(text: String): Flow<Boolean>

    // Practice Sessions History
    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<PracticeSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSession)
}

// --- App Database ---

@Database(
    entities = [UserProfile::class, SavedQuestion::class, PracticeSession::class],
    version = 1,
    exportSchema = false
)
abstract class ExamDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun practiceDao(): PracticeDao
}
