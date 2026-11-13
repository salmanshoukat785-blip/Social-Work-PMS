package com.islamicstudies.academy.data.backup

import androidx.room.withTransaction
import com.islamicstudies.academy.data.db.AppDatabase
import com.islamicstudies.academy.data.db.BookmarkEntity
import com.islamicstudies.academy.data.db.PersonalNoteEntity
import com.islamicstudies.academy.data.db.QuizAttemptEntity
import com.islamicstudies.academy.data.db.UserProgressEntity
import com.islamicstudies.academy.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Saves and restores the learner's own data: progress, bookmarks, notes, quiz history,
 * study days and settings. Course content is not included (it ships inside the app),
 * and the PIN is never written to a backup file.
 */
object BackupManager {

    private const val FORMAT = "islamic-studies-academy-backup"
    private const val VERSION = 1

    suspend fun createBackup(db: AppDatabase, repo: SettingsRepository): String =
        withContext(Dispatchers.IO) {
            val userDao = db.userDao()
            val quizDao = db.quizDao()

            val root = JSONObject()
            root.put("format", FORMAT)
            root.put("version", VERSION)
            root.put("exportedAt", System.currentTimeMillis())
            root.put("settings", repo.exportSettings())

            val days = JSONArray()
            for (d in repo.studyDays.first()) days.put(d)
            root.put("studyDays", days)

            val progress = JSONArray()
            for (p in userDao.getAllProgress()) {
                progress.put(
                    JSONObject().put("h", p.headingId).put("c", p.completed).put("t", p.lastOpenedAt)
                )
            }
            root.put("progress", progress)

            val bookmarks = JSONArray()
            for (b in userDao.getAllBookmarks()) {
                bookmarks.put(JSONObject().put("h", b.headingId).put("t", b.createdAt))
            }
            root.put("bookmarks", bookmarks)

            val notes = JSONArray()
            for (n in userDao.getAllNotes()) {
                notes.put(JSONObject().put("h", n.headingId).put("text", n.text).put("t", n.updatedAt))
            }
            root.put("notes", notes)

            val attempts = JSONArray()
            for (a in quizDao.getAttemptsChronological()) {
                attempts.put(
                    JSONObject()
                        .put("startedAt", a.startedAt)
                        .put("mode", a.mode)
                        .put("total", a.totalQuestions)
                        .put("correct", a.correctCount)
                        .put("duration", a.durationSec)
                        .put("details", a.detailsJson)
                )
            }
            root.put("attempts", attempts)

            root.toString()
        }

    /** Returns true when the file was a valid backup and the data was replaced. */
    suspend fun restoreBackup(json: String, db: AppDatabase, repo: SettingsRepository): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val root = JSONObject(json)
                if (root.optString("format") != FORMAT) return@withContext false

                // Read everything first, so a damaged file changes nothing.
                val progress = ArrayList<UserProgressEntity>()
                val progressArr = root.optJSONArray("progress") ?: JSONArray()
                for (i in 0 until progressArr.length()) {
                    val o = progressArr.getJSONObject(i)
                    progress.add(
                        UserProgressEntity(o.getString("h"), o.optBoolean("c", false), o.optLong("t", 0L))
                    )
                }

                val bookmarks = ArrayList<BookmarkEntity>()
                val bookmarkArr = root.optJSONArray("bookmarks") ?: JSONArray()
                for (i in 0 until bookmarkArr.length()) {
                    val o = bookmarkArr.getJSONObject(i)
                    bookmarks.add(BookmarkEntity(o.getString("h"), o.optLong("t", 0L)))
                }

                val notes = ArrayList<PersonalNoteEntity>()
                val noteArr = root.optJSONArray("notes") ?: JSONArray()
                for (i in 0 until noteArr.length()) {
                    val o = noteArr.getJSONObject(i)
                    notes.add(PersonalNoteEntity(o.getString("h"), o.getString("text"), o.optLong("t", 0L)))
                }

                val attempts = ArrayList<QuizAttemptEntity>()
                val attemptArr = root.optJSONArray("attempts") ?: JSONArray()
                for (i in 0 until attemptArr.length()) {
                    val o = attemptArr.getJSONObject(i)
                    attempts.add(
                        QuizAttemptEntity(
                            startedAt = o.optLong("startedAt", 0L),
                            mode = o.optString("mode", "practice"),
                            totalQuestions = o.optInt("total", 0),
                            correctCount = o.optInt("correct", 0),
                            durationSec = o.optInt("duration", 0),
                            detailsJson = o.optString("details", "[]")
                        )
                    )
                }

                val days = HashSet<Long>()
                val dayArr = root.optJSONArray("studyDays") ?: JSONArray()
                for (i in 0 until dayArr.length()) days.add(dayArr.getLong(i))

                val userDao = db.userDao()
                val quizDao = db.quizDao()
                db.withTransaction {
                    userDao.clearProgress()
                    userDao.clearBookmarks()
                    userDao.clearNotes()
                    quizDao.clearAttempts()
                    userDao.insertProgressItems(progress)
                    userDao.insertBookmarkItems(bookmarks)
                    userDao.insertNoteItems(notes)
                    quizDao.insertAttemptItems(attempts)
                }
                repo.importSettings(root.optJSONObject("settings"), days)
                true
            } catch (e: Exception) {
                false
            }
        }
}
