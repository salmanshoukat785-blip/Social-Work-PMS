package com.islamicstudies.academy.data.importer

import android.content.Context
import androidx.room.withTransaction
import com.islamicstudies.academy.data.db.AppDatabase
import com.islamicstudies.academy.data.db.ChapterEntity
import com.islamicstudies.academy.data.db.HeadingEntity
import com.islamicstudies.academy.data.db.MetaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Imports assets/master_outline.json (chapter > sub-heading > topic) into the database.
 * Runs only when OUTLINE_VERSION changes. User data (progress, bookmarks, notes) is stored in
 * separate tables and is never touched here.
 */
object OutlineImporter {

    const val OUTLINE_VERSION = "outline-1"
    private const val META_KEY = "outline_version"
    private const val ASSET_NAME = "master_outline.json"

    suspend fun importIfNeeded(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        val dao = db.contentDao()
        if (dao.getMeta(META_KEY) == OUTLINE_VERSION) return@withContext

        val text = context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val chapters = ArrayList<ChapterEntity>()
        val headings = ArrayList<HeadingEntity>()
        parse(text, chapters, headings)

        db.withTransaction {
            dao.deleteAllHeadings()
            dao.deleteAllChapters()
            dao.insertChapters(chapters)
            dao.insertHeadings(headings)
            dao.setMeta(MetaEntity(META_KEY, OUTLINE_VERSION))
        }
    }

    private fun parse(
        json: String,
        chaptersOut: MutableList<ChapterEntity>,
        headingsOut: MutableList<HeadingEntity>
    ) {
        val root = JSONObject(json)
        val chapters = root.getJSONArray("chapters")
        for (i in 0 until chapters.length()) {
            val c = chapters.getJSONObject(i)
            val chapterId = c.getString("id")
            chaptersOut.add(
                ChapterEntity(
                    id = chapterId,
                    number = c.getInt("order"),
                    titleUr = c.getString("title"),
                    titleEn = c.optString("title_en", ""),
                    sortIndex = i,
                    srcRef = c.optString("src", "")
                )
            )
            val counter = IntArray(1)
            walk(c.optJSONArray("children"), chapterId, null, 1, counter, headingsOut)
        }
    }

    private fun walk(
        children: JSONArray?,
        chapterId: String,
        parentId: String?,
        depth: Int,
        counter: IntArray,
        out: MutableList<HeadingEntity>
    ) {
        if (children == null) return
        for (i in 0 until children.length()) {
            val n = children.getJSONObject(i)
            val id = n.getString("id")
            counter[0] = counter[0] + 1
            out.add(
                HeadingEntity(
                    id = id,
                    chapterId = chapterId,
                    parentId = parentId,
                    type = n.getString("type"),
                    depth = depth,
                    titleUr = n.getString("title"),
                    titleEn = n.optString("title_en", ""),
                    sortIndex = counter[0],
                    srcRef = n.optString("src", "")
                )
            )
            walk(n.optJSONArray("children"), chapterId, id, depth + 1, counter, out)
        }
    }
}
