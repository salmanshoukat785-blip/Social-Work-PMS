package com.islamicstudies.academy.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.islamicstudies.academy.BuildConfig

@Database(
    entities = [
        ChapterEntity::class,
        HeadingEntity::class,
        LessonSectionEntity::class,
        QuestionEntity::class,
        QuizAttemptEntity::class,
        UserProgressEntity::class,
        BookmarkEntity::class,
        PersonalNoteEntity::class,
        MetaEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contentDao(): ContentDao
    abstract fun userDao(): UserDao
    abstract fun quizDao(): QuizDao

    companion object {
        fun build(context: Context): AppDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "islamic_academy.db"
            )
            // Development builds only: if the schema changes between test APKs, rebuild the DB.
            // Release builds will get proper migrations before the final release.
            if (BuildConfig.DEBUG) {
                builder.fallbackToDestructiveMigration()
            }
            return builder.build()
        }
    }
}
