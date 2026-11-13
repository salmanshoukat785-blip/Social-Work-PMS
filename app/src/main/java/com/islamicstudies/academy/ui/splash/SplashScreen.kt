package com.islamicstudies.academy.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.islamicstudies.academy.ui.EnStrings
import com.islamicstudies.academy.ui.UrStrings
import com.islamicstudies.academy.ui.theme.Gold
import com.islamicstudies.academy.ui.theme.Navy
import com.islamicstudies.academy.ui.theme.UrduText
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val latestOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(1200)
        latestOnFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Navy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val r = size.minDimension
                drawCircle(
                    color = Gold,
                    radius = r * 0.30f,
                    center = Offset(size.width * 0.50f, size.height * 0.50f)
                )
                drawCircle(
                    color = Navy,
                    radius = r * 0.26f,
                    center = Offset(size.width * 0.58f, size.height * 0.45f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = UrStrings.appName, style = UrduText.title, color = Gold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = EnStrings.appName, color = Color.White)
        }
    }
}
