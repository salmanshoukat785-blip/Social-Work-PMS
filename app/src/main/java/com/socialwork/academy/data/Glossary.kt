package com.socialwork.academy.data

import android.content.Context
import org.json.JSONObject

/**
 * One English / Urdu pair taken from the notes. The two languages are matched by their
 * position in the lists of the Word file, so this is a study aid, not a dictionary.
 */
data class GlossaryTerm(
    val id: String,
    /** "topic", "heading" or "point": where in the notes the pair comes from. */
    val kind: String,
    val topicId: String,
    val paper: Int,
    val en: String,
    val ur: String
)

data class GlossaryBundle(val label: String, val terms: List<GlossaryTerm>)

sealed interface GlossaryState {
    data object Loading : GlossaryState
    data class Ready(val bundle: GlossaryBundle) : GlossaryState
    data object Failed : GlossaryState
}

object GlossaryParser {
    fun parse(json: String): GlossaryBundle {
        val root = JSONObject(json)
        val array = root.getJSONArray("terms")
        val list = (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            GlossaryTerm(
                id = o.getString("id"),
                kind = o.optString("kind"),
                topicId = o.getString("topicId"),
                paper = o.getInt("paper"),
                en = o.getString("en"),
                ur = o.getString("ur")
            )
        }
        return GlossaryBundle(root.optString("label"), list)
    }
}

object GlossaryRepository {
    private const val ASSET_PATH = "content/glossary.json"

    fun load(context: Context): GlossaryBundle? = try {
        val text = context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
        GlossaryParser.parse(text)
    } catch (e: Exception) {
        null
    }
}

object GlossarySearch {

    /** Terms whose English or Urdu text contains the query (Urdu spelling variants ignored). */
    fun filter(
        terms: List<GlossaryTerm>,
        query: String,
        paper: Int? = null
    ): List<GlossaryTerm> {
        val q = SearchEngine.normalize(query.trim())
        return terms.filter { t ->
            (paper == null || t.paper == paper) &&
                (q.isEmpty() ||
                    SearchEngine.normalize(t.en).contains(q) ||
                    SearchEngine.normalize(t.ur).contains(q))
        }
    }
}

/** Which cards go into a flashcard round. */
object FlashDeck {

    fun build(
        terms: List<GlossaryTerm>,
        paper: Int?,
        onlyUnknown: Boolean,
        known: Set<String>,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): List<GlossaryTerm> =
        terms
            .filter { (paper == null || it.paper == paper) && (!onlyUnknown || it.id !in known) }
            .shuffled(random)
}
