package com.islamicstudies.academy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamicstudies.academy.data.settings.AppSettings
import com.islamicstudies.academy.ui.theme.UrduText

/** The languages to show for quiz content: follows the content-language setting ("both" shows two). */
fun contentLanguages(settings: AppSettings, uiLanguage: String): List<String> {
    val lang = if (settings.contentLanguage.isBlank()) uiLanguage else settings.contentLanguage
    return if (lang == "both") listOf("ur", "en") else listOf(lang)
}

/** Builds (language, text) pairs for the wanted languages; falls back to whatever text exists. */
fun linesFor(ur: String, en: String, langs: List<String>): List<Pair<String, String>> {
    val out = ArrayList<Pair<String, String>>()
    for (l in langs) {
        val t = if (l == "ur") ur else en
        if (t.isNotBlank()) out.add(Pair(l, t))
    }
    if (out.isEmpty()) {
        if (ur.isNotBlank()) out.add(Pair("ur", ur)) else if (en.isNotBlank()) out.add(Pair("en", en))
    }
    return out
}

/** Shows each (language, text) line with the right direction and font style. */
@Composable
fun LangLines(
    lines: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    scale: Float = 1f
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            val isUrdu = line.first == "ur"
            val direction = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
            val style = if (isUrdu) {
                if (bold) {
                    UrduText.subheading.copy(fontSize = (18 * scale).sp, lineHeight = (32 * scale).sp)
                } else {
                    UrduText.body.copy(fontSize = (17 * scale).sp, lineHeight = (30 * scale).sp)
                }
            } else {
                if (bold) {
                    MaterialTheme.typography.titleMedium.copy(fontSize = (17 * scale).sp, lineHeight = (24 * scale).sp)
                } else {
                    MaterialTheme.typography.bodyLarge.copy(fontSize = (16 * scale).sp, lineHeight = (24 * scale).sp)
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                Text(text = line.second, style = style, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
