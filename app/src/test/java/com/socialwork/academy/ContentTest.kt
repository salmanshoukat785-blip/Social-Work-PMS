package com.socialwork.academy

import com.socialwork.academy.data.ContentBundle
import com.socialwork.academy.data.ContentParser
import com.socialwork.academy.data.TopicCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Checks the bundled study content (assets/content/content.json). */
class ContentTest {

    private val bundle: ContentBundle by lazy {
        // Unit tests run inside the app module folder.
        val file = File("src/main/assets/content/content.json")
        assertTrue("content.json not found at ${file.absolutePath}", file.exists())
        ContentParser.parse(file.readText(Charsets.UTF_8))
    }

    @Test
    fun everyContentTopicExistsInTheTopicList() {
        assertTrue(bundle.topics.keys.all { TopicCatalog.byId(it) != null })
    }

    @Test
    fun mostTopicsHaveMaterial() {
        val withMaterial = bundle.topics.values.count { it.sections.isNotEmpty() }
        assertEquals(33, withMaterial)
    }

    @Test
    fun headingOnlyTopicsAreEmptyNotInvented() {
        listOf("p1_society", "p1_social_institutions", "p2_methods").forEach {
            assertTrue("$it should have no points", bundle.forTopic(it)?.sections.orEmpty().isEmpty())
        }
    }

    @Test
    fun everySectionHasItemsAndNoBlankLines() {
        bundle.topics.values.forEach { topic ->
            topic.sections.forEach { s ->
                // A heading that only groups sub-headings (level 0 or 1) may have no points.
                assertTrue(s.itemsEn.isNotEmpty() || s.itemsUr.isNotEmpty() || s.level <= 1)
                assertTrue(s.itemsEn.all { it.isNotBlank() })
                assertTrue(s.itemsUr.all { it.isNotBlank() })
            }
        }
    }

    @Test
    fun noHiddenSymbolCharactersInTheText() {
        // Word's bullet symbol (private-use U+F0B7) must not reach the app.
        fun bad(s: String) = s.any { it in ''..'' }
        bundle.topics.values.forEach { topic ->
            topic.sections.forEach { s ->
                assertTrue(!bad(s.headingEn) && !bad(s.headingUr))
                assertTrue(s.itemsEn.none { bad(it) } && s.itemsUr.none { bad(it) })
            }
        }
    }

    @Test
    fun familySectionHasTheTwelveUrduPoints() {
        val family = bundle.forTopic("p1_family")
        assertNotNull(family)
        assertEquals(12, family!!.sections.sumOf { it.itemsUr.size })
    }

    @Test
    fun primaryMethodsListTheThreeMethods() {
        val items = bundle.forTopic("p2_primary_methods")!!.sections.flatMap { it.itemsEn }
        assertEquals(listOf("Social Case Work", "Social Group Work", "Community Development"), items)
    }

    @Test
    fun caseWorkAndItsPhasesAreSeparateTopics() {
        val headings = bundle.forTopic("p2_case_work")!!.sections.map { it.headingEn }
        assertTrue(headings.contains("Fields of Social Case Work"))
        assertTrue(headings.contains("Elements Of Social Case Work"))
        assertTrue(headings.contains("Objectives"))
        assertTrue(!headings.contains("Diagnosis"))
        val phases = bundle.forTopic("p2_case_work_phases")!!.sections.map { it.headingEn }
        assertEquals(listOf("Study", "Diagnosis", "Treatment", "Follow up"), phases)
    }

    @Test
    fun normsAndSocializationHaveTwoHeadings() {
        val s = bundle.forTopic("p1_norms_socialization")!!.sections
        assertEquals(listOf("Social Norms", "Factor Of Socialization"), s.map { it.headingEn })
        assertEquals(listOf("Undefined Acts", "Folkways", "Mores", "Laws"), s[0].itemsEn)
        assertEquals(5, s[1].itemsEn.size)
        assertEquals(4, s[0].itemsUr.size)
        assertEquals(5, s[1].itemsUr.size)
    }

    @Test
    fun nestedTopicsHaveTheThreeLevels() {
        val case = bundle.forTopic("p2_case_work")!!.sections
        assertEquals(listOf("Person", "Problem", "Place", "Professional", "Process"),
            case.filter { it.level == 2 }.map { it.headingEn })

        val group = bundle.forTopic("p2_group_work")!!.sections
        assertEquals(listOf("Group", "Agency", "Group Work"), group.filter { it.level == 2 }.map { it.headingEn })

        val community = bundle.forTopic("p2_community_development")!!.sections
        assertEquals(listOf("Study", "Planning", "Implementation", "Evaluation"),
            community.filter { it.level == 2 }.map { it.headingEn })
    }

    @Test
    fun aSubHeadingAlwaysFollowsAHeading() {
        bundle.topics.values.forEach { topic ->
            val levels = topic.sections.map { it.level }
            if (levels.isNotEmpty()) assertTrue(topic.topicId, levels.first() != 2)
        }
    }
}
