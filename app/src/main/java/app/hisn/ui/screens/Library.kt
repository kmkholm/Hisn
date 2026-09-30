package app.hisn.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import app.hisn.content.Exercise
import app.hisn.content.Exercises
import app.hisn.content.Method
import app.hisn.content.Phase
import app.hisn.content.Program
import app.hisn.domain.ContentMode
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.ProgressBar
import app.hisn.ui.SectionCard
import app.hisn.ui.UiState
import app.hisn.ui.VSpace
import app.hisn.ui.theme.color
import app.hisn.ui.theme.icon

/** مكتبة الأدوات: تمارين، قصص، وأنشطة بديلة. */
@Composable
fun ExercisesScreen(
    onOpen: (String) -> Unit,
    onOpenStory: (String) -> Unit,
    activityCount: Int,
    onLogActivity: (String) -> Unit,
    onOpenGame: (GameKind) -> Unit,
    onOpenShelf: () -> Unit,
    onOpenBreath: () -> Unit = {}
) {
    var tab by remember { mutableStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)) {
            Text(
                when (tab) {
                    0 -> "التمارين الذهنية"
                    1 -> "قصص تُروى"
                    2 -> "أنشطة بديلة"
                    else -> "ألعاب ذهنية"
                },
                style = MaterialTheme.typography.headlineMedium
            )
            VSpace(10)
            Segmented(
                options = listOf("تمارين", "قصص", "أنشطة", "ألعاب"),
                selected = tab,
                onSelect = { tab = it }
            )
            VSpace(10)
        }
        when (tab) {
            0 -> ExercisesList(onOpen, onOpenBreath)
            1 -> StoriesList(onOpenStory, onOpenShelf)
            2 -> ActivitiesList(activityCount, onLogActivity)
            else -> GamesList(onOpenGame)
        }
    }
}

