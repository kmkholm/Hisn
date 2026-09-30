package app.hisn.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import app.hisn.content.AchCategory
import app.hisn.content.Achievement
import app.hisn.content.Achievements
import app.hisn.content.Tier
import app.hisn.ui.Backdrop
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.ProgressBar
import app.hisn.ui.ProgressRing
import app.hisn.ui.SectionCard
import app.hisn.ui.TrophyCup
import app.hisn.ui.UiState
import app.hisn.ui.VSpace
import java.io.File
import java.time.LocalDate

/** شاشة الكؤوس والرتب. */
@Composable
fun TrophiesScreen(
    state: UiState,
    onOpenCert: (String) -> Unit
) {
    val stats = state.gamifyStats
    val xp = state.xp
    val level = Achievements.levelFor(xp)
    val next = Achievements.nextLevel(xp)

    Backdrop {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("الإنجازات", style = MaterialTheme.typography.headlineMedium)
            }

            // ── بطاقة الرتبة ──
            item {
                SectionCard(border = MaterialTheme.colorScheme.primary.copy(alpha = .4f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Eyebrow("رتبتك")
                            Text(level.title, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "المستوى ${level.index} من ${Achievements.levels.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.WorkspacePremium, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                "$xp نقطة",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    ProgressBar(Achievements.levelProgress(xp), height = 8)
                    Text(
                        if (next != null)
                            "بقي ${next.minXp - xp} نقطة لرتبة «${next.title}»"
                        else "وصلت إلى أعلى رتبة.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "النقاط تُحسب من كل ما تفعله: الأيام النظيفة، الموجات المركوبة، " +
                            "التمارين، المسار، والأنشطة — ولا تنقص أبدًا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── جرّة الانتصارات ──
            item { WinsJar(state) }

            // ── الكأس القادمة ──
            item {
                val current = state.selected?.currentStreakDays() ?: 0
                val nextCup = Achievements.nextStreakCup(current)
                if (nextCup != null) {
                    val progress = current.toFloat() / nextCup.threshold
                    SectionCard(border = nextCup.tier.dark.copy(alpha = .45f)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProgressRing(
                                progress = progress,
                                modifier = Modifier.size(92.dp),
                                strokeWidth = 8.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                colors = listOf(nextCup.tier.light, nextCup.tier.dark)
                            ) {
                                TrophyCup(nextCup.tier, 52.dp)
                            }
                            Column(Modifier.weight(1f)) {
                                Eyebrow("الكأس القادمة", nextCup.tier.dark)
                                Text(nextCup.title, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "أنت في اليوم $current من ${nextCup.threshold} — كأس ${nextCup.tier.label}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                state.selected?.let { h ->
                                    app.hisn.ui.LiveCountdown(
                                        target = h.streakStart.plus(
                                            java.time.Duration.ofDays(nextCup.threshold.toLong())
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = nextCup.tier.dark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── الفئات ──
            AchCategory.entries.forEach { cat ->
                val items = Achievements.all.filter { it.category == cat }
                item {
                    VSpace(4)
                    Eyebrow(cat.label)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowItems.forEach { a ->
                                    TrophyTile(
                                        a = a,
                                        unlocked = a.id in state.unlocked,
                                        metric = Achievements.metric(a, stats),
                                        modifier = Modifier.weight(1f),
                                        onCert = { onOpenCert(a.id) }
                                    )
                                }
                                repeat(3 - rowItems.size) { Box(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyTile(
    a: Achievement,
    unlocked: Boolean,
    metric: Int,
    modifier: Modifier = Modifier,
    onCert: () -> Unit
) {
    var showDetail by remember { mutableStateOf(false) }
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (unlocked) a.tier.dark.copy(alpha = .55f)
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable { showDetail = true }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            TrophyCup(a.tier, 54.dp, locked = !unlocked)
            if (!unlocked) {
                Icon(
                    Icons.Outlined.Lock, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            "${a.threshold}",
            style = MaterialTheme.typography.titleMedium,
            color = if (unlocked) a.tier.dark else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            a.title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }

    if (showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            icon = {
                Box(contentAlignment = Alignment.Center) {
                    TrophyCup(a.tier, 72.dp, locked = !unlocked)
                }
            },
            title = { Text(a.title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Pill(
                        "كأس ${a.tier.label}",
                        bg = a.tier.dark.copy(alpha = .16f),
                        fg = a.tier.dark
                    )
                    Text(a.desc, textAlign = TextAlign.Center)
                    Text(
                        if (unlocked) "مفتوحة ✓"
                        else "تقدّمك: $metric من ${a.threshold}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (unlocked) a.tier.dark
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (unlocked && a.certEligible) {
                    TextButton(onClick = { showDetail = false; onCert() }) { Text("عرض الشهادة") }
                } else {
                    TextButton(onClick = { showDetail = false }) { Text("حسنًا") }
                }
            },
            dismissButton = if (unlocked && a.certEligible) {
                { TextButton(onClick = { showDetail = false }) { Text("إغلاق") } }
            } else null
        )
    }
}

/** احتفال فتح كؤوس جديدة — يُعرض فوق أي شاشة. */
@Composable
fun UnlockCelebrationDialog(
    unlocks: List<Achievement>,
    onOpenCert: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val top = unlocks.maxByOrNull { it.tier.ordinal } ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { TrophyCup(top.tier, 84.dp) },
        title = {
            Text(
                if (unlocks.size == 1) "كأس جديدة!" else "${unlocks.size} كؤوس جديدة!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                unlocks.take(4).forEach { a ->
                    Text(
                        "🏆 ${a.title} — ${a.tier.label}",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                if (unlocks.size > 4) {
                    Text(
                        "و${unlocks.size - 4} أخرى…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    top.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            val certAch = unlocks.filter { it.certEligible }.maxByOrNull { it.threshold }
            if (certAch != null) {
                TextButton(onClick = { onDismiss(); onOpenCert(certAch.id) }) { Text("شهادتي 🎓") }
            } else {
                TextButton(onClick = onDismiss) { Text("رائع!") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("متابعة") } }
    )
}

// ═════════════════ الشهادة ═════════════════

@Composable
fun CertificateScreen(
    achievement: Achievement,
    state: UiState,
    onSetName: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember(state.userName) { mutableStateOf(state.userName) }
    val habitLabel = state.habits.maxByOrNull { it.bestStreakDays() }?.displayName ?: ""
    val goldTop = MaterialTheme.colorScheme.background.toArgb()

    val bmp = remember(achievement.id, name) {
        renderCertificate(
            name = name.ifBlank { "بطل حِصن" },
            days = achievement.threshold,
            habitLabel = habitLabel,
            tier = achievement.tier,
            isProgram = achievement.category == AchCategory.PROGRAM
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("شهادة إنجاز")
            TextButton(onClick = onBack) { Text("رجوع") }
        }

        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "شهادة ${achievement.title}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(bmp.width.toFloat() / bmp.height)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; onSetName(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("الاسم على الشهادة (اختياري)") },
            supportingText = { Text("يبقى على جهازك — يظهر على الشهادة فقط") },
            singleLine = true
        )

        Button(
            onClick = { shareBitmap(context, bmp) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Share, null, modifier = Modifier.size(18.dp))
            Text("  مشاركة الشهادة")
        }
        VSpace(40)
    }
}

/**
 * رسم الشهادة كصورة 1200×1600: خلفية زمردية عميقة، إطار ذهبي مزدوج بزوايا
 * معينية، ختم بلون الكأس. النص العربي يُشكَّل تلقائيًا عبر Paint.
 */
fun renderCertificate(
    name: String,
    days: Int,
    habitLabel: String,
    tier: Tier,
    isProgram: Boolean = false
): Bitmap {
    val w = 1200
    val h = 1600
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)

    // خلفية متدرجة
    val bg = Paint().apply {
        shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(0xFF0A2E27.toInt(), 0xFF0E3B4E.toInt(), 0xFF1D1B4B.toInt()),
            null, Shader.TileMode.CLAMP
        )
    }
    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

    // هالة خلف الختم
    val glow = Paint().apply {
        shader = RadialGradient(
            w / 2f, 1210f, 260f,
            intArrayOf(tier.dark.toArgb() and 0x60FFFFFF, 0x00000000),
            null, Shader.TileMode.CLAMP
        )
    }
    c.drawCircle(w / 2f, 1210f, 260f, glow)

    val gold = 0xFFD4AF37.toInt()
    val frame = Paint().apply {
        color = gold; style = Paint.Style.STROKE; strokeWidth = 6f; isAntiAlias = true
    }
    c.drawRect(RectF(50f, 50f, w - 50f, h - 50f), frame)
    frame.strokeWidth = 2f
    c.drawRect(RectF(70f, 70f, w - 70f, h - 70f), frame)
    // معينات الزوايا
    val diamond = Paint().apply { color = gold; isAntiAlias = true }
    listOf(50f to 50f, w - 50f to 50f, 50f to h - 50f, w - 50f to h - 50f).forEach { (x, y) ->
        val p = android.graphics.Path().apply {
            moveTo(x, y - 26f); lineTo(x + 26f, y); lineTo(x, y + 26f); lineTo(x - 26f, y); close()
        }
        c.drawPath(p, diamond)
    }

    fun text(
        str: String, y: Float, sizePx: Float, color: Int,
        bold: Boolean = false, alpha: Int = 255
    ) {
        val p = Paint().apply {
            this.color = color
            this.alpha = alpha
            textSize = sizePx
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            else Typeface.DEFAULT
        }
        c.drawText(str, w / 2f, y, p)
    }

    text("حِصن", 200f, 58f, gold, bold = true)
    text("شهادة إنجاز", 330f, 96f, 0xFFFFFFFF.toInt(), bold = true)

    // خط فاصل ذهبي
    val sep = Paint().apply { color = gold; strokeWidth = 3f }
    c.drawLine(w / 2f - 180f, 380f, w / 2f + 180f, 380f, sep)

    text("تُمنح هذه الشهادة إلى", 480f, 40f, 0xFFCFE8E0.toInt())
    text(name, 580f, 72f, 0xFFFFFFFF.toInt(), bold = true)

    val deed = if (isProgram) "لإتمامه مسار التسعين يومًا كاملًا"
    else "لإتمامه $days يومًا نظيفًا متتاليًا"
    text(deed, 700f, 52f, 0xFFFFE082.toInt(), bold = true)
    if (!isProgram && habitLabel.isNotBlank()) {
        text("في رحلة التعافي من: $habitLabel", 775f, 38f, 0xFFCFE8E0.toInt())
    }

    text("«ومن جاهد فإنما يجاهد لنفسه»", 900f, 40f, 0xFFB9E5D9.toInt())

    // التاريخ بالعربية
    val today = LocalDate.now()
    val months = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )
    text("${today.dayOfMonth} ${months[today.monthValue - 1]} ${today.year}", 985f, 36f, 0xFF9FB8B0.toInt())

    // الختم: دائرة بلون الكأس
    val sealOuter = Paint().apply {
        color = tier.dark.toArgb(); style = Paint.Style.STROKE; strokeWidth = 10f; isAntiAlias = true
    }
    val sealFill = Paint().apply {
        shader = LinearGradient(
            w / 2f - 150f, 1060f, w / 2f + 150f, 1360f,
            tier.light.toArgb(), tier.dark.toArgb(), Shader.TileMode.CLAMP
        )
        isAntiAlias = true
    }
    c.drawCircle(w / 2f, 1210f, 150f, sealFill)
    c.drawCircle(w / 2f, 1210f, 150f, sealOuter)
    val inner = Paint().apply {
        color = 0x66FFFFFF; style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true
    }
    c.drawCircle(w / 2f, 1210f, 128f, inner)

    text("🏆", 1250f, 110f, 0xFFFFFFFF.toInt())
    text("كأس ${tier.label}", 1330f, 40f, 0xFFFFFFFF.toInt(), bold = true)

    text("العدّاد الذي لا يعود إلى الصفر", h - 120f, 30f, 0xFF7FA89E.toInt())

    return bmp
}

/** حفظ الصورة في الكاش ومشاركتها عبر FileProvider. */
fun shareBitmap(context: Context, bmp: Bitmap) {
    val dir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(dir, "hisn-certificate.png")
    file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الشهادة"))
}
