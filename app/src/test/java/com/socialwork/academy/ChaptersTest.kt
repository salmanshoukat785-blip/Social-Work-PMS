package com.socialwork.academy

import com.socialwork.academy.data.ChapterCatalog
import com.socialwork.academy.data.TopicCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The chapter list follows the PDF: 6 chapters in Paper 1, 13 in Paper 2. */
class ChaptersTest {

    @Test
    fun chapterCounts() {
        assertEquals(6, ChapterCatalog.paper1.size)
        assertEquals(13, ChapterCatalog.paper2.size)
    }

    @Test
    fun everyTopicIsInExactlyOneChapter() {
        val ids = ChapterCatalog.all.flatMap { it.allTopicIds }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(TopicCatalog.all.map { it.id }.toSet(), ids.toSet())
    }

    @Test
    fun chaptersStayInTheirOwnPaper() {
        ChapterCatalog.all.forEach { c ->
            c.allTopicIds.forEach { assertEquals(c.paper, TopicCatalog.byId(it)!!.paper) }
        }
    }

    @Test
    fun titlesAndKeysAreUsable() {
        assertTrue(ChapterCatalog.all.all { it.titleEn.isNotBlank() && it.titleUr.isNotBlank() })
        val keys = ChapterCatalog.all.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }
}
