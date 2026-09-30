package app.hisn.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.theme.Accent

/**
 * مدرّب التنفّس المرئي.
 *
 * الرغبة الشديدة حالة استثارة جسدية قبل أن تكون فكرة، والزفير الطويل هو أسرع
 * مفتاح يدوي للجهاز العصبي اللاودّي. الدائرة تكبر وتصغر مع النفس، فيتبعها الجسد
 * بلا عدّ ذهني — وهذا مهم لأن الذهن وقت الرغبة لا يحسن العدّ.
 */
enum class BreathPattern(
    val title: String,
    val why: String,
    /** المراحل: (التسمية، الثواني، هل تكبر الدائرة؟ 1 شهيق · 0 ثبات · -1 زفير) */
    val phases: List<Triple<String, Float, Int>>
) {
    SIGH(
        "التنهيدة الفسيولوجية",
        "شهيقان متتاليان ثم زفير طويل — أسرع طريقة موثّقة لخفض الاستثارة في أقل من دقيقة.",
        listOf(Triple("شهيق", 2.5f, 1), Triple("شهيق قصير", 1f, 1), Triple("زفير طويل", 6f, -1))
    ),
    FOUR_SEVEN_EIGHT(
        "4-7-8",
        "الحبس الطويل والزفير الأطول يبطئان القلب. مناسب قبل النوم وعند التسارع.",
        listOf(Triple("شهيق", 4f, 1), Triple("احبس", 7f, 0), Triple("زفير", 8f, -1))
    ),
    BOX(
        "المربّع 4-4-4-4",
        "نمط تستعمله فرق النخبة تحت الضغط. توازن بسيط يعيد التركيز.",
        listOf(Triple("شهيق", 4f, 1), Triple("احبس", 4f, 0), Triple("زفير", 4f, -1), Triple("احبس", 4f, 0))
    ),
    RESONANCE(
        "الرنين 5.5",
        "خمس ثوانٍ ونصف لكل اتجاه — الإيقاع الذي يرفع تباين نبض القلب أكثر من غيره.",
        listOf(Triple("شهيق", 5.5f, 1), Triple("زفير", 5.5f, -1))
    );

    val cycleSeconds: Float get() = phases.sumOf { it.second.toDouble() }.toFloat()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreathScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var pattern by remember { mutableStateOf(BreathPattern.SIGH) }
    var running by remember { mutableStateOf(false) }
    var phaseIx by remember { mutableIntStateOf(0) }
    var phaseT by remember { mutableFloatStateOf(0f) }   // 0..1 داخل المرحلة
    var scale by remember { mutableFloatStateOf(.45f) }   // حجم الدائرة 0..1
    var cycles by remember { mutableIntStateOf(0) }
    var rewarded by remember { mutableStateOf(false) }
    val target = 4

    LaunchedEffect(running, pattern) {
        if (!running) return@LaunchedEffect
        phaseIx = 0; phaseT = 0f; cycles = 0
        var last = withFrameNanos { it }
        var acc = 0f
        while (running) {
            withFrameNanos { now ->
                val dt = ((now - last) / 1_000_000_000f).coerceAtMost(.05f)
                last = now
                val (_, secs, dir) = pattern.phases[phaseIx]
                acc += dt
                phaseT = (acc / secs).coerceIn(0f, 1f)
                scale = when (dir) {
                    1 -> .45f + .55f * ease(phaseT)
                    -1 -> 1f - .55f * ease(phaseT)
                    else -> scale
                }
                if (acc >= secs) {
                    acc = 0f
                    phaseIx = (phaseIx + 1) % pattern.phases.size
                    if (phaseIx == 0) {
                        cycles++
                        if (cycles >= target && !rewarded) {
                            rewarded = true
                            onDone("breath_${pattern.name.lowercase()}")
                        }
                    }
                }
            }
        }
    }

    val phase = pattern.phases[phaseIx]
    val secsLeft = kotlin.math.ceil((phase.second * (1f - phaseT)).toDouble()).toInt()

    StaticBackdrop(tint = Accent.Cyan) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("مدرّب التنفّس", Accent.Cyan)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill("دورة $cycles / $target")
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }

            if (!running) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BreathPattern.entries.forEach { p ->
                        val on = p == pattern
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (on) Accent.Cyan else Accent.Cyan.copy(alpha = .12f))
                                .border(1.dp, Accent.Cyan.copy(alpha = if (on) 1f else .4f), RoundedCornerShape(999.dp))
                                .clickable { pattern = p }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                p.title, style = MaterialTheme.typography.labelMedium,
                                color = if (on) Color.White else Accent.Cyan
                            )
                        }
                    }
                }
                Text(
                    pattern.why,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── الدائرة ──
            Box(
                Modifier
                    .fillMaxWidth(.85f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2, size.height / 2)
                    val maxR = size.minDimension / 2
                    drawCircle(Accent.Cyan.copy(alpha = .08f), maxR, c)
                    drawCircle(Accent.Cyan.copy(alpha = .10f), maxR * .45f, c)
                    val r = maxR * (if (running) scale else .6f)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Accent.Cyan.copy(alpha = .95f), Accent.Teal.copy(alpha = .75f), Accent.Indigo.copy(alpha = .35f)),
                            center = c, radius = r
                        ),
                        r, c
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (running) phase.first else "جاهز؟",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    if (running) Text(
                        "$secsLeft",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }
            }

            if (!running) {
                Button(onClick = { running = true; rewarded = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("ابدأ · ${pattern.title}")
                }
            } else {
                OutlinedButton(onClick = { running = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("إيقاف")
                }
            }

            if (cycles >= target) {
                SectionCard(tone = Accent.Emerald.copy(alpha = .12f), border = Accent.Emerald.copy(alpha = .5f)) {
                    Text("أربع دورات كاملة ✓", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "لاحظ جسدك الآن مقارنةً بما قبل دقيقتين. هذا الفرق هو ما تستطيع صنعه بنفسك، في أي مكان، بلا شيء.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "سُجّلت في نقاطك.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun ease(t: Float): Float {
    // منحنى ناعم يبدأ وينتهي ببطء
    val x = t.coerceIn(0f, 1f)
    return x * x * (3 - 2 * x)
}
