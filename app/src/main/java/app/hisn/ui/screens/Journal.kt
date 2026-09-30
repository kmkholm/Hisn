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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.domain.MoodEntry
import app.hisn.domain.SlipKind
import app.hisn.ui.Eyebrow
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.UiState
import app.hisn.ui.theme.Accent
import java.time.LocalDate
import java.time.ZoneId

/*
 * يوميات المزاج.
 *
 * تسجيل يومي في ثوانٍ: وجه واحد من خمسة، وطاقة من خمسة، وسطر اختياري.
 * القيمة العلاجية ليست في التسجيل نفسه، بل في ما يظهر بعد أسبوعين:
 * هل تسبق الزلاتِ أيامٌ منخفضة المزاج؟ هذا هو المؤشر المبكّر الذي لا يراه
 * المرء بنفسه — والتطبيق يحسبه في «رؤى».
 */

private fun moodColor(m: Int): Color = when (m) {
    1 -> Accent.Coral; 2 -> Accent.Amber; 3 -> Accent.Cyan; 4 -> Accent.Teal; else -> Accent.Emerald
}

/** بطاقة التسجيل اليومي — تظهر في الرئيسية. */
@Composable
fun MoodCheckInCard(
    state: UiState,
    onLog: (mood: Int, energy: Int, note: String?) -> Unit,
    compact: Boolean = true
) {
    val today = state.todayMood
    var editing by remember(today?.id) { mutableStateOf(today == null) }
    var mood by remember(today?.id) { mutableIntStateOf(today?.mood ?: 0) }
    var energy by remember(today?.id) { mutableIntStateOf(today?.energy ?: 3) }
    var note by remember(today?.id) { mutableStateOf(today?.note ?: "") }

    SectionCard(border = Accent.Teal.copy(alpha = .45f)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("مزاجك اليوم", Accent.Teal)
            if (today != null && !editing) {
                TextButton(onClick = { editing = true }) { Text("تعديل") }
            }
        }

        if (today != null && !editing) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(today.emoji, style = MaterialTheme.typography.displaySmall)
                Column {
                    Text(
                        "${MoodEntry.moodLabel(today.mood)} · الطاقة ${today.energy}/5",
                        style = MaterialTheme.typography.titleMedium
                    )
                    today.note?.let {
                        Text(
                            it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            return@SectionCard
        }

        Text(
            "لمسة واحدة. بعد أسبوعين سترى في «رؤى» ما إذا كانت أيامك المنخفضة تسبق الزلات.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            (1..5).forEach { m ->
                val on = m == mood
                val c = moodColor(m)
                Box(
                    Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (on) c.copy(alpha = .28f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(if (on) 2.dp else 0.dp, if (on) c else Color.Transparent, CircleShape)
                        .clickable { mood = m },
                    contentAlignment = Alignment.Center
                ) {
                    Text(MoodEntry.moodEmoji(m), style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        if (mood > 0) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الطاقة", style = MaterialTheme.typography.labelMedium)
                (1..5).forEach { e ->
                    val on = e <= energy
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (on) Accent.Amber else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { energy = e },
                        contentAlignment = Alignment.Center
                    ) { Text("⚡", style = MaterialTheme.typography.labelSmall) }
                }
            }
            if (!compact || note.isNotEmpty() || today != null) {
                OutlinedTextField(
                    value = note, onValueChange = { note = it.take(200) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("سطر واحد — ما الذي أثّر في يومك؟ (اختياري)") }
                )
            } else {
                TextButton(onClick = { note = " " }) { Text("＋ أضف ملاحظة") }
            }
            Button(onClick = {
                onLog(mood, energy, note.trim().ifBlank { null })
                editing = false
            }) { Text("حفظ") }
        }
    }
}

/** قسم المزاج في «رؤى»: 14 يومًا + الارتباط بالزلات. */
@Composable
fun MoodSection(state: UiState) {
    if (state.moods.isEmpty()) return
    val today = LocalDate.now().toEpochDay()
    val byDay = state.moods.associateBy { it.epochDay }

    SectionCard {
        Eyebrow("مزاجك في آخر 14 يومًا", Accent.Teal)
        Row(
            Modifier.fillMaxWidth().height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            for (d in (today - 13)..today) {
                val e = byDay[d]
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(e?.let { it.mood / 5f } ?: .06f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(e?.let { moodColor(it.mood) } ?: MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("قبل أسبوعين", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("اليوم", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        val avgAll = state.moods.map { it.mood }.average()
        val avgEnergy = state.moods.map { it.energy }.average()
        Text(
            "متوسط مزاجك ${"%.1f".format(avgAll)}/5 · الطاقة ${"%.1f".format(avgEnergy)}/5 عبر ${state.moods.size} يومًا مسجَّلًا.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── الارتباط: مزاج اليوم السابق للزلة ──
        val zone = ZoneId.systemDefault()
        val beforeSlips = state.relapses.mapNotNull { r ->
            val day = r.at.atZone(zone).toLocalDate().toEpochDay()
            byDay[day] ?: byDay[day - 1]
        }
        if (beforeSlips.size >= 2) {
            val avgBefore = beforeSlips.map { it.mood }.average()
            val diff = avgAll - avgBefore
            Text(
                when {
                    diff >= .8 -> "🔎 نمط واضح: مزاجك قبل الزلات (${"%.1f".format(avgBefore)}) أدنى بوضوح من معدّلك. " +
                        "اليوم المنخفض عندك إنذار مبكّر — افتح أداة قبل أن تشتدّ الرغبة لا بعدها."
                    diff >= .3 -> "مزاجك قبل الزلات (${"%.1f".format(avgBefore)}) أدنى قليلًا من معدّلك. " +
                        "راقب هذا مع مزيد من التسجيل."
                    else -> "لا يبدو أن المزاج المنخفض هو ما يسبق زلاتك — ابحث في «محفّزاتك» عن النمط الأقوى."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (state.moods.size < 7) {
            Text(
                "سجّل يوميًا لأسبوع على الأقل ليظهر هنا ارتباط المزاج بالزلات.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** شاشة اليوميات الكاملة. */
@Composable
fun JournalScreen(
    state: UiState,
    onLog: (Int, Int, String?) -> Unit,
    onBack: () -> Unit
) {
    StaticBackdrop(tint = Accent.Teal) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 60.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("يومياتي", style = MaterialTheme.typography.headlineMedium)
                    TextButton(onClick = onBack) { Text("رجوع") }
                }
            }
            item { MoodCheckInCard(state, onLog, compact = false) }
            item { MoodSection(state) }

            if (state.moods.isNotEmpty()) {
                item { Eyebrow("السجل") }
                items(state.moods.size) { i ->
                    val e = state.moods[i]
                    val d = LocalDate.ofEpochDay(e.epochDay)
                    val slipThatDay = state.relapses.any {
                        it.at.atZone(ZoneId.systemDefault()).toLocalDate() == d
                    }
                    SectionCard(tone = MaterialTheme.colorScheme.surface) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(e.emoji, style = MaterialTheme.typography.headlineMedium)
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${d.dayOfMonth}/${d.monthValue}/${d.year} · ${MoodEntry.moodLabel(e.mood)} · طاقة ${e.energy}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                e.note?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (slipThatDay) Text(
                                    "🌊 سُجّلت زلة أو انتكاسة في هذا اليوم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Accent.Coral
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "لا تسجيلات بعد. ابدأ بلمسة واحدة أعلاه.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private val UiState.todayMood: MoodEntry?
    get() {
        val t = LocalDate.now().toEpochDay()
        return moods.firstOrNull { it.epochDay == t }
    }

@Suppress("unused")
private fun relapseKindLabel(k: SlipKind) = k.label
