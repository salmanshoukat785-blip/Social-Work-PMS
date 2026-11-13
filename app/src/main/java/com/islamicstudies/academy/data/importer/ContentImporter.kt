package com.islamicstudies.academy.data.importer

import android.content.Context
import androidx.room.withTransaction
import com.islamicstudies.academy.data.db.AppDatabase
import com.islamicstudies.academy.data.db.LessonSectionEntity
import com.islamicstudies.academy.data.db.MetaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Imports every JSON file inside the assets folder named content into lesson_sections.
 * File format:
 * { "version": "x", "lessons": [ { "headingId": "ch01-t01",
 *     "sections": [ { "lang": "ur|en", "level": "basic|intermediate|advanced",
 *                     "kind": "text|point|reference", "body": "..." } ] } ] }
 * The import re-runs only when a file name or its "version" changes.
 */
object ContentImporter {

    private const val META_KEY = "content_signature"
    private const val DIR = "content"

    suspend fun importIfNeeded(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        val dao = db.contentDao()
        val names = (context.assets.list(DIR) ?: emptyArray())
            .filter { n -> n.endsWith(".json") }
            .sorted()

        val docs = ArrayList<Pair<String, JSONObject>>()
        for (name in names) {
            val text = context.assets.open("$DIR/$name")
                .bufferedReader(Charsets.UTF_8)
                .use { r -> r.readText() }
            docs.add(Pair(name, JSONObject(text)))
        }

        val signature = docs.joinToString("|") { d -> d.first + ":" + d.second.optString("version", "0") }
        if (dao.getMeta(META_KEY) == signature) return@withContext

        val rows = ArrayList<LessonSectionEntity>()
        for (doc in docs) {
            val lessons = doc.second.optJSONArray("lessons") ?: continue
            for (i in 0 until lessons.length()) {
                val lesson = lessons.getJSONObject(i)
                val headingId = lesson.getString("headingId")
                val sections = lesson.optJSONArray("sections") ?: continue
                for (j in 0 until sections.length()) {
                    val sec = sections.getJSONObject(j)
                    rows.add(
                        LessonSectionEntity(
                            headingId = headingId,
                            lang = sec.getString("lang"),
                            level = sec.getString("level"),
                            kind = sec.optString("kind", "text"),
                            orderIdx = j,
                            body = sec.getString("body")
                        )
                    )
                }
            }
        }

        db.withTransaction {
            dao.deleteAllLessonSections()
            dao.insertLessonSections(rows)
            dao.setMeta(MetaEntity(META_KEY, signature))
        }
    }
}
