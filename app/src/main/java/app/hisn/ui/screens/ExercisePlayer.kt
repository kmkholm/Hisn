package app.hisn.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.content.Exercise
import app.hisn.content.Step
import app.hisn.domain.ContentMode
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.ProgressBar
import app.hisn.ui.SectionCard
import app.hisn.ui.VSpace
import kotlinx.coroutines.delay

/**
 * مشغّل التمرين.
 *
 * التمرين تفاعلي لا نص يُقرأ: مؤقّتات صامتة، تنفّس موجّه بأطوار،
 * وقياس شدة قبل/بعد يعطي المستخدم دليلًا رقميًا على أن الأداة تعمل.
 */
@Composable
fun ExercisePlayerScreen(
    exercise: Exercise,
    mode: ContentMode,
    onFinish: (before: Int, after: Int) -> Unit,
    onExit: () -> Unit
) {
    var index by remember(exercise.id) { mutableIntStateOf(-1) }
    val answers = remember(exercise.id) { mutableStateMapOf<Int, String>() }
    var beforeLevel by remember(exercise.id) { mutableIntStateOf(-1) }
    var afterLevel by remember(exercise.id) { mutableIntStateOf(-1) }

    if (index < 0) {
        IntroCard(exercise, mode, onStart = { index = 0 }, onExit = onExit)
        return
    }

    if (index >= exercise.steps.size) {
        OutroCard(
            exercise = exercise,
            before = beforeLevel,
            after = afterLevel,
            onDone = {
                onFinish(
                    if (beforeLevel >= 0) beforeLevel else 0,
                    if (afterLevel >= 0) afterLevel else 0
                )
            }
        )
        return
    }

    val step = exercise.steps[index]

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                exercise.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onExit) { Text("إنهاء") }
        }
        VSpace(8)
        ProgressBar((index + 1f) / exercise.steps.size, height = 5)
        VSpace(24)

        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            when (step) {
                is Step.Say -> SayStep(step)
                is Step.Hold -> HoldStep(step) { index++ }
                is Step.Breath -> BreathStep(step) { index++ }
                is Step.Ask -> AskStep(
                    step,
                    answers[index] ?: ""
                ) { answers[index] = it }

                is Step.Scale -> ScaleStep(step) { v ->
                    if (step.isBefore) beforeLevel = v
                    if (step.isAfter) afterLevel = v
                }
            }
        }

        VSpace(16)
        val autoAdvancing = step is Step.Hold || step is Step.Breath
        if (!autoAdvancing) {
            Button(
                onClick = { index++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (index == exercise.steps.lastIndex) "أنهيت" else "التالي")
            }
        } else {
            TextButton(
                onClick = { index++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تخطّي")
            }
        }
    }
}

