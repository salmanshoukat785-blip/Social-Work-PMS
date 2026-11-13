package com.islamicstudies.academy.data.importer

import android.content.Context
import androidx.room.withTransaction
import com.islamicstudies.academy.data.db.AppDatabase
import com.islamicstudies.academy.data.db.MetaEntity
import com.islamicstudies.academy.data.db.QuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Imports every JSON file inside the assets folder named questions into the questions table.
 * Each file: { "version": "x", "questions": [ { "id", "chapterId", "headingId" (optional),
 * "difficulty": "basic|intermediate|advanced", "textUr", "textEn", "optionsUr": [..],
 * "optionsEn": [..], "correctIndex": 0, "explanationUr", "explanationEn" } ] }
 * The import re-runs only when a file name or its "version" changes.
 */
object QuestionImporter {

    private const val META_KEY = "questions_signature"
    private const val DIR = "questions"

    suspend fun importIfNeeded(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        val contentDao = db.contentDao()
        val quizDao = db.quizDao()
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
        if (contentDao.getMeta(META_KEY) == signature) return@withContext

        val rows = ArrayList<QuestionEntity>()
        for (doc in docs) {
            val items = doc.second.optJSONArray("questions") ?: continue
            for (i in 0 until items.length()) {
                val q = items.getJSONObject(i)
                rows.add(
                    QuestionEntity(
                        id = q.getString("id"),
                        chapterId = q.getString("chapterId"),
                        headingId = q.optString("headingId", ""),
                        difficulty = q.optString("difficulty", "basic"),
                        textUr = q.optString("textUr", ""),
                        textEn = q.optString("textEn", ""),
                        optionsUrJson = (q.optJSONArray("optionsUr") ?: JSONArray()).toString(),
                        optionsEnJson = (q.optJSONArray("optionsEn") ?: JSONArray()).toString(),
                        correctIndex = q.getInt("correctIndex"),
                        explanationUr = q.optString("explanationUr", ""),
                        explanationEn = q.optString("explanationEn", "")
                    )
                )
            }
        }

        db.withTransaction {
            quizDao.deleteAllQuestions()
            quizDao.insertQuestions(rows)
            contentDao.setMeta(MetaEntity(META_KEY, signature))
        }
    }
}
