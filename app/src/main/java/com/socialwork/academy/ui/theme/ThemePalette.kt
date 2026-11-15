package com.socialwork.academy.ui.theme

import androidx.compose.ui.graphics.Color

/** The main colours of one theme, for light or dark mode. */
data class PaletteColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color
)

/** A colour theme the user can pick in Settings: app colours plus the 3 note-level colours. */
data class ThemePalette(
    val id: String,
    val nameEn: String,
    val nameUr: String,
    val light: PaletteColors,
    val dark: PaletteColors,
    val lightLevels: HierarchyColors,
    val darkLevels: HierarchyColors
) {
    fun name(isUrdu: Boolean): String = if (isUrdu) nameUr else nameEn
    fun colors(dark: Boolean): PaletteColors = if (dark) this.dark else light
    fun levels(dark: Boolean): HierarchyColors = if (dark) darkLevels else lightLevels

    companion object {
        const val DEFAULT_ID = "emerald"

        private val W = Color(0xFFFFFFFF)

        val Emerald = ThemePalette(
            id = "emerald", nameEn = "Emerald", nameUr = "زمرد",
            light = PaletteColors(
                Color(0xFF0F766E), W, Color(0xFFB9ECE3), Color(0xFF00302B),
                Color(0xFF9A4A00), W, Color(0xFFFFDDB8), Color(0xFF3A1D00),
                Color(0xFFF5FAF9), Color(0xFFE0EEEB), Color(0xFF3F4948)
            ),
            dark = PaletteColors(
                Color(0xFF7ED8CC), Color(0xFF00302B), Color(0xFF0F5A52), Color(0xFFC8F3EC),
                Color(0xFFFFB871), Color(0xFF4A2600), Color(0xFF6B3A00), Color(0xFFFFE0C0),
                Color(0xFF0E1A19), Color(0xFF182B29), Color(0xFFBFC9C7)
            ),
            lightLevels = HierarchyColors(
                Color(0xFF0F766E), W, Color(0xFFFFE4C7), Color(0xFF4A2600), Color(0xFFD5F0EA), Color(0xFF0B3B36)
            ),
            darkLevels = HierarchyColors(
                Color(0xFF0F5A52), W, Color(0xFF5A3200), Color(0xFFFFE0C0), Color(0xFF1E4A44), Color(0xFFD0F0EA)
            )
        )

        val Royal = ThemePalette(
            id = "royal", nameEn = "Royal", nameUr = "شاہی",
            light = PaletteColors(
                Navy, W, NavyLight, NavyDark,
                Purple, W, PurpleLight, Color(0xFF2B0A57),
                SurfaceLight, Color(0xFFE5E3F0), Color(0xFF47464F)
            ),
            dark = PaletteColors(
                NavyLight, NavyDark, Navy, Color(0xFFDDE1FF),
                PurpleLight, Color(0xFF3F1478), Color(0xFF5A2A98), Color(0xFFF0DBFF),
                SurfaceDark, CardDark, Color(0xFFC8C5D0)
            ),
            lightLevels = HierarchyColors(
                Navy, W, Color(0xFFE5DBFA), Color(0xFF2B0A57), Color(0xFFD7E3FF), Color(0xFF0B1445)
            ),
            darkLevels = HierarchyColors(
                Color(0xFF2A3A9E), W, Color(0xFF4A2A82), Color(0xFFF0DBFF), Color(0xFF26335F), Color(0xFFDDE1FF)
            )
        )

        val Rose = ThemePalette(
            id = "rose", nameEn = "Rose", nameUr = "گلاب",
            light = PaletteColors(
                Color(0xFF9F1239), W, Color(0xFFFFD9DF), Color(0xFF3F0014),
                Color(0xFF475569), W, Color(0xFFDFE6EF), Color(0xFF1B2433),
                Color(0xFFFFF8F8), Color(0xFFF4E2E5), Color(0xFF524345)
            ),
            dark = PaletteColors(
                Color(0xFFFFB2BF), Color(0xFF5F0021), Color(0xFF84102F), Color(0xFFFFD9DF),
                Color(0xFFB9C4D6), Color(0xFF233044), Color(0xFF3A465A), Color(0xFFDCE4F0),
                Color(0xFF1A1114), Color(0xFF2D1D21), Color(0xFFD8C2C6)
            ),
            lightLevels = HierarchyColors(
                Color(0xFF9F1239), W, Color(0xFFFFE0E6), Color(0xFF4C0519), Color(0xFFE6ECF4), Color(0xFF1E293B)
            ),
            darkLevels = HierarchyColors(
                Color(0xFF84102F), W, Color(0xFF5C1A2A), Color(0xFFFFD9DF), Color(0xFF2F3B50), Color(0xFFDCE4F0)
            )
        )

        val Ocean = ThemePalette(
            id = "ocean", nameEn = "Ocean", nameUr = "سمندر",
            light = PaletteColors(
                Color(0xFF0B5CAD), W, Color(0xFFD3E4FF), Color(0xFF001C3A),
                Color(0xFF00696B), W, Color(0xFFB6EFF0), Color(0xFF002021),
                Color(0xFFF5F9FE), Color(0xFFE0E9F3), Color(0xFF42474E)
            ),
            dark = PaletteColors(
                Color(0xFFA4C8FF), Color(0xFF00315C), Color(0xFF00468A), Color(0xFFD3E4FF),
                Color(0xFF6FD9DB), Color(0xFF003738), Color(0xFF004F51), Color(0xFFB6EFF0),
                Color(0xFF0F1620), Color(0xFF1A2531), Color(0xFFC2C7CF)
            ),
            lightLevels = HierarchyColors(
                Color(0xFF0B5CAD), W, Color(0xFFD7ECF6), Color(0xFF063C52), Color(0xFFFFF0C2), Color(0xFF4A3800)
            ),
            darkLevels = HierarchyColors(
                Color(0xFF00468A), W, Color(0xFF1B4A5E), Color(0xFFD2EEFA), Color(0xFF5A4410), Color(0xFFFFE9B0)
            )
        )

        val all: List<ThemePalette> = listOf(Emerald, Royal, Rose, Ocean)

        /** An unknown or missing id gives the default theme. */
        fun byId(id: String?): ThemePalette = all.firstOrNull { it.id == id } ?: Emerald
    }
}

/** Texts for the theme card in Settings. */
data class ThemeStrings(val title: String, val note: String) {
    companion object {
        val English = ThemeStrings("Colour theme", "Pick the colours of the app. Works in light and dark mode.")
        val Urdu = ThemeStrings("رنگوں کا تھیم", "ایپ کے رنگ منتخب کریں۔ لائٹ اور ڈارک دونوں موڈ میں کام کرتا ہے۔")
        fun of(isUrdu: Boolean) = if (isUrdu) Urdu else English
    }
}
