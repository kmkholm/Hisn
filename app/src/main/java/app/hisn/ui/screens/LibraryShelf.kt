package app.hisn.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hisn.content.Novella
import app.hisn.domain.LibItem
import app.hisn.domain.LibType
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.ProgressBar
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.UiState
import app.hisn.ui.VSpace
import app.hisn.ui.theme.Accent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * المكتبة الشخصية.
 *
 * الفكرة العلاجية خلفها: القراءة الطويلة من أفضل بدائل التمرير القهري —
 * تشغل العقل بعمق، وتُنهي اليوم بإيقاع هادئ. هنا يضيف المستخدم كتبه
 * (PDF) ونصوصه الطويلة بعناوينها، ويعود إليها من حيث توقف.
 */
@Composable
fun LibraryShelfScreen(
    state: UiState,
    novellaChapter: Int,
    onAddPdf: (title: String, uri: String) -> Unit,
    onAddText: (title: String, content: String) -> Unit,
    onDelete: (Long) -> Unit,
    onOpen: (LibItem) -> Unit,
    onOpenNovella: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pendingPdfUri by remember { mutableStateOf<Uri?>(null) }
    var showTextDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<LibItem?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // إذن دائم حتى تبقى القراءة ممكنة بعد إعادة التشغيل.
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            pendingPdfUri = uri
        }
    }

    StaticBackdrop(tint = Accent.Sand) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 60.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مكتبتي 📚", style = MaterialTheme.typography.headlineMedium)
                    TextButton(onClick = onBack) { Text("رجوع") }
                }
                Text(
                    "أضف كتبك ونصوصك الطويلة، وعُد إليها من حيث توقفت. " +
                        "القراءة العميقة من أفضل ما يُنهى به اليوم.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                        Text("  إضافة PDF")
                    }
                    OutlinedButton(
                        onClick = { showTextDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Description, null, modifier = Modifier.size(18.dp))
                        Text("  إضافة نص")
                    }
                }
            }

            // ── الرواية المدمجة ──
            item {
                val done = novellaChapter >= Novella.chapters.size
                SectionCard(
                    modifier = Modifier.clickable { onOpenNovella() },
                    border = Accent.Sand.copy(alpha = .55f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(46.dp).clip(CircleShape)
                                .background(Accent.Sand.copy(alpha = .18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AutoStories, null,
                                tint = Accent.Sand, modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(Novella.TITLE, style = MaterialTheme.typography.titleLarge)
                            Text(
                                Novella.SUBTITLE,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    ProgressBar(
                        (novellaChapter.toFloat() / Novella.chapters.size).coerceIn(0f, 1f),
                        height = 6
                    )
                    Text(
                        if (done) "أنهيت الرواية ✓ — يمكنك إعادتها متى شئت"
                        else if (novellaChapter == 0) "لم تبدأ بعد — الفصل الأول بانتظارك"
                        else "توقفت عند الفصل ${novellaChapter + 1} من ${Novella.chapters.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.libraryItems.isEmpty()) {
                item {
                    SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            "رفّك الخاص فارغ بعد. أضف أول كتاب PDF من جهازك، أو الصق " +
                                "نصًا طويلًا بعنوانه — قصة، مقالًا، أو حتى رواية كاملة.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item { Eyebrow("رفّك (${state.libraryItems.size})") }
                items(state.libraryItems.size) { i ->
                    val item = state.libraryItems[i]
                    SectionCard(modifier = Modifier.clickable { onOpen(item) }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(44.dp).clip(CircleShape).background(
                                    (if (item.type == LibType.PDF) Accent.Coral
                                    else Accent.Cyan).copy(alpha = .16f)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (item.type == LibType.PDF) Icons.Outlined.PictureAsPdf
                                    else Icons.Outlined.MenuBook,
                                    null,
                                    tint = if (item.type == LibType.PDF) Accent.Coral
                                    else Accent.Cyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    (if (item.type == LibType.PDF) "PDF" else "نص") +
                                        if (item.lastPage > 0)
                                            " · توقفت عند صفحة ${item.lastPage + 1}"
                                        else " · لم تبدأ بعد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { confirmDelete = item }) {
                                Icon(
                                    Icons.Outlined.Delete, "حذف",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── حوار تسمية ملف الـPDF المُختار ──
    pendingPdfUri?.let { uri ->
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { pendingPdfUri = null },
            title = { Text("عنوان الكتاب") },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    placeholder = { Text("مثال: رياض الصالحين") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddPdf(title.ifBlank { "كتاب بلا عنوان" }, uri.toString())
                        pendingPdfUri = null
                    }
                ) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { pendingPdfUri = null }) { Text("إلغاء") }
            }
        )
    }

    // ── حوار إضافة نص طويل ──
    if (showTextDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text("إضافة نص طويل") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        label = { Text("العنوان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("النص — الصق هنا") },
                        minLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (body.isNotBlank()) {
                            onAddText(title.ifBlank { "نص بلا عنوان" }, body)
                        }
                        showTextDialog = false
                    }
                ) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) { Text("إلغاء") }
            }
        )
    }

    confirmDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("حذف «${item.title}»؟") },
            text = { Text("سيُحذف من رفّك. الملف الأصلي على جهازك لا يتأثر.") },
            confirmButton = {
                TextButton(onClick = { onDelete(item.id); confirmDelete = null }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("إلغاء") }
            }
        )
    }
}

// ═════════════════ قارئ PDF ═════════════════

/**
 * قارئ PDF بصفحة واحدة وتقليب — [PdfRenderer] لا يتحمل فتح صفحات متوازية،
 * فالعرض صفحة-بصفحة أكثر أمانًا ويجعل حفظ الموضع طبيعيًا.
 */
@Composable
fun PdfReaderScreen(
    item: LibItem,
    onSaveProgress: (Long, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(item.lastPage) }
    var pageCount by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    val bitmap by produceState<Bitmap?>(null, item.id, page) {
        value = withContext(Dispatchers.IO) {
            try {
                context.contentResolver
                    .openFileDescriptor(Uri.parse(item.uri), "r")
                    ?.use { pfd -> renderPdfPage(pfd, page) { pageCount = it } }
            } catch (e: SecurityException) {
                error = "انتهى إذن الوصول للملف. احذفه من الرف وأضفه من جديد."
                null
            } catch (e: Exception) {
                error = "تعذّر فتح الملف — ربما نُقل أو حُذف من الجهاز."
                null
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                if (pageCount > 0) {
                    Text(
                        "صفحة ${page + 1} من $pageCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = { onSaveProgress(item.id, page); onBack() }) { Text("رجوع") }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when {
                error != null -> Text(
                    error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
                bitmap == null -> CircularProgressIndicator()
                else -> Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "صفحة ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (pageCount > 1) {
            Slider(
                value = page.toFloat(),
                onValueChange = { page = it.toInt().coerceIn(0, pageCount - 1) },
                valueRange = 0f..(pageCount - 1).toFloat()
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { if (page > 0) { page--; onSaveProgress(item.id, page) } },
                modifier = Modifier.weight(1f),
                enabled = page > 0
            ) { Text("السابقة") }
            Button(
                onClick = {
                    if (page < pageCount - 1) { page++; onSaveProgress(item.id, page) }
                },
                modifier = Modifier.weight(1f),
                enabled = page < pageCount - 1
            ) { Text("التالية") }
        }
    }
}

private fun renderPdfPage(
    pfd: ParcelFileDescriptor,
    pageIndex: Int,
    onCount: (Int) -> Unit
): Bitmap? = PdfRenderer(pfd).use { renderer ->
    onCount(renderer.pageCount)
    if (pageIndex >= renderer.pageCount) return@use null
    renderer.openPage(pageIndex).use { p ->
        val targetW = 1080
        val targetH = (targetW.toFloat() / p.width * p.height).toInt()
        val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(android.graphics.Color.WHITE)
        p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        bmp
    }
}

// ═════════════════ قارئ النص الطويل ═════════════════

@Composable
fun TextReaderScreen(item: LibItem, onBack: () -> Unit) {
    var fontSize by remember { mutableIntStateOf(18) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (fontSize > 14) fontSize -= 2 }) { Text("أ−") }
                TextButton(onClick = { if (fontSize < 28) fontSize += 2 }) { Text("أ+") }
                TextButton(onClick = onBack) { Text("رجوع") }
            }
        }
        VSpace(8)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                item.content.orEmpty(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.9).sp
                )
            )
            VSpace(60)
        }
    }
}

