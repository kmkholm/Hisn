package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.content.Achievements
import app.hisn.content.Exercises
import app.hisn.content.Program
import app.hisn.content.Stories
import app.hisn.content.StoryKind
import app.hisn.domain.ContentMode
import app.hisn.ui.Backdrop
import app.hisn.ui.CountUpText
import app.hisn.ui.LiveCountdown
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.ProgressBar
import app.hisn.ui.ProgressRing
import app.hisn.ui.SectionCard
import app.hisn.ui.TrophyCup
import app.hisn.ui.UiState
import app.hisn.ui.dayWord
import app.hisn.ui.theme.Accent
import app.hisn.ui.theme.color
import app.hisn.ui.theme.heroGradient
import app.hisn.ui.theme.icon
import java.time.LocalTime

@Composable
fun HomeScreen(
    state: UiState,
    onSelectHabit: (Long) -> Unit,
    onOpenExercise: (String) -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenUrge: () -> Unit,
    onLogRelapse: () -> Unit,
    onOpenDay: (Int) -> Unit,
    onOpenDhikr: () -> Unit,
    onOpenTrophies: () -> Unit,
    onOpenSettings: () -> Unit,
    onPrepare: () -> Unit,
    onOpenBreath: () -> Unit,
    onOpenJournal: () -> Unit,
    onLogMood: (Int, Int, String?) -> Unit,
    onSetCost: (Long, app.hisn.domain.HabitCost, String) -> Unit
) {
    val dark = isSystemInDarkTheme()
    val habit = state.selected
    val streak = habit?.currentStreakDays() ?: 0
    val nextCup = Achievements.nextStreakCup(streak)
    val level = Achievements.levelFor(state.xp)

    Backdrop {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── الترويسة: تحية + رتبة + إعدادات ──
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            greeting(),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Pill(
                                "⭐ ${level.title} · ${state.xp} نقطة",
                                bg = MaterialTheme.colorScheme.primaryContainer,
                                fg = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Outlined.Settings, "الإعدادات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── مبدّل العادات ──
            if (state.habits.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.habits) { h ->
                            val on = h.id == habit?.id
                            val hc = h.kind.color()
                            Row(
                                Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (on) hc else hc.copy(alpha = .12f))
                                    .border(
                                        1.dp,
                                        hc.copy(alpha = if (on) 1f else .35f),
                                        RoundedCornerShape(999.dp)
                                    )
                                    .clickable { onSelectHabit(h.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    h.kind.icon(), null,
                                    tint = if (on) Color.White else hc,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    h.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (on) Color.White else hc
                                )
                            }
                        }
                    }
                }
            }

            // ── البطاقة البطلة: حلقة تقدّم نحو الكأس القادمة ──
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(heroGradient(dark))
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        habit?.let {
                            Icon(
                                it.kind.icon(), null,
                                tint = Color.White.copy(alpha = .9f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            habit?.displayName ?: "ابدأ متابعة عادة",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = .92f)
                        )
                    }

                    ProgressRing(
                        progress = nextCup?.let { streak.toFloat() / it.threshold } ?: 1f,
                        modifier = Modifier
                            .fillMaxWidth(.62f)
                            .aspectRatio(1f),
                        strokeWidth = 12.dp,
                        colors = nextCup?.let { listOf(it.tier.light, Color.White) }
                            ?: listOf(Color.White, Color.White)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CountUpText(
                                habit?.totalCleanDays() ?: 0,
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White
                            )
                            Text(
                                "يومًا نظيفًا",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = .95f)
                            )
                            Text(
                                "لا ينقص أبدًا",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = .7f)
                            )
                        }
                    }

                    if (nextCup != null && habit != null) {
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = .16f))
                                .clickable { onOpenTrophies() }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrophyCup(nextCup.tier, 30.dp)
                            Column {
                                Text(
                                    "كأس «${nextCup.title}» — ${nextCup.tier.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = .92f)
                                )
                                LiveCountdown(
                                    target = habit.streakStart.plus(
                                        java.time.Duration.ofDays(nextCup.threshold.toLong())
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = .14f))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HeroStat("$streak", "السلسلة الحالية")
                        HeroStat("${habit?.bestStreakDays() ?: 0}", "الرقم القياسي")
                        HeroStat("${state.urgesSurvived}", "موجة تجاوزتها")
                    }

                    // كم بقي حتى يُحتسب يوم جديد في العدّاد
                    if (habit != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⏳ يكتمل يوم جديد:",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = .85f)
                            )
                            LiveCountdown(
                                target = habit.streakStart.plus(
                                    java.time.Duration.ofDays(streak + 1L)
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ── شبكة الإجراءات السريعة ──
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickTile(
                            "أشعر برغبة الآن", Icons.Filled.Bolt,
                            listOf(Accent.Coral, Accent.Magenta),
                            Modifier.weight(1f), onOpenUrge
                        )
                        QuickTile(
                            "سبحة ذكر", Icons.Outlined.RadioButtonChecked,
                            listOf(Accent.Emerald, Accent.Teal),
                            Modifier.weight(1f), onOpenDhikr
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickTile(
                            "تمرين اليوم", Icons.Outlined.SelfImprovement,
                            listOf(Accent.Violet, Accent.Indigo),
                            Modifier.weight(1f)
                        ) { onOpenExercise(Exercises.daily(state.programDay).id) }
                        QuickTile(
                            "تسجيل انتكاسة", Icons.Outlined.EditNote,
                            listOf(Color(0xFF5C6B66), Color(0xFF3D4C47)),
                            Modifier.weight(1f), onLogRelapse
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickTile(
                            "تنفّس معي", Icons.Outlined.Air,
                            listOf(Accent.Cyan, Accent.Blue),
                            Modifier.weight(1f), onOpenBreath
                        )
                        QuickTile(
                            "يومياتي", Icons.Outlined.Mood,
                            listOf(Accent.Amber, Accent.Coral),
                            Modifier.weight(1f), onOpenJournal
                        )
                    }
                }
            }

            // ── مزاج اليوم ──
            item { MoodCheckInCard(state, onLogMood) }

            // ── نشرة الليلة ──
            item { ForecastCard(state, onPrepare) }

            // ── ما وفّرته ──
            item { SavingsCard(state, onSetCost) }

            // ── إرشاد اللحظة ──
            state.tip?.let { tip ->
                item {
                    val c = MaterialTheme.colorScheme.primary
                    SectionCard(border = c.copy(alpha = .55f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Lightbulb, null, tint = c,
                                modifier = Modifier.size(18.dp)
                            )
                            Eyebrow("إرشاد الآن", c)
                        }
                        Text(tip.text, style = MaterialTheme.typography.titleMedium)
                        tip.why?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        tip.exerciseId?.let { id ->
                            Exercises.byId(id)?.let { ex ->
                                OutlinedButton(onClick = { onOpenExercise(id) }) {
                                    Text("افتح: ${ex.title}")
                                }
                            }
                        }
                    }
                }
            }

            // ── لماذا بدأت ──
            if (state.whyStatement.isNotBlank()) {
                item {
                    SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                        Eyebrow("لماذا بدأت")
                        Text(state.whyStatement, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // ── يوم المسار ──
            item {
                val d = Program.day(state.programDay)
                val pc = d.phase.color(dark)
                val done = state.completed(d.day)
                SectionCard(
                    modifier = Modifier.clickable { onOpenDay(d.day) },
                    border = pc.copy(alpha = .45f)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Eyebrow("مسار الـ90 يومًا · ${d.phase.label}", pc)
                        Pill(
                            if (done) "تم ✓" else "اليوم ${d.day}",
                            bg = if (done) pc else pc.copy(alpha = .16f),
                            fg = if (done) Color.White else pc
                        )
                    }
                    Text(d.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        d.science,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    ProgressBar(state.completions.size / 90f, height = 6)
                }
            }

            // ── قصة اليوم ──
            item {
                val s = Stories.daily(state.programDay)
                val c = s.method?.color(dark) ?: Accent.Cyan
                SectionCard(
                    modifier = Modifier.clickable { onOpenStory(s.id) },
                    border = c.copy(alpha = .45f)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(42.dp).clip(CircleShape).background(c.copy(alpha = .18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (s.kind == StoryKind.REAL) Icons.Outlined.Verified
                                else Icons.Outlined.AutoStories,
                                null, tint = c, modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Eyebrow(s.kind.label, c)
                            Text(s.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                s.hook,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // ── التزام ──
            item {
                SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "التزامك بالجلسات",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(dayWord(state.sessionStreak), style = MaterialTheme.typography.titleMedium)
                    }
                    state.avgExerciseDrop?.let {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "متوسط انخفاض الشدة بعد التمرين",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "−${"%.1f".format(it)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (state.mode != ContentMode.SCIENTIFIC) {
                item {
                    Program.day(state.programDay).islamic?.let { note ->
                        SectionCard(
                            tone = MaterialTheme.colorScheme.tertiaryContainer,
                            border = MaterialTheme.colorScheme.tertiary
                        ) {
                            Eyebrow("وقفة", MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(note, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "صباح الخير ☀️"
    in 12..16 -> "نهارك طيب 🌤"
    in 17..21 -> "مساء الخير 🌆"
    else -> "سهرة هادئة 🌙"
}

@Composable
private fun HeroStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = .8f)
        )
    }
}

@Composable
private fun QuickTile(
    label: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
