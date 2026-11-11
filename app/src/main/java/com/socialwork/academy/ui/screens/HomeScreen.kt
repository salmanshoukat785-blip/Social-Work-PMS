package com.socialwork.academy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialwork.academy.ui.AppStrings

@Composable
fun HomeScreen(
    strings: AppStrings,
    profileName: String,
    onOpenPaper: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = if (profileName.isNotBlank()) strings.welcomeName(profileName) else strings.welcome,
            subtitle = strings.tagline
        )
        PaperCard(
            title = strings.paper1,
            subtitle = strings.paper1Sub,
            icon = Icons.Filled.AccountBalance,
            onClick = { onOpenPaper(1) }
        )
        PaperCard(
            title = strings.paper2,
            subtitle = strings.paper2Sub,
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = { onOpenPaper(2) }
        )
        InfoCard(text = strings.offlineNote)
    }
}
