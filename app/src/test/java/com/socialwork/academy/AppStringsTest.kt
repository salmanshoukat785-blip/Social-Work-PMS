package com.socialwork.academy

import com.socialwork.academy.ui.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStringsTest {

    @Test
    fun englishTextsAreNotBlank() {
        assertTrue(AppStrings.English.all().all { it.isNotBlank() })
    }

    @Test
    fun urduTextsAreNotBlank() {
        assertTrue(AppStrings.Urdu.all().all { it.isNotBlank() })
    }

    @Test
    fun bothLanguagesHaveSameNumberOfTexts() {
        assertEquals(AppStrings.English.all().size, AppStrings.Urdu.all().size)
    }

    @Test
    fun urduAppNameDiffersFromEnglish() {
        assertNotEquals(AppStrings.English.appName, AppStrings.Urdu.appName)
    }

    @Test
    fun welcomeNameInsertsTheName() {
        assertTrue(AppStrings.English.welcomeName("Ali").contains("Ali"))
        assertTrue(AppStrings.Urdu.welcomeName("علی").contains("علی"))
    }
}
