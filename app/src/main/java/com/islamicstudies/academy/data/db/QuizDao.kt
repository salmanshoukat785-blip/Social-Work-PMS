package com.islamicstudies.academy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query(
        "SELECT * FROM questions WHERE (:chapterId = 'all' OR chapterId = :chapterId) " +
            "AND (:difficulty = 'all' OR difficulty = :difficulty)"
    )
    suspend fun getQuestions(chapterId: String, difficulty: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<String>): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    fun observeQuestionCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(items: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()

    @Insert
    suspend fun insertAttempt(item: QuizAttemptEntity): Long

    @Query("SELECT * FROM quiz_attempts ORDER BY startedAt DESC")
    fun observeAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    suspend fun getAttempt(id: Long): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts ORDER BY startedAt ASC")
    suspend fun getAttemptsChronological(): List<QuizAttemptEntity>

    @Query("DELETE FROM quiz_attempts")
    suspend fun clearAttempts()

    @Insert
    suspend fun insertAttemptItems(items: List<QuizAttemptEntity>)
}
