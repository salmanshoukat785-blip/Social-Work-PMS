package com.socialwork.academy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.socialwork.academy.data.FlashDeck
import com.socialwork.academy.data.GlossaryState
import com.socialwork.academy.data.GlossaryTerm
import com.socialwork.academy.data.TopicCatalog
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.StudyStrings

/** Flashcards made from the glossary terms. Progress ("I know it") is kept on the phone. */
@Composable
fun FlashcardsScreen(
    strings: AppStrings,
    st: StudyStrings,
    isUrdu: Boolean,
    state: GlossaryState,
    known: Set<String>,
    onSetKnown: (String, Boolean) -> Unit,
    onClearKnown: () -> Unit,
    onBack: () -> Unit
) {
    // The queue of the round in progress; null while choosing cards.
    var queue by remember { mutableStateOf<androidx.compose.runtime.snapshots.SnapshotStateList<GlossaryTerm>?>(null) }
    var total by remember { mutableStateOf(0) }
    var knownThisRound by remember { mutableStateOf(0) }
    var againIds by remember { mutableStateOf(setOf<String>()) }
    var frontUrdu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ToolHeader(st.flashcardsTitle, strings.back, onBack)
        when (state) {
            GlossaryState.Loading -> Text(st.loading, modifier = Modifier.padding(16.dp))
            GlossaryState.Failed -> InfoCard(st.loadError, Modifier.padding(16.dp))
            is GlossaryState.Ready -> {
                val current = queue
                if (current == null) {
                    DeckSetup(
                        strings, st, state.bundle.terms, known, frontUrdu,
                        onFrontChange = { frontUrdu = it },
                        onClearKnown = onClearKnown,
                        onStart = { deck ->
                            queue = mutableStateListOf<GlossaryTerm>().apply { addAll(deck) }
                            total = deck.size
                            knownThisRound = 0
                            againIds = emptySet()
                        }
                    )
                } else if (current.isEmpty()) {
                    RoundDone(st, knownThisRound, againIds.size,
                        onNewRound = { queue = null }, onExit = onBack)
                } else {
                    CardSession(
                        st = st, isUrdu = isUrdu, term = current.first(), frontUrdu = frontUrdu,
                        position = knownThisRound + 1, total = total, knownThisRound = knownThisRound,
                        onKnow = {
                            val card = current.removeAt(0)
                            onSetKnown(card.id, true)
                            knownThisRound++
                        },
                        onAgain = {
                            val card = current.removeAt(0)
                            current.add(card)
                            againIds = againIds + card.id
                            onSetKnown(card.id, false)
                        },
                        onStop = { queue = null }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeckSetup(
    strings: AppStrings,
    st: StudyStrings,
    terms: List<GlossaryTerm>,
    known: Set<String>,
    frontUrdu: Boolean,
    onFrontChange: (Boolean) -> Unit,
    onClearKnown: () -> Unit,
    onStart: (List<GlossaryTerm>) -> Unit
) {
    var paper by remember { mutableStateOf(0) }
    var onlyNew by remember { mutableStateOf(false) }
    val deck = FlashDeck.build(terms, paper.takeIf { it != 0 }, onlyNew, known)
    val knownCount = terms.count { it.id in known }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoCard(st.glossaryNote)
        Text(st.deckTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.asHeading())
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(paper == 0, { paper = 0 }, { Text(st.allPapers) })
            FilterChip(paper == 1, { paper = 1 }, { Text(strings.paper1) })
            FilterChip(paper == 2, { paper = 2 }, { Text(strings.paper2) })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(st.onlyNew, modifier = Modifier.weight(1f))
            Switch(checked = onlyNew, onCheckedChange = { onlyNew = it })
        }
        Text(st.frontLanguage, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!frontUrdu, { onFrontChange(false) }, { Text(st.english) })
            FilterChip(frontUrdu, { onFrontChange(true) }, { Text(st.urdu) })
        }
        Text(
            st.knownCount(knownCount, terms.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (deck.isEmpty()) st.noCards else st.termsCount(deck.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = { onStart(deck) },
            enabled = deck.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(st.startCards) }
        if (knownCount > 0) {
            OutlinedButton(onClick = onClearKnown, modifier = Modifier.fillMaxWidth()) {
                Text(st.resetKnown)
            }
        }
    }
}

@Composable
private fun CardSession(
    st: StudyStrings,
    isUrdu: Boolean,
    term: GlossaryTerm,
    frontUrdu: Boolean,
    position: Int,
    total: Int,
    knownThisRound: Int,
    onKnow: () -> Unit,
    onAgain: () -> Unit,
    onStop: () -> Unit
) {
    // A new card always starts on its front side.
    var flipped by remember(term.id) { mutableStateOf(false) }
    val showUrdu = if (flipped) !frontUrdu else frontUrdu

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(st.cardOf(position.coerceAtMost(total), total), style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else knownThisRound / total.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
                .clickable(onClickLabel = st.tapToFlip) { flipped = !flipped },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (flipped) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    McqDirection(isUrdu = showUrdu) {
                        Text(
                            text = if (showUrdu) term.ur else term.en,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (flipped) {
                        TopicCatalog.byId(term.topicId)?.let {
                            Text(
                                it.title(isUrdu),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        if (!flipped) {
            Text(
                st.tapToFlip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAgain, modifier = Modifier.weight(1f)) { Text(st.showAgain) }
                Button(onClick = onKnow, modifier = Modifier.weight(1f)) { Text(st.iKnow) }
            }
        }
        OutlinedButton(onClick = onStop, modifier = Modifier.fillMaxWidth()) { Text(st.exit) }
    }
}

@Composable
private fun RoundDone(st: StudyStrings, known: Int, again: Int, onNewRound: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoCard(st.roundDone + "\n" + st.roundSummary(known, again))
        Button(onClick = onNewRound, modifier = Modifier.fillMaxWidth()) { Text(st.newRound) }
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text(st.exit) }
    }
}
