package com.socialwork.academy.ui.screens

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.Mcq
import com.socialwork.academy.data.QuizScope
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.QuizStrings
import java.util.Locale

/** How one answer option is shown. */
enum class OptionMark { None, Selected, Correct, Wrong }

/** Lays out question text in the direction of the question's own language. */
@Composable
fun McqDirection(isUrdu: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalLayoutDirection provides if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr,
        content = content
    )
}

@Composable
fun OptionRow(
    text: String,
    mark: OptionMark,
    qs: QuizStrings,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = when (mark) {
        OptionMark.None -> MaterialTheme.colorScheme.surfaceVariant
        OptionMark.Selected -> MaterialTheme.colorScheme.secondaryContainer
        OptionMark.Correct -> MaterialTheme.colorScheme.tertiaryContainer
        OptionMark.Wrong -> MaterialTheme.colorScheme.errorContainer
    }
    val icon = when (mark) {
        OptionMark.None -> Icons.Filled.RadioButtonUnchecked
        OptionMark.Selected -> Icons.Filled.RadioButtonChecked
        OptionMark.Correct -> Icons.Filled.CheckCircle
        OptionMark.Wrong -> Icons.Filled.Cancel
    }
    // The icon is not the only signal: correct and wrong options also say so in words.
    val tag = when (mark) {
        OptionMark.Correct -> qs.correct
        OptionMark.Wrong -> qs.wrong
        else -> null
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = mark != OptionMark.None,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics { if (tag != null) stateDescription = tag },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = text, style = MaterialTheme.typography.bodyLarge)
                if (tag != null) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Question text, then the explanation card if requested. */
@Composable
fun QuestionText(question: Mcq) {
    Text(
        text = question.question,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ExplanationCard(question: Mcq, qs: QuizStrings) {
    if (question.explanation.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = qs.explanation,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = question.explanation, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Name of a quiz scope in the app language, e.g. "Paper 1" or the topic title. */
fun scopeLabel(key: String, strings: AppStrings, qs: QuizStrings, isUrdu: Boolean): String =
    when (val scope = QuizScope.fromKey(key)) {
        null -> key
        QuizScope.Everything -> qs.scopeAll
        is QuizScope.ByPaper -> if (scope.paperId == 1) strings.paper1 else strings.paper2
        is QuizScope.ByTopic -> TopicCatalog.byId(scope.topicId)
            ?.let { if (isUrdu) it.titleUr else it.titleEn } ?: scope.topicId
    }

/** 75 seconds -> "01:15"; 3700 seconds -> "1:01:40". */
fun formatDuration(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, sec)
    else String.format(Locale.US, "%02d:%02d", m, sec)
}
