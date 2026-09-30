package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.content.Exercises
import app.hisn.domain.Place
import app.hisn.domain.SlipKind
import app.hisn.domain.Trigger
import app.hisn.ui.Eyebrow
import app.hisn.ui.SectionCard
import app.hisn.ui.UiState
import app.hisn.ui.VSpace
import kotlinx.coroutines.delay

/**
 * لحظة الرغبة.
 *
 * لا حجب ولا قفل — الفكرة أن يبقى المستخدم مع الموجة حتى تنكسر،
 * مع تذكيره بسببه الخاص وخطته التي كتبها وهو هادئ.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UrgeScreen(
    state: UiState,
    onOpenExercise: (String) -> Unit,
    onLogRide: (peak: Int, ending: Int, seconds: Int, survived: Boolean) -> Unit,
    onExit: () -> Unit,
    onOpenBreath: () -> Unit = {}
) {
    var phase by remember { mutableIntStateOf(0) } // 0 قياس · 1 ركوب · 2 نتيجة
    var peak by remember { mutableStateOf(7f) }
    var ending by remember { mutableStateOf(5f) }
    var seconds by remember { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("لحظة الرغبة")
            TextButton(onClick = onExit) { Text("خروج") }
        }

        when (phase) {
            0 -> {
                Text("أنت هنا وهذا يكفي الآن.", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "لن نطلب منك مقاومة. سنبقى مع الموجة حتى تنكسر — وهي تنكسر.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onOpenBreath, modifier = Modifier.fillMaxWidth()) {
                    Text("🫁 تنفّس معي أولًا — دقيقة واحدة")
                }

                if (state.whyStatement.isNotBlank()) {
                    SectionCard(tone = MaterialTheme.colorScheme.primaryContainer) {
                        Eyebrow("لماذا بدأت", MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(state.whyStatement, style = MaterialTheme.typography.titleLarge)
                    }
                }
                if (state.copingPlan.isNotBlank()) {
                    SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                        Eyebrow("خطتك التي كتبتها وأنت هادئ")
                        Text(state.copingPlan, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                SectionCard {
                    Text("ما شدة الرغبة الآن؟", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${peak.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        value = peak,
                        onValueChange = { peak = it },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                }

                Button(
                    onClick = { phase = 1 },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("ابدأ ركوب الموجة") }

                Eyebrow("أو افتح تمرينًا مباشرة")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Exercises.emergency.forEach { ex ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(999.dp)
                                )
                                .clickable { onOpenExercise(ex.id) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(ex.title, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            1 -> {
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1000)
                        seconds++
                    }
                }
                Text(
                    "%d:%02d".format(seconds / 60, seconds % 60),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    ridingText(seconds),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "لا شيء مطلوب منك سوى ألا تفعل. أنت الشاطئ، وهي تمرّ.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                VSpace(20)
                Button(onClick = { phase = 2 }, modifier = Modifier.fillMaxWidth()) {
                    Text("انتهت الموجة")
                }
                OutlinedButton(
                    onClick = { onOpenExercise("urge_surf") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("افتح تمرين ركوب الموجة الموجّه") }
            }

            else -> {
                Text("كيف هي الآن؟", style = MaterialTheme.typography.headlineMedium)
                SectionCard {
                    Text(
                        "${ending.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        value = ending,
                        onValueChange = { ending = it },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                    Text(
                        "من ${peak.toInt()} إلى ${ending.toInt()} خلال ${seconds / 60} دقيقة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        onLogRide(peak.toInt(), ending.toInt(), seconds, true)
                        onExit()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("تجاوزتها") }
                OutlinedButton(
                    onClick = {
                        onLogRide(peak.toInt(), ending.toInt(), seconds, false)
                        onExit()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("لم أتجاوزها") }
                Text(
                    "في الحالتين: التسجيل نفسه مفيد. كل موجة مركوبة تعلّم دماغك أن الرغبة تمرّ دون أن تطيعها.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        VSpace(40)
    }
}

private fun ridingText(s: Int): String = when {
    s < 30 -> "لاحظ أين تشعر بها في جسدك."
    s < 90 -> "سمِّها: «هذه موجة رغبة. أنا ألاحظها.»"
    s < 180 -> "هل تتغيّر شدتها؟ راقب فقط."
    s < 300 -> "أنت في المنتصف. الذروة عادة تنكسر قريبًا."
    s < 600 -> "أنت تتجاوز أصعب جزء الآن."
    else -> "مرّت أكثر من عشر دقائق. هذا وحده إنجاز."
}

/**
 * تسجيل الانتكاسة — ثلاثة أسئلة فقط.
 * لا تقييم أخلاقي، ولا رسالة تأنيب، ولا عدّاد يعود للصفر.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelapseScreen(
    onSave: (Trigger, Place, Int, String?, SlipKind) -> Unit,
    onOpenExercise: (String) -> Unit,
    onExit: () -> Unit
) {
    var kind by remember { mutableStateOf<SlipKind?>(null) }
    var saved by remember { mutableStateOf(false) }
    var trigger by remember { mutableStateOf(Trigger.UNKNOWN) }
    var place by remember { mutableStateOf(Place.OTHER) }
    var intensity by remember { mutableStateOf(5f) }
    var note by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("تسجيل")
            TextButton(onClick = onExit) { Text("إغلاق") }
        }

        // ── الخطوة صفر: زلة أم انتكاسة؟ التفريق ممارسة سريرية لا تساهل ──
        if (kind == null && !saved) {
            Text("ماذا حدث بالضبط؟", style = MaterialTheme.typography.headlineMedium)
            Text(
                "الصدق هنا مع نفسك أنت — لا أحد يرى هذا غيرك.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SectionCard(
                modifier = Modifier.clickable { kind = SlipKind.LAPSE },
                border = MaterialTheme.colorScheme.primary.copy(alpha = .5f)
            ) {
                Text("🌊 زلة قصيرة تداركتها", style = MaterialTheme.typography.titleLarge)
                Text(
                    "بدأتَ ثم أوقفت نفسك فورًا ولم تُكمل. " +
                        "تُسجَّل كإشارة إنذار — وسلسلتك تستمر كما هي.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SectionCard(
                modifier = Modifier.clickable { kind = SlipKind.RELAPSE }
            ) {
                Text("💔 انتكاسة", style = MaterialTheme.typography.titleLarge)
                Text(
                    "حدثت كاملة. تُسجَّل ويُعاد عدّ السلسلة الحالية — " +
                        "ومجموعك ورقمك القياسي محفوظان لا يُمسّان.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    "لماذا نفرّق؟ لأن معاملة الزلة القصيرة كسقوط كامل هي بالضبط " +
                        "ما يحوّلها إلى واحد. لكن كن صادقًا: الزلات المتكررة كل يوم " +
                        "اسمها الحقيقي انتكاسة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VSpace(40)
            return@Column
        }

        if (saved) {
            val wasLapse = kind == SlipKind.LAPSE
            Text(
                if (wasLapse) "سُجِّلت الزلة." else "سُجِّلت.",
                style = MaterialTheme.typography.headlineLarge
            )
            SectionCard(tone = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    if (wasLapse)
                        "سلسلتك مستمرة — لم يتغير أي عدّاد. تداركك الفوري هو عين التعافي."
                    else
                        "مجموع أيامك النظيفة ورقمك القياسي لم يتغيّرا. ما تغيّر هو السلسلة الحالية فقط.",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "هذه المعلومة التي سجّلتها الآن ستظهر في الرؤى كنمط — وهي ما يجعل المرة القادمة أسهل.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    "أهم شيء الآن ألا تتحوّل الزلّة إلى انهيار. الخجل يرفع احتمال التكرار لا يخفضه.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { onOpenExercise("self_compassion") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("تمرين: بعد الزلّة") }
            OutlinedButton(
                onClick = { onOpenExercise("chain") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("تمرين: تحليل السلسلة (لاحقًا بعد أن تهدأ)") }
            TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("رجوع") }
            VSpace(40)
            return@Column
        }

        Text("ماذا حدث؟", style = MaterialTheme.typography.headlineMedium)
        Text(
            "ثلاثة أسئلة فقط. هذه بيانات لتحليل النمط — لا محاسبة.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionCard {
            Eyebrow("ما الذي سبقها؟")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Trigger.entries.forEach { t ->
                    ChoiceChip(t.label, t == trigger) { trigger = t }
                }
            }
        }

        SectionCard {
            Eyebrow("أين كنت؟")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Place.entries.forEach { p ->
                    ChoiceChip(p.label, p == place) { place = p }
                }
            }
        }

        SectionCard {
            Eyebrow("كم كانت الرغبة قوية؟")
            Text(
                "${intensity.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0f..10f,
                steps = 9
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            label = { Text("ملاحظة (اختياري)") }
        )

        Button(
            onClick = {
                onSave(
                    trigger, place, intensity.toInt(),
                    note.takeIf { it.isNotBlank() },
                    kind ?: SlipKind.RELAPSE
                )
                saved = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) { Text("حفظ") }
        VSpace(40)
    }
}

@Composable
private fun ChoiceChip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (on) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (on) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
