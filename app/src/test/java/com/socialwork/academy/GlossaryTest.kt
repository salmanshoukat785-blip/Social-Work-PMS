package com.socialwork.academy

import com.socialwork.academy.data.FlashDeck
import com.socialwork.academy.data.GlossaryBundle
import com.socialwork.academy.data.GlossaryParser
import com.socialwork.academy.data.GlossarySearch
import com.socialwork.academy.data.GlossaryTerm
import com.socialwork.academy.data.TopicCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class GlossaryTest {

    private val bundle: GlossaryBundle by lazy {
        val file = File("src/main/assets/content/glossary.json")
        assertTrue("glossary.json not found at ${file.absolutePath}", file.exists())
        GlossaryParser.parse(file.readText(Charsets.UTF_8))
    }

    @Test
    fun bundledTermsAreWellFormed() {
        assertTrue(bundle.terms.size >= 300)
        assertEquals(bundle.terms.size, bundle.terms.map { it.id }.toSet().size)
        bundle.terms.forEach { t ->
            assertTrue(t.id, t.en.isNotBlank() && t.ur.isNotBlank())
            val topic = TopicCatalog.byId(t.topicId)
            assertTrue("${t.id}: unknown topic", topic != null)
            assertEquals(t.id, topic!!.paper, t.paper)
        }
    }

    @Test
    fun noPlaceholderCharactersFromTheWordFile() {
        assertTrue(bundle.terms.none { it.en.contains('') || it.ur.contains('') })
    }

    @Test
    fun bundleSaysItIsNotADictionary() {
        assertTrue(bundle.label.contains("not a dictionary", ignoreCase = true))
    }

    private fun term(id: String, en: String, ur: String, paper: Int = 1) =
        GlossaryTerm(id, "point", "p1_family", paper, en, ur)

    private val sample = listOf(
        term("a", "Family", "خاندان"),
        term("b", "Case Work", "کیس ورک", paper = 2),
        term("c", "Culture", "ثقافت")
    )

    @Test
    fun searchFindsEnglishUrduAndIgnoresCase() {
        assertEquals(listOf("a"), GlossarySearch.filter(sample, "FAMILY").map { it.id })
        assertEquals(listOf("b"), GlossarySearch.filter(sample, "کیس").map { it.id })
        assertEquals(3, GlossarySearch.filter(sample, "  ").size)
    }

    @Test
    fun searchIgnoresUrduSpellingVariants() {
        // Arabic yeh / kaf / heh are treated like the Urdu letters.
        val t = listOf(term("x", "Society", "معاشرہ"))
        assertEquals(1, GlossarySearch.filter(t, "معاشره").size)
    }

    @Test
    fun searchCanBeLimitedToOnePaper() {
        assertEquals(listOf("b"), GlossarySearch.filter(sample, "", paper = 2).map { it.id })
    }

    @Test
    fun deckFiltersByPaperAndKnownAndHasNoRepeats() {
        val all = FlashDeck.build(sample, null, false, emptySet(), Random(1))
        assertEquals(3, all.size)
        assertEquals(setOf("b"), FlashDeck.build(sample, 2, false, emptySet(), Random(1)).map { it.id }.toSet())
        val unknown = FlashDeck.build(sample, null, true, setOf("a"), Random(1))
        assertEquals(setOf("b", "c"), unknown.map { it.id }.toSet())
    }
}
