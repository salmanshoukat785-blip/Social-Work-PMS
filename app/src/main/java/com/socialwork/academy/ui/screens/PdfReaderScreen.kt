package com.socialwork.academy.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.socialwork.academy.ui.AppStrings
import com.socialwork.academy.ui.StudyStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** An open PDF, drawn page by page with Android's own PdfRenderer (works offline). */
class PdfDoc private constructor(
    private val fd: ParcelFileDescriptor,
    private val renderer: PdfRenderer
) {
    val pageCount: Int = renderer.pageCount
    private val lock = Any()
    private var closed = false

    /** Page size ratio (width / height) of the first page, used for placeholders. */
    val firstPageRatio: Float = synchronized(lock) {
        renderer.openPage(0).use { it.width.toFloat() / it.height.toFloat() }
    }

    /** Draws one page at the given width. Returns null if the file was closed or the page failed. */
    suspend fun render(index: Int, targetWidth: Int): Bitmap? = withContext(Dispatchers.IO) {
        synchronized(lock) {
            if (closed) return@synchronized null
            try {
                renderer.openPage(index).use { page ->
                    val h = (targetWidth.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(targetWidth, h, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            runCatching { renderer.close() }
            runCatching { fd.close() }
        }
    }

    companion object {
        /** Returns null for a file that is not a PDF, is damaged, is password-protected or has no pages. */
        fun open(context: Context, uri: Uri): PdfDoc? {
            val fd = try {
                context.contentResolver.openFileDescriptor(uri, "r")
            } catch (e: Exception) {
                null
            } ?: return null
            return try {
                val renderer = PdfRenderer(fd)
                if (renderer.pageCount == 0) {
                    renderer.close()
                    fd.close()
                    null
                } else {
                    PdfDoc(fd, renderer)
                }
            } catch (e: Exception) {
                runCatching { fd.close() }
                null
            }
        }
    }
}

@Composable
fun PdfReaderScreen(strings: AppStrings, st: StudyStrings, onBack: () -> Unit) {
    val context = LocalContext.current
    var uriText by rememberSaveable { mutableStateOf<String?>(null) }
    var failed by rememberSaveable { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            uriText = uri.toString()
            failed = false
        }
    }

    val doc = remember(uriText) { uriText?.let { PdfDoc.open(context, Uri.parse(it)) } }
    DisposableEffect(doc) { onDispose { doc?.close() } }
    val hasError = failed || (uriText != null && doc == null)

    Column(modifier = Modifier.fillMaxSize()) {
        ToolHeader(st.pdfTitle, strings.back, onBack)
        if (doc == null) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InfoCard(st.pdfNote)
                if (hasError) {
                    Text(st.pdfError, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = { picker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(st.pdfChoose) }
            }
        } else {
            PdfPages(st, doc) { picker.launch(arrayOf("application/pdf")) }
        }
    }
}

@Composable
private fun PdfPages(st: StudyStrings, doc: PdfDoc, onChooseAnother: () -> Unit) {
    val listState = rememberLazyListState()
    val widthPx = LocalContext.current.resources.displayMetrics.widthPixels.coerceIn(480, 1600)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(doc.pageCount) { index ->
                val bitmap by produceState<Bitmap?>(null, doc, index) {
                    value = doc.render(index, widthPx)
                }
                val shown = bitmap
                if (shown != null) {
                    Image(
                        bitmap = shown.asImageBitmap(),
                        contentDescription = st.pdfPageDescription + " " + (index + 1),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(doc.firstPageRatio)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            }
            item {
                OutlinedButton(
                    onClick = onChooseAnother,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text(st.pdfChooseAnother) }
            }
        }
        Text(
            text = st.pageOf(listState.firstVisibleItemIndex.coerceAtMost(doc.pageCount - 1) + 1, doc.pageCount),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
