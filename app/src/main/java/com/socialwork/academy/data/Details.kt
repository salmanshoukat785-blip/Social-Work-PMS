package com.socialwork.academy.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * "Additional Explanation": a short and a long explanation for each
 * sub-heading of the Word file. These texts are NOT from the Word file;
 * they were written to help study and are not teacher-reviewed.
 */
data class DetailEntry(val heading: String, val short: String, val long: String)

data class TopicDetails(
    val topicId: String,
    val en: List<DetailEntry>,
    val ur: List<DetailEntry>
) {
    fun entries(isUrdu: Boolean): List<DetailEntry> = if (isUrdu) ur else en
}

data class DetailsBundle(val topics: Map<String, TopicDetails>) {
    fun forTopic(topicId: String): TopicDetails? = topics[topicId]
}

object DetailsParser {
    fun parse(json: String): DetailsBundle {
        val root = JSONObject(json)
        val arr = root.getJSONArray("topics")
        val map = LinkedHashMap<String, TopicDetails>()
        for (i in 0 until arr.length()) {
            val t = arr.getJSONObject(i)
            val id = t.getString("id")
            map[id] = TopicDetails(id, entries(t.getJSONArray("en")), entries(t.getJSONArray("ur")))
        }
        return DetailsBundle(map)
    }

    private fun entries(a: JSONArray): List<DetailEntry> = (0 until a.length()).map {
        val o = a.getJSONObject(it)
        DetailEntry(o.getString("t").trim(), o.getString("s").trim(), o.getString("l").trim())
    }
}

object DetailsRepository {
    private const val ASSET_PATH = "content/details.json"

    /** Reads the bundled explanations. Null if unreadable (the screen then simply hides them). */
    fun load(context: Context): DetailsBundle? = try {
        val text = context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
        DetailsParser.parse(text)
    } catch (e: Exception) {
        null
    }
}
