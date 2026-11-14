package com.socialwork.academy

import com.socialwork.academy.data.ContentParser
import com.socialwork.academy.data.DetailsParser
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.DetailStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Checks assets/content/details.json against the Word-file content. */
class DetailsTest {
    private val details by lazy {
        val f = File("src/main/assets/content/details.json")
        assertTrue(f.exists())
        DetailsParser.parse(f.readText(Charsets.UTF_8))
    }
    private val content by lazy {
        ContentParser.parse(File("src/main/assets/content/content.json").readText(Charsets.UTF_8))
    }

    @Test
    fun paperOneHasThirteenTopics() {
        assertEquals(13, details.topics.size)
        assertTrue(details.topics.keys.all { TopicCatalog.byId(it) != null })
    }

    @Test
    fun everySubHeadingHasShortAndLongText() {
        details.topics.values.forEach { t ->
            (t.en + t.ur).forEach {
                assertTrue(it.heading, it.heading.isNotBlank() && it.short.isNotBlank() && it.long.isNotBlank())
                assertTrue(it.heading, it.long.length > it.short.length)
            }
        }
    }

    @Test
    fun headingsMatchTheWordFileItems() {
        details.topics.values.forEach { t ->
            val sections = content.forTopic(t.topicId)!!.sections
            assertEquals(t.topicId, sections.flatMap { it.itemsEn }.map { it.trim() }, t.en.map { it.heading })
            assertEquals(t.topicId, sections.flatMap { it.itemsUr }.map { it.trim() }, t.ur.map { it.heading })
        }
    }

    @Test
    fun stringsMatchInBothLanguages() {
        assertTrue(DetailStrings.English.all().all { it.isNotBlank() })
        assertTrue(DetailStrings.Urdu.all().all { it.isNotBlank() })
        assertNotNull(DetailStrings.of(true))
    }
}
