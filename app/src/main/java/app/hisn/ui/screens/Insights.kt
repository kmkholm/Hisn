package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.hisn.content.Guidance
import app.hisn.domain.Place
import app.hisn.domain.SlipKind
import app.hisn.domain.Trigger
import app.hisn.ui.Eyebrow
import app.hisn.ui.SectionCard
import app.hisn.ui.StatRow
import app.hisn.ui.StatTile
import app.hisn.ui.UiState
import app.hisn.ui.VSpace
import java.time.Duration
import java.time.Instant

/**
 * الرؤى.
 *
 * الغرض ليس عرض الأرقام بل تحويل الانتكاسات إلى معرفة قابلة للاستخدام:
 * أي ساعة، أي محفّز، أي مكان — وهي المدخلات نفسها التي يستعملها محرّك الإرشاد.
 */
@Composable
fun InsightsScreen(state: UiState) {
    val h = state.selected

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("رؤى", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "انتكاساتك ليست فشلًا مكررًا — هي بيانات عن نفسك لم تكن تملكها.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SectionCard {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatTile(
                        "${h?.totalCleanDays() ?: 0}",
                        "يومًا نظيفًا",
                        Modifier.weight(1f),
                        emphasis = true
                    )
                    StatTile("${state.urgesSurvived}", "موجة تجاوزتها", Modifier.weight(1f))
                    StatTile("${state.sessions.size}", "تمرينًا أنجزته", Modifier.weight(1f))
                }
            }
        }

        // ── تقرير هذا الأسبوع ──
        item {
            val weekAgo = Instant.now().minus(Duration.ofDays(7))
            val exWeek = state.sessions.count { it.at.isAfter(weekAgo) }
            val urgesWeek = state.rides.count { it.at.isAfter(weekAgo) && it.survived }
            val relapsesWeek = state.relapses.count {
                it.at.isAfter(weekAgo) && it.kind == SlipKind.RELAPSE
            }
            val lapsesWeek = state.relapses.count {
                it.at.isAfter(weekAgo) && it.kind == SlipKind.LAPSE
            }
            val daysWeek = minOf(h?.currentStreakDays() ?: 0, 7)
            SectionCard(border = MaterialTheme.colorScheme.primary.copy(alpha = .4f)) {
                Eyebrow("هذا الأسبوع")
                StatRow("أيام نظيفة", "$daysWeek من 7", accent = true)
                StatRow("تمارين أنجزتها", "$exWeek")
                StatRow("موجات تجاوزتها", "$urgesWeek")
                StatRow("زلات متدارَكة", "$lapsesWeek")
                StatRow("انتكاسات", "$relapsesWeek")
                Text(
                    when {
                        relapsesWeek == 0 && exWeek >= 3 ->
                            "أسبوع قوي: التزام بالأدوات وصفر انتكاسات. هذا هو النمط الذي يبني التعافي."
                        relapsesWeek == 0 ->
                            "أسبوع نظيف — لكن الأدوات شبه مهجورة. الأدوات تضعف بالإهمال لا بالوقت."
                        exWeek >= 3 ->
                            "رغم التعثر، بقيت ملتزمًا بالأدوات — وهذا أهم مؤشر للأسبوع القادم."
                        else ->
                            "أسبوع صعب. لا تحاول تعويضه دفعة واحدة: تمرين واحد اليوم يكفي للعودة."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── المزاج ──
        if (state.moods.isNotEmpty()) {
            item { MoodSection(state) }
        }

        // ── الساعة الأخطر ──
        if (state.relapses.size >= 3) {
            item {
                SectionCard(tone = MaterialTheme.colorScheme.tertiaryContainer) {
                    Eyebrow("نافذتك الأخطر", MaterialTheme.colorScheme.onTertiaryContainer)
                    Text(
                        state.riskiestHour?.let {
                            "معظم انزلاقك يقع قرب الساعة ${Guidance.fmtHour(it)}."
                        } ?: "لا يوجد نمط زمني واضح بعد.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "خطّط لهذه الساعة تحديدًا. الاستعداد قبل الموجة أسهل من مقاومتها أثناءها.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = .85f)
                    )
                }
            }
        }

        // ── توزيع الساعات ──
        if (state.relapses.isNotEmpty()) {
            item {
                SectionCard {
                    Eyebrow("التوزيع على مدار اليوم")
                    HourHistogram(state)
                }
            }
        }

        // ── المحفزات ──
        if (state.relapses.isNotEmpty()) {
            item {
                SectionCard {
                    Eyebrow("محفّزاتك")
                    val counts = state.relapses.groupingBy { it.trigger }.eachCount()
                        .toList().sortedByDescending { it.second }
                    val max = counts.firstOrNull()?.second ?: 1
                    counts.forEach { (t, n) ->
                        BarRow(t.label, n, max)
                    }
                }
            }
            item {
                SectionCard {
                    Eyebrow("أين يحدث")
                    val counts = state.relapses.groupingBy { it.place }.eachCount()
                        .toList().sortedByDescending { it.second }
                    val max = counts.firstOrNull()?.second ?: 1
                    counts.forEach { (p, n) -> BarRow(p.label, n, max) }
                }
            }
        }

        // ── أثر التمارين ──
        item {
            SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                Eyebrow("هل تعمل الأدوات معك؟")
                state.avgExerciseDrop?.let {
                    StatRow("متوسط انخفاض الشدة بعد التمرين", "−${"%.1f".format(it)}", accent = true)
                    Text(
                        if (it > 1.5)
                            "الأدوات تعمل معك بوضوح. هذا دليل رقمي لا شعور."
                        else
                            "الأثر ما زال محدودًا. جرّب تمرينًا من منهج مختلف — لا كل أداة تناسب كل شخص.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: Text(
                    "أنهِ بعض التمارين ليظهر هنا قياس أثرها عليك.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── السجل ──
        if (state.relapses.isNotEmpty()) {
            item { VSpace(4); Eyebrow("السجل") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.relapses.take(12).forEach { r ->
                        SectionCard(tone = MaterialTheme.colorScheme.surface) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    (if (r.kind == SlipKind.LAPSE) "🌊 زلة · " else "") + r.trigger.label,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    ago(r.at),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${r.place.label} · الساعة ${Guidance.fmtHour(r.hourOfDay())} · الشدة ${r.intensity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            r.note?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        "لا سجلّ بعد. حين تسجّل انتكاسة، ستتحوّل هنا إلى نمط يمكن التدخل فيه.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** مدرّج تكراري لساعات اليوم — 24 عمودًا، الأطول هو الأخطر. */
@Composable
private fun HourHistogram(state: UiState) {
    val counts = IntArray(24)
    state.relapses.forEach { counts[it.hourOfDay()]++ }
    val max = (counts.maxOrNull() ?: 1).coerceAtLeast(1)

    Row(
        Modifier
            .fillMaxWidth()
            .height(90.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (h in 0..23) {
            val f = counts[h].toFloat() / max
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(if (f == 0f) 0.04f else f.coerceAtLeast(0.08f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (counts[h] == max && max > 0)
                            MaterialTheme.colorScheme.tertiary
                        else if (counts[h] > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("١٢ص", "٦ص", "١٢ظ", "٦م", "١١م").forEach {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BarRow(label: String, count: Int, max: Int) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(count.toFloat() / max)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun ago(at: Instant): String {
    val d = Duration.between(at, Instant.now())
    return when {
        d.toHours() < 1 -> "قبل قليل"
        d.toHours() < 24 -> "قبل ${d.toHours()} ساعة"
        d.toDays() == 1L -> "أمس"
        d.toDays() < 30 -> "قبل ${d.toDays()} يومًا"
        else -> "قبل ${d.toDays() / 30} شهرًا"
    }
}
