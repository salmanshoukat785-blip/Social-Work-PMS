package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.ReviewItem
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.QuizStrings

/** Every question of a finished quiz with the right answer, your answer and the explanation. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizReviewScreen(
    strings: AppStrings,
    qs: QuizStrings,
    items: List<ReviewItem>,
    onBack: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf("all") } // all, wrong, unanswered
    val shown = remember(items, filter) {
        items.withIndex().filter { (_, item) ->
            when (filter) {
                "wrong" -> item.isAnswered && !item.isCorrect
                "unanswered" -> !item.isAnswered
                else -> true
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(qs.reviewTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
        }
        if (items.isEmpty()) {
            InfoCard(text = qs.loadError, modifier = Modifier.padding(16.dp))
            return@Column
        }
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(filter == "all", { filter = "all" }, { Text(qs.filterAll) })
            FilterChip(filter == "wrong", { filter = "wrong" }, { Text(qs.filterWrong) })
            FilterChip(
                filter == "unanswered", { filter = "unanswered" }, { Text(qs.filterUnanswered) }
            )
        }
        if (shown.isEmpty()) {
            InfoCard(text = qs.noItemsInFilter, modifier = Modifier.padding(16.dp))
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // The number shown is the question's place in the quiz, even when filtered.
            itemsIndexed(shown) { _, entry -> ReviewCard(qs, entry.index + 1, entry.value) }
        }
    }
}

@Composable
private fun ReviewCard(qs: QuizStrings, number: Int, item: ReviewItem) {
    val q = item.question
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        McqDirection(isUrdu = q.isUrdu) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "$number. " + when {
                        !item.isAnswered -> qs.notAnswered
                        item.isCorrect -> qs.correct
                        else -> qs.wrong
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (item.isAnswered && item.isCorrect) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
                QuestionText(q)
                q.options.forEachIndexed { i, text ->
                    val mark = when {
                        i == q.answerIndex -> OptionMark.Correct
                        i == item.chosen -> OptionMark.Wrong
                        else -> OptionMark.None
                    }
                    OptionRow(text = text, mark = mark, qs = qs, enabled = false, onClick = {})
                }
                ExplanationCard(q, qs)
            }
        }
    }
}