/** مبدّل مقطعي — بديل أخف من التبويبات لخيارين. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            val bg by animateColorAsState(
                if (on) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                label = "seg"
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .clickable { onSelect(i) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExercisesList(onOpen: (String) -> Unit, onOpenBreath: () -> Unit = {}) {
    val dark = isSystemInDarkTheme()
    var filter by remember { mutableStateOf<Method?>(null) }
    val list = Exercises.all.filter { filter == null || it.method == filter }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── مدرّب التنفّس المرئي ──
        item {
            val c = app.hisn.ui.theme.Accent.Cyan
            SectionCard(
                modifier = Modifier.clickable(onClick = onOpenBreath),
                tone = c.copy(alpha = .10f),
                border = c.copy(alpha = .5f)
            ) {
                Eyebrow("🫁 مدرّب التنفّس المرئي", c)
                Text("دائرة تكبر وتصغر مع نفسك", style = MaterialTheme.typography.titleLarge)
                Text(
                    "أربعة أنماط موثّقة: التنهيدة الفسيولوجية، 4-7-8، المربّع، والرنين. " +
                        "أسرع مفتاح يدوي لتهدئة الجسد قبل الذهن.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    MethodChip(
                        "الكل",
                        filter == null,
                        MaterialTheme.colorScheme.primary
                    ) { filter = null }
                }
                items(Method.entries.toList()) { m ->
                    MethodChip(m.label, filter == m, m.color(dark)) {
                        filter = if (filter == m) null else m
                    }
                }
            }
        }

        filter?.let { m ->
            item {
                val c = m.color(dark)
                SectionCard(tone = c.copy(alpha = .10f), border = c.copy(alpha = .45f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(m.icon(), null, tint = c, modifier = Modifier.size(20.dp))
                        Eyebrow(m.full, c)
                    }
                    Text(m.evidence, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        val emergency = list.filter { it.isEmergency }
        if (emergency.isNotEmpty()) {
            item { Eyebrow("لحظة الرغبة") }
            items(emergency) { ex -> ExerciseRow(ex, dark, onOpen) }
        }
        val building = list.filterNot { it.isEmergency }
        if (building.isNotEmpty()) {
            item { VSpace(4); Eyebrow("تمارين البناء") }
            items(building) { ex -> ExerciseRow(ex, dark, onOpen) }
        }
    }
}

@Composable
private fun MethodChip(
    label: String,
    on: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (on) color else color.copy(alpha = .10f))
            .border(1.dp, color.copy(alpha = if (on) 1f else .35f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (on) androidx.compose.ui.graphics.Color.White else color
        )
    }
}

@Composable
private fun ExerciseRow(ex: Exercise, dark: Boolean, onOpen: (String) -> Unit) {
    val c = ex.method.color(dark)
    SectionCard(modifier = Modifier.clickable { onOpen(ex.id) }) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(c.copy(alpha = .28f), c.copy(alpha = .10f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(ex.method.icon(), null, tint = c, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(ex.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    ex.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Pill("${ex.minutes} د", bg = c.copy(alpha = .16f), fg = c)
        }
        Text(ex.useWhen, style = MaterialTheme.typography.labelSmall, color = c)
    }
}

/** مسار الـ90 يومًا. */
@Composable
fun ProgramScreen(
    state: UiState,
    onOpenDay: (Int) -> Unit,
    onOpenTimeline: () -> Unit,
    onChallengeDone: (String) -> Unit,
    onAddCapsule: (String, java.time.Instant) -> Unit,
    onOpenCapsule: (Long) -> Unit,
    onDeleteCapsule: (Long) -> Unit
) {
    val dark = isSystemInDarkTheme()
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("مسار الـ90 يومًا", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "فهم، ثم أدوات، ثم إعادة بناء، ثم ترسيخ. الامتناع وحده ينهار حين ينتهي " +
                        "الحماس؛ ما يصمد هو أن يتغيّر ما حول السلوك.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ProgressBar(state.completions.size / 90f, height = 7)
                Text(
                    "أنجزت ${state.completions.size} من 90",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // تحدّي الأسبوع
        item { WeeklyChallengeCard(state, onChallengeDone) }

        // كبسولة الزمن
        item { CapsulesSection(state, onAddCapsule, onOpenCapsule, onDeleteCapsule) }

        // مدخل الخط الزمني
        item {
            val c = app.hisn.ui.theme.Accent.Cyan
            SectionCard(
                modifier = Modifier.clickable { onOpenTimeline() },
                tone = c.copy(alpha = .12f),
                border = c.copy(alpha = .45f)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(c.copy(alpha = .2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Timeline, null, tint = c, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "ماذا يحدث خلال 90 يومًا",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "ما يحدث في جسدك وعقلك فعلًا — مع فصل المثبت عن المتداول.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Phase.entries.forEach { phase ->
            val pc = phase.color(dark)
            item {
                VSpace(6)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(10.dp).clip(CircleShape).background(pc)
                    )
                    Column {
                        Eyebrow("${phase.label} · الأيام ${phase.range.first}–${phase.range.last}", pc)
                        Text(
                            phase.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(Program.phaseDays(phase)) { d ->
                val done = state.completed(d.day)
                val current = d.day == state.programDay
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                current -> pc.copy(alpha = .16f)
                                done -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                        .border(
                            1.dp,
                            if (current) pc else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onOpenDay(d.day) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (done) pc.copy(alpha = .22f) else androidx.compose.ui.graphics.Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (done) "✓" else "${d.day}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (done) pc else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(d.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            d.task,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/** تفاصيل يوم من المسار. */
@Composable
fun DayDetailScreen(
    day: Int,
    state: UiState,
    onComplete: (String?) -> Unit,
    onOpenExercise: (String) -> Unit,
    onBack: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val d = Program.day(day)
    val pc = d.phase.color(dark)
    var reflection by remember(day) { mutableStateOf("") }
    val done = state.completed(day)

    app.hisn.ui.StaticBackdrop(tint = pc) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Pill(
                    "${d.phase.label} · اليوم $day",
                    bg = pc.copy(alpha = .18f),
                    fg = pc
                )
                TextButton(onClick = onBack) { Text("رجوع") }
            }

            Text(d.title, style = MaterialTheme.typography.headlineLarge)

            SectionCard {
                Eyebrow("ما يقوله العلم", pc)
                Text(d.science, style = MaterialTheme.typography.bodyLarge)
            }

            if (state.mode != ContentMode.SCIENTIFIC) {
                d.islamic?.let {
                    SectionCard(
                        tone = MaterialTheme.colorScheme.tertiaryContainer,
                        border = MaterialTheme.colorScheme.tertiary
                    ) {
                        Eyebrow("وقفة", MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(it, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            SectionCard(tone = pc.copy(alpha = .14f), border = pc.copy(alpha = .5f)) {
                Eyebrow("مهمة اليوم", pc)
                Text(d.task, style = MaterialTheme.typography.titleMedium)
            }

            d.exerciseId?.let { id ->
                Exercises.byId(id)?.let { ex ->
                    OutlinedButton(
                        onClick = { onOpenExercise(id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تمرين اليوم: ${ex.title}")
                    }
                }
            }

            if (!done) {
                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("ملاحظة (اختياري)") },
                    placeholder = { Text("ما الذي لاحظته اليوم؟") }
                )
                Button(
                    onClick = { onComplete(reflection.takeIf { it.isNotBlank() }) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("أنهيت اليوم")
                }
            } else {
                SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("✓ أنجزت هذا اليوم", style = MaterialTheme.typography.titleMedium)
                    state.completions.firstOrNull { it.day == day }?.reflection?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            VSpace(50)
        }
    }
}

// ═════════════════ الأنشطة البديلة ═════════════════

/**
 * مكتبة الأنشطة — التنشيط السلوكي عمليًا.
 * البديل يجب أن يكون أسهل من السلوك القديم في اللحظة، لذلك كل نشاط
 * محدد المدة وقابل للبدء خلال دقيقة.
 */
@Composable
fun ActivitiesList(activityCount: Int, onLog: (String) -> Unit) {
    var cat by remember { mutableStateOf<app.hisn.content.ActCat?>(null) }
    var maxMin by remember { mutableStateOf<Int?>(null) }
    var suggested by remember { mutableStateOf<app.hisn.content.Activity?>(null) }
    var justLogged by remember { mutableStateOf<String?>(null) }

    val list = app.hisn.content.ActivitiesLib.byCat(cat)
        .filter { a -> maxMin?.let { a.minutes <= it } ?: true }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    "السلوك لا يُحذف — يُستبدل. الفراغ الذي يتركه هو أخطر ما في " +
                        "التعافي، وهذه المكتبة تملؤه بأفعال تبدأ خلال دقيقة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "أنشطة أنجزتها",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$activityCount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Button(
                onClick = { suggested = app.hisn.content.ActivitiesLib.suggest(maxMin) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("🎲  اقترح لي نشاطًا الآن") }
        }

        suggested?.let { a ->
            item {
                val c = MaterialTheme.colorScheme.primary
                SectionCard(tone = c.copy(alpha = .1f), border = c.copy(alpha = .5f)) {
                    Eyebrow("اقتراح", c)
                    Text("${a.cat.emoji}  ${a.title}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        a.benefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("${a.minutes} دقيقة")
                        Pill(a.cat.label)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            onLog(a.id); justLogged = a.id; suggested = null
                        }) { Text("أنجزته ✓") }
                        OutlinedButton(onClick = {
                            suggested = app.hisn.content.ActivitiesLib.suggest(maxMin)
                        }) { Text("اقتراح آخر") }
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterPill("الكل", cat == null) { cat = null } }
                items(app.hisn.content.ActCat.entries.toList()) { c ->
                    FilterPill("${c.emoji} ${c.label}", cat == c) {
                        cat = if (cat == c) null else c
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("أي مدة", maxMin == null) { maxMin = null }
                FilterPill("≤ ٥ دقائق", maxMin == 5) { maxMin = if (maxMin == 5) null else 5 }
                FilterPill("≤ ١٥ دقيقة", maxMin == 15) { maxMin = if (maxMin == 15) null else 15 }
            }
        }

        items(list) { a ->
            SectionCard {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(a.cat.emoji, style = MaterialTheme.typography.headlineSmall)
                    Column(Modifier.weight(1f)) {
                        Text(a.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            a.benefit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Pill("${a.minutes} د")
                }
                if (justLogged == a.id) {
                    Text(
                        "سُجّل ✓ — أُضيف إلى نقاطك وكؤوسك.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    OutlinedButton(onClick = { onLog(a.id); justLogged = a.id }) {
                        Text("أنجزته ✓")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (on) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                1.dp,
                if (on) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (on) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
