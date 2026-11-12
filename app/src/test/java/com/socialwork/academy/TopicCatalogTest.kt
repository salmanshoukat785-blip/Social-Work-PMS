package com.socialwork.academy

import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.data.progressFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicCatalogTest {

    @Test
    fun topicIdsAreUnique() {
        val ids = TopicCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun titlesAreNotBlank() {
        assertTrue(TopicCatalog.all.all { it.titleEn.isNotBlank() && it.titleUr.isNotBlank() })
    }

    @Test
    fun papersHaveTheirOwnTopics() {
        assertTrue(TopicCatalog.paper1.all { it.paper == 1 })
        assertTrue(TopicCatalog.paper2.all { it.paper == 2 })
        assertEquals(TopicCatalog.paper1.size + TopicCatalog.paper2.size, TopicCatalog.all.size)
    }

    @Test
    fun lookupById() {
        assertNotNull(TopicCatalog.byId("p1_society"))
        assertNull(TopicCatalog.byId("does_not_exist"))
        assertNull(TopicCatalog.byId(null))
    }

    @Test
    fun progressCountsOnlyKnownCompletedTopics() {
        val stats = progressFor(TopicCatalog.all, setOf("p1_society", "p2_methods", "unknown_id"))
        assertEquals(TopicCatalog.all.size, stats.total)
        assertEquals(2, stats.completed)
        assertEquals(TopicCatalog.all.size - 2, stats.remaining)
    }

    @Test
    fun progressIsZeroWithNothingCompleted() {
        val stats = progressFor(TopicCatalog.paper1, emptySet())
        assertEquals(0, stats.completed)
        assertEquals(0f, stats.fraction, 0.0001f)
    }
}
