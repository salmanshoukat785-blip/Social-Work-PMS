package com.islamicstudies.academy.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A main heading (bold heading in the DOCX), e.g. "توحید". */
@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val number: Int,
    val titleUr: String,
    val titleEn: String,
    val sortIndex: Int,
    val srcRef: String
)

/**
 * A sub-heading (highlighted in the DOCX) or a topic (a line under it).
 * type is "subheading" or "topic". depth 1 = directly under the chapter.
 */
@Entity(
    tableName = "headings",
    indices = [Index("chapterId"), Index("parentId")]
)
data class HeadingEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val parentId: String?,
    val type: String,
    val depth: Int,
    val titleUr: String,
    val titleEn: String,
    val sortIndex: Int,
    val srcRef: String,
    val reviewStatus: String = "Draft"
)

/** Lesson text blocks (filled by the content parts later). */
@Entity(tableName = "lesson_sections", indices = [Index("headingId")])
data class LessonSectionEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val headingId: String,
    val lang: String,
    val level: String,
    val kind: String,
    val orderIdx: Int,
    val body: String
)

@Entity(tableName = "questions", indices = [Index("chapterId"), Index("headingId")])
data class QuestionEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val headingId: String,
    val difficulty: String,
    val textUr: String,
    val textEn: String,
    val optionsUrJson: String,
    val optionsEnJson: String,
    val correctIndex: Int,
    val explanationUr: String,
    val explanationEn: String,
    val reviewStatus: String = "Draft"
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val mode: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val durationSec: Int,
    val detailsJson: String
)

// ---- User data. No foreign keys on purpose: re-importing content never deletes user data. ----

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val headingId: String,
    val completed: Boolean,
    val lastOpenedAt: Long
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val headingId: String,
    val createdAt: Long
)

@Entity(tableName = "personal_notes")
data class PersonalNoteEntity(
    @PrimaryKey val headingId: String,
    val text: String,
    val updatedAt: Long
)

/** Small key/value table (e.g. imported content version). */
@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey val metaKey: String,
    val metaValue: String
)
