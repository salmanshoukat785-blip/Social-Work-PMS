package com.socialwork.academy.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Study content taken from the supplied "Social Work Paper 1 & 2" Word file.
 * The text is stored exactly as in the file (see assets/content/content.json).
 * Nothing in here is written by the app; new explanations added in the
 * future must be stored separately and labelled "Additional Explanation".
 */
data class TopicSection(
    val headingEn: String,
    val headingUr: String,
    val itemsEn: List<String>,
    val itemsUr: List<String>
) {
    fun heading(isUrdu: Boolean): String = if (isUrdu) headingUr else headingEn
    fun otherHeading(isUrdu: Boolean): String = if (isUrdu) headingEn else headingUr
}

data class TopicContent(
    val topicId: String,
    val sections: List<TopicSection>
)

data class ContentBundle(
    val source: String,
    val topics: Map<String, TopicContent>
) {
    fun forTopic(topicId: String): TopicContent? = topics[topicId]
}

/** Loading state of the bundled content, so screens never show a blank page. */
sealed interface ContentState {
    data object Loading : ContentState
    data class Ready(val bundle: ContentBundle) : ContentState
    data object Failed : ContentState
}

object ContentParser {

    fun parse(json: String): ContentBundle {
        val root = JSONObject(json)
        val topics = LinkedHashMap<String, TopicContent>()
        val array = root.getJSONArray("topics")
        for (i in 0 until array.length()) {
            val t = array.getJSONObject(i)
            val id = t.getString("id")
            val sections = t.getJSONArray("sections")
            val list = ArrayList<TopicSection>()
            for (j in 0 until sections.length()) {
                val s = sections.getJSONObject(j)
                list += TopicSection(
                    headingEn = s.optString("headingEn").trim(),
                    headingUr = s.optString("headingUr").trim(),
                    itemsEn = s.getJSONArray("itemsEn").toStringList(),
                    itemsUr = s.getJSONArray("itemsUr").toStringList()
                )
            }
            topics[id] = TopicContent(id, list)
        }
        return ContentBundle(source = root.optString("source"), topics = topics)
    }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { getString(it) }
}

object ContentRepository {

    private const val ASSET_PATH = "content/content.json"

    /** Reads the bundled content. Returns null if the file cannot be read. */
    fun load(context: Context): ContentBundle? = try {
        val text = context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
        ContentParser.parse(text)
    } catch (e: Exception) {
        null
    }
}
