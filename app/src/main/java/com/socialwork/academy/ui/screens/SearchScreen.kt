package com.socialwork.academy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.ContentState
import com.socialwork.academy.data.SearchEngine
import com.socialwork.academy.data.SearchHit
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings

/** Searches topic names and the notes text of every topic, offline, in English and Urdu. */
@Composable
fun SearchScreen(
    strings: AppStrings,
    isUrdu: Boolean,
    content: ContentState,
    onOpenTopic: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    val bundle = (content as? ContentState.Ready)?.bundle
    val hits = remember(query, bundle) { SearchEngine.search(query, TopicCatalog.all, bundle) }
    val focus = remember { FocusRequester() }
    // Open the keyboard straight away; ignore the rare case where the field is not ready yet.
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
            Text(text = strings.searchTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.asHeading())
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(strings.searchHint) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .focusRequester(focus)
        )

        val enough = SearchEngine.normalize(query).length >= SearchEngine.MIN_QUERY_LENGTH
        when {
            !enough -> InfoCard(text = strings.searchPrompt, modifier = Modifier.padding(16.dp))
            hits.isEmpty() -> InfoCard(text = strings.noResults, modifier = Modifier.padding(16.dp))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hits, key = { it.topic.id }) { hit ->
                    HitCard(hit = hit, strings = strings, isUrdu = isUrdu, onClick = { onOpenTopic(hit.topic.id) })
                }
            }
        }
    }
}

@Composable
private fun HitCard(hit: SearchHit, strings: AppStrings, isUrdu: Boolean, onClick: () -> Unit) {
    val paperLabel = if (hit.topic.paper == 2) strings.paper2 else strings.paper1
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = paperLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(text = hit.topic.title(isUrdu), style = MaterialTheme.typography.titleMedium)
            Text(
                text = hit.topic.otherTitle(isUrdu),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hit.matchCount > 0) {
                Text(
                    text = strings.matches(hit.matchCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                hit.snippets.forEach { line ->
                    Text(
                        text = "• $line",
                        style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Content),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
