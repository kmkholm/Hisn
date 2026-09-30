package app.hisn.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.content.Challenges
import app.hisn.domain.Capsule
import app.hisn.domain.SlipKind
import app.hisn.ui.Eyebrow
import app.hisn.ui.LiveCountdown
import app.hisn.ui.Pill
import app.hisn.ui.SectionCard
import app.hisn.ui.UiState
import app.hisn.ui.theme.Accent
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

// ═════════════════ ١) نشرة الليلة — توقّع الخطر ═════════════════

private data class Forecast(val level: Int, val emoji: String, val title: String, val lines: List<String>)

/**
 * توقّع خطر كنشرة الطقس، محسوب من أنماط المستخدم الفعلية لا من قوالب:
 * ساعته الأخطر، يوم الأسبوع الذي تتكرر فيه انزلاقاته، زلة قريبة، وانقطاعه عن الأدوات.
 */
private fun forecast(state: UiState): Forecast {
    var score = 0
    val lines = mutableListOf<String>()
    val now = Instant.now().atZone(ZoneId.systemDefault())

    state.riskiestHour?.let { h ->
        val hoursUntil = (h - now.hour + 24) % 24
        if (hoursUntil in 0..6) {
            score += 35
            lines += "نافذتك الأخطر (${app.hisn.content.Guidance.fmtHour(h)}) خلال الساعات القادمة"
        }
    }
    val today: DayOfWeek = now.dayOfWeek
    val sameWeekday = state.relapses.count { it.at.atZone(ZoneId.systemDefault()).dayOfWeek == today }
    if (state.relapses.size >= 3 && sameWeekday * 3 >= state.relapses.size) {
        score += 25
        lines += "هذا اليوم من الأسبوع يتكرر في سجلّك"
    }
    val recentLapse = state.relapses.firstOrNull {
        it.kind == SlipKind.LAPSE && Duration.between(it.at, Instant.now()).toHours() < 48
    }
    if (recentLapse != null) { score += 20; lines += "زلة خلال آخر ٤٨ ساعة — الشهية مفتوحة للتكرار" }
    if (state.daysSinceLastExercise >= 3) { score += 15; lines += "٣ أيام بلا تمرين — الأدوات باردة" }
    if (now.hour >= 22 || now.hour <= 3) { score += 15; lines += "الساعة متأخرة والإرهاق يضعف الكبح" }
    if (state.sessionStreak >= 5) { score -= 10 }
    if (state.urgesSurvived >= 3) { score -= 10 }

    return when {
        score >= 50 -> Forecast(2, "🌩", "خطر مرتفع الليلة", lines)
        score >= 25 -> Forecast(1, "⛅", "خطر متوسط", lines)
        else -> Forecast(0, "☀️", "أجواء هادئة", lines.ifEmpty { listOf("لا مؤشرات خطر في أنماطك الآن — استثمر الهدوء في تمرين") })
    }
}

