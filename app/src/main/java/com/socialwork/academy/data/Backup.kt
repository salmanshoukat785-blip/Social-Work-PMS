package com.socialwork.academy.data

import org.json.JSONArray
import org.json.JSONObject

/** Everything the student created in the app, for a backup file. */
data class BackupData(
    val createdAtMillis: Long,
    val settings: UserSettings,
    val notes: Map<String, String>,
    val knownCards: Set<String>,
    val attempts: List<QuizAttempt>,
    val topicStats: Map<String, TopicScore>,
    val activity: Map<String, DayActivity>
)

/**
 * The backup file is plain JSON text kept on the phone (or wherever the student saves it).
 * It has no network part. A file that is not one of ours is rejected instead of half-loaded.
 */
object BackupJson {

    const val APP = "SocialWorkAcademy"
    const val VERSION = 1

    fun encode(d: BackupData): String {
        val s = d.settings
        val settings = JSONObject()
            .put("isUrdu", s.isUrdu)
            .put("profileName", s.profileName)
            .put("institution", s.institution)
            .put("completedTopics", JSONArray(s.completedTopics.toList()))
            .put("bookmarks", JSONArray(s.bookmarks.toList()))
            .put("fontScale", s.fontScale.toDouble())
        s.darkMode?.let { settings.put("darkMode", it) }
        s.lastTopicId?.let { settings.put("lastTopicId", it) }
        return JSONObject()
            .put("app", APP)
            .put("backupVersion", VERSION)
            .put("createdAt", d.createdAtMillis)
            .put("settings", settings)
            .put("notes", JSONObject(StudyJson.encodeNotes(d.notes)))
            .put("knownCards", JSONArray(d.knownCards.toList()))
            .put("attempts", JSONArray(QuizJson.encodeAttempts(d.attempts)))
            .put("topicStats", JSONObject(QuizJson.encodeStats(d.topicStats)))
            .put("activity", JSONObject(ActivityJson.encode(d.activity)))
            .toString()
    }

    /** Returns null if the text is not a valid backup of this app. */
    fun decode(text: String): BackupData? {
        return try {
            val root = JSONObject(text)
            if (root.optString("app") != APP) return null
            if (root.optInt("backupVersion", -1) != VERSION) return null
            val s = root.getJSONObject("settings")
            val topicIds = TopicCatalog.all.map { it.id }.toSet()
            fun ids(name: String): Set<String> {
                val a = s.optJSONArray(name) ?: return emptySet()
                return (0 until a.length()).map { a.getString(it) }.filter { it in topicIds }.toSet()
            }
            val settings = UserSettings(
                isUrdu = s.optBoolean("isUrdu", false),
                darkMode = if (s.has("darkMode")) s.getBoolean("darkMode") else null,
                setupDone = true,
                profileName = s.optString("profileName"),
                institution = s.optString("institution"),
                completedTopics = ids("completedTopics"),
                lastTopicId = s.optString("lastTopicId").takeIf { it in topicIds },
                bookmarks = ids("bookmarks"),
                fontScale = FontScale.steps[FontScale.nearestIndex(s.optDouble("fontScale", 1.0).toFloat())]
            )
            BackupData(
                createdAtMillis = root.optLong("createdAt"),
                settings = settings,
                notes = StudyJson.decodeNotes(root.optJSONObject("notes")?.toString())
                    .filterKeys { it in topicIds },
                knownCards = StudyJson.decodeIds(root.optJSONArray("knownCards")?.toString()),
                attempts = QuizJson.decodeAttempts(root.optJSONArray("attempts")?.toString()),
                topicStats = QuizJson.decodeStats(root.optJSONObject("topicStats")?.toString())
                    .filterKeys { it in topicIds },
                activity = ActivityJson.decode(root.optJSONObject("activity")?.toString())
            )
        } catch (e: Exception) {
            null
        }
    }
}
