package com.socialwork.academy

import com.socialwork.academy.data.ContentBundle
import com.socialwork.academy.data.ContentParser
import com.socialwork.academy.data.FontScale
import com.socialwork.academy.data.ReadingText
import com.socialwork.academy.data.SearchEngine
import com.socialwork.academy.data.Topic
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.data.TopicContent
import com.socialwork.academy.data.TopicSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReadingTest {

    private val topicA = Topic("a", 1, "Family", "خاندان")
    private val topicB = Topic("b", 1, "Zakat and charity", "زکوٰۃ اور خیرات")
    private val topicC = Topic("c", 2, "Methods", "طریقے")

    private val bundle = ContentBundle(
        source = "test",
        topics = mapOf(
            "a" to TopicContent("a", listOf(
                TopicSection("", "", listOf("Reproduction", "Education & training"), listOf("تولید", "تعلیم اور تربیت"))
            )),
            "b" to TopicContent("b", listOf(
                TopicSection("Concept", "تصور", listOf("Zakat system", "Sadaqat"), listOf("نظامِ زکوٰۃ", "صدقات"))
            )),
            "c" to TopicContent("c", emptyList())
        )
    )
    private val topics = listOf(topicA, topicB, topicC)

    // ---- Font size ----

    @Test
    fun fontScaleMovesOneStepAndStopsAtTheEnds() {
        assertEquals(1.15f, FontScale.larger(1.0f), 0.0001f)
        assertEquals(0.85f, FontScale.smaller(1.0f), 0.0001f)
        assertEquals(1.6f, FontScale.larger(1.6f), 0.0001f)
        assertEquals(0.85f, FontScale.smaller(0.85f), 0.0001f)
        assertFalse(FontScale.canGrow(1.6f))
        assertFalse(FontScale.canShrink(0.85f))
        assertTrue(FontScale.canGrow(1.0f) && FontScale.canShrink(1.0f))
    }

    @Test
    fun fontScaleSnapsAnOddSavedValueToTheNearestStep() {
        assertEquals(1, FontScale.nearestIndex(1.02f))
    }

    // ---- Copy / share text ----

    @Test
    fun readingTextHasTitleAndBothLanguages() {
        val text = ReadingText.build(topicA, bundle.forTopic("a")!!.sections, isUrdu = false)
        assertTrue(text.startsWith("Family\nخاندان"))
        assertTrue(text.contains("• Reproduction"))
        assertTrue(text.contains("• تولید"))
        // English first when the app is English
        assertTrue(text.indexOf("• Reproduction") < text.indexOf("• تولید"))
    }

    @Test
    fun readingTextPutsUrduFirstInUrduMode() {
        val text = ReadingText.build(topicA, bundle.forTopic("a")!!.sections, isUrdu = true)
        assertTrue(text.startsWith("خاندان\nFamily"))
        assertTrue(text.indexOf("• تولید") < text.indexOf("• Reproduction"))
    }

    // ---- Search ----

    @Test
    fun shortQueriesGiveNoResults() {
        assertTrue(SearchEngine.search("a", topics, bundle).isEmpty())
        assertTrue(SearchEngine.search("   ", topics, bundle).isEmpty())
    }

    @Test
    fun englishSearchIgnoresCaseAndFindsNotesText() {
        val hits = SearchEngine.search("ZAKAT", topics, bundle)
        assertEquals(listOf("b"), hits.map { it.topic.id })
        assertTrue(hits[0].titleMatch)
    }

    @Test
    fun searchFindsTextInsideNotes() {
        val hits = SearchEngine.search("reproduction", topics, bundle)
        assertEquals(1, hits.size)
        assertFalse(hits[0].titleMatch)
        assertEquals(1, hits[0].matchCount)
        assertEquals("Reproduction", hits[0].snippets[0])
    }

    @Test
    fun urduSearchIgnoresDiacritics() {
        // typed without the marks that the text contains
        val hits = SearchEngine.search("زکوۃ", topics, bundle)
        assertEquals("b", hits.first().topic.id)
    }

    @Test
    fun urduSearchTreatsArabicAndUrduLettersAlike() {
        // Arabic yeh (U+064A) typed instead of Urdu yeh (U+06CC)
        val hits = SearchEngine.search("طريقے", topics, bundle)
        assertEquals(listOf("c"), hits.map { it.topic.id })
    }

    @Test
    fun noMatchGivesEmptyList() {
        assertTrue(SearchEngine.search("zzzzqq", topics, bundle).isEmpty())
    }

    @Test
    fun searchWorksWithoutLoadedContentUsingTitles() {
        val hits = SearchEngine.search("family", topics, null)
        assertEquals(listOf("a"), hits.map { it.topic.id })
    }

    @Test
    fun titleMatchesComeBeforeNotesMatches() {
        val t1 = Topic("x", 1, "Other", "دوسرا")
        val t2 = Topic("y", 1, "Education", "تعلیم")
        val b = ContentBundle("t", mapOf(
            "x" to TopicContent("x", listOf(TopicSection("", "", listOf("education 1", "education 2"), emptyList()))),
            "y" to TopicContent("y", emptyList())
        ))
        val hits = SearchEngine.search("education", listOf(t1, t2), b)
        assertEquals(listOf("y", "x"), hits.map { it.topic.id })
    }

    // ---- Real bundled content ----

    @Test
    fun realContentFindsZakatAndBookmarkableTopics() {
        val file = File("src/main/assets/content/content.json")
        assertTrue(file.exists())
        val real = ContentParser.parse(file.readText(Charsets.UTF_8))
        val hits = SearchEngine.search("zakat", TopicCatalog.all, real)
        assertTrue(hits.any { it.topic.id == "p2_islam_social_work" })
        val urdu = SearchEngine.search("زکوۃ", TopicCatalog.all, real)
        assertTrue(urdu.any { it.topic.id == "p2_islam_social_work" })
    }
}
