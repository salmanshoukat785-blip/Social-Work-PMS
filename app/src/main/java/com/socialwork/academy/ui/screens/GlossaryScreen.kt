package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.GlossarySearch
import com.socialwork.academy.data.GlossaryState
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.StudyStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlossaryScreen(
    strings: AppStrings,
    st: StudyStrings,
    isUrdu: Boolean,
    state: GlossaryState,
    onBack: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var paper by rememberSaveable { mutableStateOf(0) } // 0 = both

    Column(modifier = Modifier.fillMaxSize()) {
        ToolHeader(st.glossaryTitle, strings.back, onBack)
        when (state) {
            GlossaryState.Loading -> Text(st.loading, modifier = Modifier.padding(16.dp))
            GlossaryState.Failed -> InfoCard(st.loadError, Modifier.padding(16.dp))
            is GlossaryState.Ready -> {
                val terms = remember(state, query, paper) {
                    GlossarySearch.filter(state.bundle.terms, query, paper.takeIf { it != 0 })
                }
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(st.searchTerms) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(paper == 0, { paper = 0 }, { Text(st.allPapers) })
                        FilterChip(paper == 1, { paper = 1 }, { Text(strings.paper1) })
                        FilterChip(paper == 2, { paper = 2 }, { Text(strings.paper2) })
                    }
                    Text(
                        st.termsCount(terms.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { InfoCard(st.glossaryNote) }
                    if (terms.isEmpty()) {
                        item { Text(st.noTerms) }
                    }
                    items(terms, key = { it.id }) { term ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                McqDirection(isUrdu = false) {
                                    Text(term.en, style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.fillMaxWidth())
                                }
                                McqDirection(isUrdu = true) {
                                    Text(term.ur, style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.fillMaxWidth())
                                }
                                val topic = TopicCatalog.byId(term.topicId)
                                if (topic != null) {
                                    Text(
                                        topic.title(isUrdu),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
