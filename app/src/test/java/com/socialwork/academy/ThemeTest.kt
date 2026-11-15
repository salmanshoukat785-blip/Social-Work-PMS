package com.socialwork.academy

import com.socialwork.academy.ui.theme.ThemePalette
import com.socialwork.academy.ui.theme.ThemeStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun fourThemesWithUniqueIds() {
        assertEquals(4, ThemePalette.all.size)
        assertEquals(4, ThemePalette.all.map { it.id }.toSet().size)
    }

    @Test
    fun unknownIdGivesTheDefaultTheme() {
        assertEquals(ThemePalette.DEFAULT_ID, ThemePalette.byId("nope").id)
        assertEquals(ThemePalette.DEFAULT_ID, ThemePalette.byId(null).id)
        assertEquals("rose", ThemePalette.byId("rose").id)
    }

    @Test
    fun namesAndTextsExistInBothLanguages() {
        ThemePalette.all.forEach { assertTrue(it.nameEn.isNotBlank() && it.nameUr.isNotBlank()) }
        assertTrue(ThemeStrings.English.title.isNotBlank() && ThemeStrings.Urdu.title.isNotBlank())
    }
}