@Composable
private fun IntroCard(
    exercise: Exercise,
    mode: ContentMode,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
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
            Pill("${exercise.method.label} · ${exercise.minutes} دقائق")
            TextButton(onClick = onExit) { Text("رجوع") }
        }
        Text(exercise.title, style = MaterialTheme.typography.headlineLarge)
        Text(
            exercise.tagline,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionCard {
            Eyebrow("متى تستخدمه")
            Text(exercise.useWhen, style = MaterialTheme.typography.bodyLarge)
        }

        SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
            Eyebrow("لماذا يعمل · ${exercise.method.full}")
            Text(
                exercise.scienceNote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                exercise.method.evidence,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .8f)
            )
        }

        if (mode != ContentMode.SCIENTIFIC) {
            exercise.islamicNote?.let {
                SectionCard(tone = MaterialTheme.colorScheme.tertiaryContainer) {
                    Eyebrow("وقفة", MaterialTheme.colorScheme.onTertiaryContainer)
                    Text(it, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("ابدأ التمرين")
        }
        VSpace(40)
    }
}

@Composable
private fun OutroCard(exercise: Exercise, before: Int, after: Int, onDone: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VSpace(40)
        Text("انتهى", style = MaterialTheme.typography.headlineLarge)
        Text(
            exercise.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (before >= 0 && after >= 0) {
            SectionCard(tone = MaterialTheme.colorScheme.primaryContainer) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$before", style = MaterialTheme.typography.headlineLarge)
                        Text("قبل", style = MaterialTheme.typography.labelMedium)
                    }
                    Text("←", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$after",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("بعد", style = MaterialTheme.typography.labelMedium)
                    }
                }
                val drop = before - after
                Text(
                    when {
                        drop > 0 -> "انخفضت الشدة $drop نقاط. هذا دليل رقمي على أن الأداة تعمل معك — احتفظ به."
                        drop == 0 -> "لم تتغيّر الشدة هذه المرة. هذا وارد؛ الأدوات تحتاج تكرارًا لتُتقَن."
                        else -> "ارتفعت الشدة. لاحظ ذلك دون حكم — أحيانًا الملاحظة تكشف ما كان مخفيًا."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            "كل تمرين تنهيه يعلّم دماغك أن الرغبة تمرّ دون أن تطيعها.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Box(Modifier.weight(1f))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("تم") }
    }
}

// ─────────────── الخطوات ───────────────

@Composable
private fun SayStep(step: Step.Say) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            step.text,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        step.hint?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HoldStep(step: Step.Hold, onDone: () -> Unit) {
    var remaining by remember(step) { mutableIntStateOf(step.seconds) }
    LaunchedEffect(step) {
        remaining = step.seconds
        while (remaining > 0) {
            delay(1000)
            remaining--
        }
        onDone()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            step.text,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            "%d:%02d".format(remaining / 60, remaining % 60),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        ProgressBar(
            1f - (remaining.toFloat() / step.seconds.coerceAtLeast(1)),
            Modifier.fillMaxWidth(.6f),
            height = 5
        )
    }
}

@Composable
private fun BreathStep(step: Step.Breath, onDone: () -> Unit) {
    // الأطوار: 0 شهيق · 1 حبس · 2 زفير
    var cycle by remember(step) { mutableIntStateOf(0) }
    var phase by remember(step) { mutableIntStateOf(0) }
    var left by remember(step) { mutableIntStateOf(step.inhale) }

    LaunchedEffect(step) {
        cycle = 0; phase = 0; left = step.inhale
        while (cycle < step.cycles) {
            delay(1000)
            left--
            if (left <= 0) {
                when (phase) {
                    0 -> {
                        if (step.hold > 0) { phase = 1; left = step.hold }
                        else { phase = 2; left = step.exhale }
                    }
                    1 -> { phase = 2; left = step.exhale }
                    else -> { phase = 0; left = step.inhale; cycle++ }
                }
            }
        }
        onDone()
    }

    val target = when (phase) {
        0 -> 1f
        1 -> 1f
        else -> .55f
    }
    val duration = when (phase) {
        0 -> step.inhale * 1000
        1 -> 200
        else -> step.exhale * 1000
    }
    val scale by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = duration),
        label = "breath"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            when (phase) {
                0 -> "شهيق"
                1 -> "احبس"
                else -> "زفير"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Box(
            Modifier
                .fillMaxWidth(.6f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Text("$left", style = MaterialTheme.typography.displaySmall)
        }
        Text(
            step.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            "الدورة ${cycle + 1} من ${step.cycles}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AskStep(step: Step.Ask, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(step.prompt, style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            placeholder = { Text(step.placeholder) }
        )
        Text(
            "يبقى على جهازك. لا يُرسل إلى أي مكان.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScaleStep(step: Step.Scale, onValue: (Int) -> Unit) {
    var v by remember(step) { mutableStateOf(5f) }
    LaunchedEffect(step) { onValue(5) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            step.prompt,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            "${v.toInt()}",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Slider(
            value = v,
            onValueChange = { v = it; onValue(it.toInt()) },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("لا شيء", style = MaterialTheme.typography.labelMedium)
            Text("أقصى شدة", style = MaterialTheme.typography.labelMedium)
        }
    }
}
