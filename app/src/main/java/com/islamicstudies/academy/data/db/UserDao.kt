package com.islamicstudies.academy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class SavedItem(
    val headingId: String,
    val titleUr: String,
    val titleEn: String,
    val chapterTitleUr: String,
    val chapterTitleEn: String
)

data class NoteItem(
    val headingId: String,
    val titleUr: String,
    val titleEn: String,
    val chapterTitleUr: String,
    val chapterTitleEn: String,
    val noteText: String
)

data class ChapterProgress(
    val chapterId: String,
    val number: Int,
    val titleUr: String,
    val titleEn: String,
    val totalTopics: Int,
    val completedTopics: Int
)

@Dao
interface UserDao {

    @Query(
        "SELECT c.id AS chapterId, c.number AS number, c.titleUr AS titleUr, c.titleEn AS titleEn, " +
            "(SELECT COUNT(*) FROM headings h WHERE h.chapterId = c.id AND h.type = 'topic') AS totalTopics, " +
            "(SELECT COUNT(*) FROM headings h2 JOIN user_progress p ON p.headingId = h2.id " +
            "WHERE h2.chapterId = c.id AND h2.type = 'topic' AND p.completed = 1) AS completedTopics " +
            "FROM chapters c ORDER BY c.sortIndex"
    )
    fun observeChapterProgress(): Flow<List<ChapterProgress>>

    // ---- backup / restore / reset ----
    @Query("SELECT * FROM user_progress")
    suspend fun getAllProgress(): List<UserProgressEntity>

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Query("SELECT * FROM personal_notes")
    suspend fun getAllNotes(): List<PersonalNoteEntity>

    @Query("DELETE FROM user_progress")
    suspend fun clearProgress()

    @Query("DELETE FROM bookmarks")
    suspend fun clearBookmarks()

    @Query("DELETE FROM personal_notes")
    suspend fun clearNotes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressItems(items: List<UserProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarkItems(items: List<BookmarkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteItems(items: List<PersonalNoteEntity>)

    // ---- progress overview ----
    @Query(
        "SELECT COUNT(*) FROM user_progress p JOIN headings h ON h.id = p.headingId " +
            "WHERE p.completed = 1 AND h.type = 'topic'"
    )
    fun observeCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM headings WHERE type = 'topic'")
    fun observeTopicCount(): Flow<Int>

    @Query("SELECT headingId FROM bookmarks")
    fun observeBookmarkIds(): Flow<List<String>>

    @Query("SELECT headingId FROM user_progress WHERE completed = 1")
    fun observeCompletedIds(): Flow<List<String>>

    // ---- single lesson state ----
    @Query("SELECT COUNT(*) FROM bookmarks WHERE headingId = :id")
    fun observeBookmarkCount(id: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_progress WHERE headingId = :id AND completed = 1")
    fun observeCompletedFlag(id: String): Flow<Int>

    @Query("SELECT text FROM personal_notes WHERE headingId = :id")
    fun observeNote(id: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(item: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE headingId = :id")
    suspend fun removeBookmark(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(item: PersonalNoteEntity)

    @Query("DELETE FROM personal_notes WHERE headingId = :id")
    suspend fun deleteNote(id: String)

    @Query("SELECT * FROM user_progress WHERE headingId = :id")
    suspend fun getProgress(id: String): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(item: UserProgressEntity)

    // ---- lists ----
    @Query(
        "SELECT h.id AS headingId, h.titleUr AS titleUr, h.titleEn AS titleEn, " +
            "c.titleUr AS chapterTitleUr, c.titleEn AS chapterTitleEn " +
            "FROM bookmarks b JOIN headings h ON h.id = b.headingId " +
            "JOIN chapters c ON c.id = h.chapterId ORDER BY b.createdAt DESC"
    )
    fun observeBookmarks(): Flow<List<SavedItem>>

    @Query(
        "SELECT h.id AS headingId, h.titleUr AS titleUr, h.titleEn AS titleEn, " +
            "c.titleUr AS chapterTitleUr, c.titleEn AS chapterTitleEn, n.text AS noteText " +
            "FROM personal_notes n JOIN headings h ON h.id = n.headingId " +
            "JOIN chapters c ON c.id = h.chapterId ORDER BY n.updatedAt DESC"
    )
    fun observeNotes(): Flow<List<NoteItem>>

    @Query(
        "SELECT h.id AS headingId, h.titleUr AS titleUr, h.titleEn AS titleEn, " +
            "c.titleUr AS chapterTitleUr, c.titleEn AS chapterTitleEn " +
            "FROM user_progress p JOIN headings h ON h.id = p.headingId " +
            "JOIN chapters c ON c.id = h.chapterId ORDER BY p.lastOpenedAt DESC LIMIT 1"
    )
    fun observeLastOpened(): Flow<SavedItem?>
}