// ═════════════════ قارئ الرواية المدمجة ═════════════════

@Composable
fun NovellaReaderScreen(
    startChapter: Int,
    onProgress: (Int) -> Unit,
    onBack: () -> Unit
) {
    var chapter by remember {
        mutableIntStateOf(startChapter.coerceIn(0, Novella.chapters.lastIndex))
    }
    val ch = Novella.chapters[chapter]

    StaticBackdrop(tint = Accent.Sand) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Pill(
                    "فصل ${chapter + 1} من ${Novella.chapters.size}",
                    bg = Accent.Sand.copy(alpha = .18f),
                    fg = Accent.Sand
                )
                TextButton(onClick = onBack) { Text("رجوع") }
            }
            VSpace(6)
            Text(
                "عمل خيالي مؤلَّف",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            VSpace(10)

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState(0), true)
            ) {
                Text(ch.title, style = MaterialTheme.typography.headlineSmall)
                VSpace(14)
                ch.body.forEach { para ->
                    Text(
                        para,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 32.sp)
                    )
                    VSpace(14)
                }
                VSpace(30)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { if (chapter > 0) chapter-- },
                    modifier = Modifier.weight(1f),
                    enabled = chapter > 0
                ) { Text("الفصل السابق") }
                Button(
                    onClick = {
                        if (chapter < Novella.chapters.lastIndex) {
                            chapter++
                            onProgress(chapter)
                        } else {
                            onProgress(Novella.chapters.size)
                            onBack()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (chapter < Novella.chapters.lastIndex) "الفصل التالي"
                        else "إنهاء الرواية ✓"
                    )
                }
            }
        }
    }
}
