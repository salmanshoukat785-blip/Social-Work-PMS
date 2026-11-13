package com.islamicstudies.academy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ChapterSummary(
    val id: String,
    val number: Int,
    val titleUr: String,
    val titleEn: String,
    val headingCount: Int
)

@Dao
interface ContentDao {

    @Query(
        "SELECT c.id AS id, c.number AS number, c.titleUr AS titleUr, c.titleEn AS titleEn, " +
            "(SELECT COUNT(*) FROM headings h WHERE h.chapterId = c.id) AS headingCount " +
            "FROM chapters c ORDER BY c.sortIndex"
    )
    fun observeChapterSummaries(): Flow<List<ChapterSummary>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    fun observeChapter(id: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM headings WHERE chapterId = :chapterId ORDER BY sortIndex")
    fun observeHeadings(chapterId: String): Flow<List<HeadingEntity>>

    @Query("SELECT * FROM headings WHERE id = :id")
    fun observeHeading(id: String): Flow<HeadingEntity?>

    @Query("SELECT * FROM lesson_sections WHERE headingId = :headingId ORDER BY orderIdx")
    fun observeSections(headingId: String): Flow<List<LessonSectionEntity>>

    @Query(
        "SELECT h.id AS headingId, h.titleUr AS titleUr, h.titleEn AS titleEn, " +
            "c.titleUr AS chapterTitleUr, c.titleEn AS chapterTitleEn " +
            "FROM headings h JOIN chapters c ON c.id = h.chapterId " +
            "WHERE h.titleUr LIKE :pattern OR h.titleEn LIKE :pattern " +
            "OR h.id IN (SELECT headingId FROM lesson_sections WHERE body LIKE :pattern) " +
            "ORDER BY c.sortIndex, h.sortIndex LIMIT 100"
    )
    suspend fun search(pattern: String): List<SavedItem>

    @Insert
    suspend fun insertLessonSections(items: List<LessonSectionEntity>)

    @Query("DELETE FROM lesson_sections")
    suspend fun deleteAllLessonSections()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(items: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeadings(items: List<HeadingEntity>)

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()

    @Query("DELETE FROM headings")
    suspend fun deleteAllHeadings()

    @Query("SELECT metaValue FROM meta WHERE metaKey = :key")
    suspend fun getMeta(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMeta(item: MetaEntity)
}
