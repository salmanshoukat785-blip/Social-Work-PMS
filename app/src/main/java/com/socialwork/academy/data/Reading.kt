package com.socialwork.academy.data

import kotlin.math.abs

/** Reading text size. The user moves between these fixed steps. */
object FontScale {
    val steps: List<Float> = listOf(0.85f, 1.0f, 1.15f, 1.3f, 1.45f, 1.6f)
    const val DEFAULT: Float = 1.0f

    fun nearestIndex(value: Float): Int =
        steps.indices.minByOrNull { abs(steps[it] - value) } ?: steps.indexOf(DEFAULT)

    fun larger(value: Float): Float = steps[(nearestIndex(value) + 1).coerceAtMost(steps.lastIndex)]

    fun smaller(value: Float): Float = steps[(nearestIndex(value) - 1).coerceAtLeast(0)]

    fun canGrow(value: Float): Boolean = nearestIndex(value) < steps.lastIndex

    fun canShrink(value: Float): Boolean = nearestIndex(value) > 0
}

/** Plain text of a topic, used by the Copy and Share buttons. */
object ReadingText {

    fun build(topic: Topic, sections: List<TopicSection>, isUrdu: Boolean): String {
        val out = StringBuilder()
        out.append(topic.title(isUrdu)).append('\n')
        out.append(topic.otherTitle(isUrdu)).append("\n")
        sections.forEach { s ->
            out.append('\n')
            val heading = s.heading(isUrdu)
            val other = s.otherHeading(isUrdu)
            if (heading.isNotBlank()) out.append(heading).append('\n')
            if (other.isNotBlank()) out.append(other).append('\n')
            val ordered = if (isUrdu) listOf(s.itemsUr, s.itemsEn) else listOf(s.itemsEn, s.itemsUr)
            ordered.forEach { items ->
                items.forEach { out.append("• ").append(it).append('\n') }
            }
        }
        return out.toString().trimEnd()
    }
}

/** One topic that matched a search. */
data class SearchHit(
    val topic: Topic,
    val titleMatch: Boolean,
    /** Number of matching lines inside the topic's notes. */
    val matchCount: Int,
    /** Up to a few matching lines to show under the topic name. */
    val snippets: List<String>
)

object SearchEngine {

    const val MIN_QUERY_LENGTH = 2
    private const val MAX_SNIPPETS = 3

    // Urdu diacritics (zer, zabar, pesh, khari zabar ...) and tatweel are ignored,
    // and the Arabic forms of yeh / kaf / heh are treated like the Urdu ones,
    // because keyboards differ in which one they type.
    private val IGNORED = Regex("[\\u064B-\\u065F\\u0670\\u0640]")
    private val SPACES = Regex("\\s+")

    fun normalize(text: String): String =
        text.replace(IGNORED, "")
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ه', 'ہ')
            .lowercase()
            .replace(SPACES, " ")
            .trim()

    fun search(query: String, topics: List<Topic>, bundle: ContentBundle?): List<SearchHit> {
        val q = normalize(query)
        if (q.length < MIN_QUERY_LENGTH) return emptyList()

        val hits = ArrayList<SearchHit>()
        for (topic in topics) {
            val titleMatch = normalize(topic.titleEn).contains(q) || normalize(topic.titleUr).contains(q)

            var count = 0
            val snippets = ArrayList<String>()
            bundle?.forTopic(topic.id)?.sections?.forEach { s ->
                val lines = buildList {
                    add(s.headingEn); add(s.headingUr)
                    addAll(s.itemsEn); addAll(s.itemsUr)
                }
                lines.forEach { line ->
                    if (line.isNotBlank() && normalize(line).contains(q)) {
                        count++
                        if (snippets.size < MAX_SNIPPETS) snippets += line
                    }
                }
            }
            if (titleMatch || count > 0) hits += SearchHit(topic, titleMatch, count, snippets)
        }
        // sortedWith is stable: equal hits stay in topic-list order.
        return hits.sortedWith(compareByDescending<SearchHit> { it.titleMatch }.thenByDescending { it.matchCount })
    }
}