@Composable
fun ForecastCard(state: UiState, onPrepare: () -> Unit) {
    val f = remember(state.relapses.size, state.sessions.size, state.rides.size) { forecast(state) }
    val c = when (f.level) { 2 -> Accent.Coral; 1 -> Accent.Amber; else -> Accent.Emerald }
    SectionCard(tone = c.copy(alpha = .10f), border = c.copy(alpha = .5f)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("نشرة الليلة", c)
            Text(f.emoji, style = MaterialTheme.typography.headlineSmall)
        }
        Text(f.title, style = MaterialTheme.typography.titleLarge)
        f.lines.forEach {
            Text("• $it", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (f.level >= 1) {
            OutlinedButton(onClick = onPrepare) { Text("جهّز خطة «إذا… فسـ…» الآن") }
        }
    }
}

// ═════════════════ ٢) جرّة الانتصارات ═════════════════

/**
 * جرّة زجاجية تمتلئ بحصى ملونة — حصاة لكل انتصار. لا تُفرَّغ أبدًا،
 * وهي الصورة البصرية لقاعدة التطبيق: التقدم يتراكم ولا يُصفَّر.
 */
@Composable
fun WinsJar(state: UiState) {
    val urges = state.urgesSurvived
    val ex = state.sessions.size
    val acts = state.activityCount
    val prog = state.completions.size
    val total = urges + ex + acts + prog
    val shown = total.coerceAtMost(220)

    SectionCard(border = Accent.Cyan.copy(alpha = .45f)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(
                Modifier
                    .size(120.dp)
                    .aspectRatio(.8f)
            ) {
                val w = size.width; val h = size.height
                // جسم الجرّة
                val jar = Path().apply {
                    moveTo(w * .28f, h * .1f)
                    lineTo(w * .72f, h * .1f)
                    lineTo(w * .72f, h * .2f)
                    quadraticBezierTo(w * .95f, h * .3f, w * .92f, h * .55f)
                    lineTo(w * .9f, h * .92f)
                    quadraticBezierTo(w * .9f, h, w * .8f, h)
                    lineTo(w * .2f, h)
                    quadraticBezierTo(w * .1f, h, w * .1f, h * .92f)
                    lineTo(w * .08f, h * .55f)
                    quadraticBezierTo(w * .05f, h * .3f, w * .28f, h * .2f)
                    close()
                }
                drawPath(jar, Color.White.copy(alpha = .35f))
                // الحصى — مواضع ثابتة بذرة واحدة حتى لا "تقفز" عند إعادة الرسم
                val rnd = Random(42)
                val palette = listOf(Accent.Teal, Accent.Amber, Accent.Violet, Accent.Coral, Accent.Lime, Accent.Cyan)
                val cols = 9
                for (i in 0 until shown) {
                    val row = i / cols
                    val col = i % cols
                    val jitter = (rnd.nextFloat() - .5f) * .02f
                    val cx = w * (.16f + .68f * (col + .5f) / cols) + jitter * w
                    val cy = h * (.97f - row * .033f)
                    if (cy < h * .22f) break
                    drawCircle(palette[i % palette.size], radius = w * .036f, center = Offset(cx, cy))
                }
                drawPath(jar, Accent.Cyan.copy(alpha = .8f), style = Stroke(width = 5f))
                // الغطاء
                drawRoundRect(
                    color = Accent.Sand,
                    topLeft = Offset(w * .24f, h * .04f),
                    size = Size(w * .52f, h * .08f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                )
            }
            Column(Modifier.weight(1f)) {
                Eyebrow("جرّة الانتصارات", Accent.Cyan)
                Text("$total حصاة", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "موجات مركوبة $urges · تمارين $ex · أنشطة $acts · أيام مسار $prog",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "كل انتصار حصاة تسقط هنا — والجرّة لا تُفرَّغ أبدًا، مهما حدث.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═════════════════ ٣) تحدّي الأسبوع ═════════════════

@Composable
fun WeeklyChallengeCard(state: UiState, onDoneToday: (String) -> Unit) {
    val day = state.programDay
    val week = ((day - 1) / 7 + 1).coerceIn(1, 13)
    val dayIx = (day - 1) % 7
    val ch = Challenges.forWeek(week)
    val doneToday = Challenges.dayId(week, dayIx) in state.activityIds
    val doneCount = (0 until 7).count { Challenges.dayId(week, it) in state.activityIds }

    SectionCard(border = Accent.Amber.copy(alpha = .5f)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("تحدّي الأسبوع $week", Accent.Amber)
            Pill("$doneCount / 7", bg = Accent.Amber.copy(alpha = .16f), fg = Accent.Amber)
        }
        Text("${ch.emoji}  ${ch.title}", style = MaterialTheme.typography.titleLarge)
        Text(ch.why, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until 7) {
                val done = Challenges.dayId(week, i) in state.activityIds
                val isToday = i == dayIx
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done -> Accent.Amber
                                isToday -> Accent.Amber.copy(alpha = .2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            if (isToday) 2.dp else 0.dp,
                            if (isToday) Accent.Amber else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (done) "✓" else "${i + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Text("مهمة اليوم: ${ch.dailyTask}", style = MaterialTheme.typography.titleMedium)
        if (doneToday) {
            Text("✓ أنجزت مهمة اليوم — سُجّلت في نقاطك",
                style = MaterialTheme.typography.labelMedium, color = Accent.Amber)
        } else {
            Button(onClick = { onDoneToday(Challenges.dayId(week, dayIx)) }) { Text("أنجزتها اليوم ✓") }
        }
        if (doneCount == 7) {
            Text("🏅 أسبوع مكتمل! سبعة من سبعة.", style = MaterialTheme.typography.titleMedium, color = Accent.Amber)
        }
    }
}

// ═════════════════ ٤) كبسولة الزمن ═════════════════

@Composable
fun CapsulesSection(
    state: UiState,
    onAdd: (String, Instant) -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    var composing by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf<Capsule?>(null) }

    SectionCard(border = Accent.Violet.copy(alpha = .5f)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("كبسولة الزمن", Accent.Violet)
            Text("⏳✉️", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "اكتب رسالة لنفسك المستقبلية وتُختم حتى موعدها. من يشعر بقربه من ذاته " +
                "المستقبلية يضبط اندفاعه أكثر — هذا مقيس، لا شعر.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        state.capsules.forEach { cap ->
            val unlockable = cap.isUnlockable()
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent.Violet.copy(alpha = if (cap.opened) .06f else .12f))
                    .clickable {
                        if (cap.opened) reading = cap
                        else if (unlockable) { onOpen(cap.id); reading = cap }
                    }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (cap.opened) "📖" else if (unlockable) "🔓" else "🔒",
                    style = MaterialTheme.typography.headlineSmall
                )
                Column(Modifier.weight(1f)) {
                    if (cap.opened) {
                        Text(cap.body.take(60) + if (cap.body.length > 60) "…" else "",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("فُتحت — اضغط للقراءة", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (unlockable) {
                        Text("حان موعدها! اضغط لفتح الرسالة", style = MaterialTheme.typography.titleMedium, color = Accent.Violet)
                    } else {
                        // النص مموّه حتى موعده — ليكون الفتح لحظة حقيقية
                        Text(cap.body.take(50), style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.blur(6.dp))
                        LiveCountdown(cap.unlockAt, style = MaterialTheme.typography.labelMedium, color = Accent.Violet)
                    }
                }
                TextButton(onClick = { onDelete(cap.id) }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            }
        }

        OutlinedButton(onClick = { composing = true }) { Text("✍️ اكتب رسالة لنفسك المستقبلية") }
    }

    if (composing) {
        var body by remember { mutableStateOf("") }
        var days by remember { mutableIntStateOf(30) }
        AlertDialog(
            onDismissRequest = { composing = false },
            title = { Text("رسالة إلى نفسي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ماذا تريد أن تقول لنفسك حين تصل؟ سيُختم النص ولن تراه حتى موعده.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = body, onValueChange = { body = it },
                        minLines = 5, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("إن كنت تقرأ هذا فقد وصلت. أتذكر كم كان صعبًا يوم كتبتها…") }
                    )
                    Eyebrow("تُفتح بعد")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 30, 60, 90).forEach { d ->
                            val on = days == d
                            Box(
                                Modifier.clip(RoundedCornerShape(999.dp))
                                    .background(if (on) Accent.Violet else Accent.Violet.copy(alpha = .12f))
                                    .clickable { days = d }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text("$d يومًا", style = MaterialTheme.typography.labelMedium,
                                    color = if (on) Color.White else Accent.Violet)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (body.isNotBlank()) onAdd(body.trim(), Instant.now().plus(Duration.ofDays(days.toLong())))
                    composing = false
                }) { Text("ختم الرسالة 🔒") }
            },
            dismissButton = { TextButton(onClick = { composing = false }) { Text("إلغاء") } }
        )
    }

    reading?.let { cap ->
        val ago = Duration.between(cap.createdAt, Instant.now()).toDays()
        AlertDialog(
            onDismissRequest = { reading = null },
            title = { Text("رسالة من نفسك قبل $ago يومًا") },
            text = { Text(cap.body, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = { TextButton(onClick = { reading = null }) { Text("شكرًا لي 💚") } }
        )
    }
}
