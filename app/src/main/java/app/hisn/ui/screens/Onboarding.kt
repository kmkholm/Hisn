package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.domain.ContentMode
import app.hisn.domain.HabitKind
import app.hisn.ui.Eyebrow
import app.hisn.ui.ProgressBar
import app.hisn.ui.SectionCard
import app.hisn.ui.VSpace
import app.hisn.ui.theme.color
import app.hisn.ui.theme.icon
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun OnboardingScreen(
    onFinish: (List<Pair<HabitKind, Int>>, ContentMode, String) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val picked = remember { mutableStateListOf<HabitKind>() }
    val cleanDays = remember { mutableStateMapOf<HabitKind, Int>() }
    var mode by remember { mutableStateOf(ContentMode.BOTH) }
    var why by remember { mutableStateOf("") }

    val total = 5

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        ProgressBar((step + 1f) / total)
        VSpace(20)

        Box(Modifier.weight(1f)) {
            when (step) {
                0 -> WelcomeStep()
                1 -> PickHabitsStep(picked)
                2 -> CleanDaysStep(picked, cleanDays)
                3 -> ModeStep(mode) { mode = it }
                4 -> WhyStep(why) { why = it }
            }
        }

        VSpace(12)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(onClick = { step-- }) { Text("رجوع") }
            } else {
                Box {}
            }
            Button(
                onClick = {
                    if (step < total - 1) step++
                    else onFinish(
                        picked.map { it to (cleanDays[it] ?: 0) },
                        mode,
                        why
                    )
                },
                enabled = when (step) {
                    1 -> picked.isNotEmpty()
                    else -> true
                }
            ) {
                Text(if (step < total - 1) "التالي" else "ابدأ")
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Eyebrow("قبل أن نبدأ")
        Text("حِصن", style = MaterialTheme.typography.displaySmall)
        Text(
            "برنامج تعافٍ وتوعية مبني على مناهج علاجية لها سند في الأدبيات: " +
                "العلاج المعرفي السلوكي، والعلاج بالقبول والالتزام، واليقظة الذهنية، " +
                "والمقابلة التحفيزية.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SectionCard(tone = MaterialTheme.colorScheme.primaryContainer) {
            Text("ثلاث قواعد", style = MaterialTheme.typography.titleMedium)
            Text(
                "• لا تأنيب. الخجل يرفع احتمال الانتكاسة لا يخفضه.\n" +
                    "• عدّادك لا يعود للصفر. مجموع أيامك النظيفة رقم لا يُمحى.\n" +
                    "• كل شيء يبقى على جهازك. لا حساب، ولا خادم، ولا مزامنة.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                "هذا التطبيق ليس بديلًا عن مختص. إن كنت تمرّ باكتئاب مستمر أو أفكار " +
                    "إيذاء للنفس، فاطلب مساعدة مهنية فورًا.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PickHabitsStep(picked: MutableList<HabitKind>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Eyebrow("الخطوة ١")
        Text("ما الذي تريد متابعته؟", style = MaterialTheme.typography.headlineSmall)
        Text(
            "اختر واحدًا أو أكثر. الأدوات العلاجية نفسها تعمل مع كل هذه السلوكيات.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(HabitKind.entries.toList()) { kind ->
                val on = kind in picked
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (on) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (on) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { if (on) picked.remove(kind) else picked.add(kind) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(kind.emoji, style = MaterialTheme.typography.titleLarge)
                    Text(kind.label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CleanDaysStep(picked: List<HabitKind>, days: MutableMap<HabitKind, Int>) {
    var pickingFor by remember { mutableStateOf<HabitKind?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Eyebrow("الخطوة ٢")
        Text("منذ متى أنت نظيف؟", style = MaterialTheme.typography.headlineSmall)
        Text(
            "إن كنت قد بدأت قبل تثبيت التطبيق، سجّل ذلك الآن. لا تبدأ من الصفر بلا سبب — " +
                "ويمكنك تعديل العدّاد لاحقًا في أي وقت من الإعدادات.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(picked) { kind ->
                val kc = kind.color()
                val v = days[kind] ?: 0
                SectionCard(border = kc.copy(alpha = .4f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(34.dp).clip(RoundedCornerShape(999.dp))
                                .background(kc.copy(alpha = .16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(kind.icon(), null, tint = kc, modifier = Modifier.size(18.dp))
                        }
                        Text(kind.label, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 1, 3, 7, 30, 90).forEach { d ->
                            val on = v == d
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (on) kc else kc.copy(alpha = .12f))
                                    .clickable { days[kind] = d }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    if (d == 0) "اليوم" else "$d",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (on) Color.White else kc
                                )
                            }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { pickingFor = kind }) { Text("اختر تاريخًا محددًا") }
                        Text(
                            if (v == 0) "من اليوم" else "$v يومًا",
                            style = MaterialTheme.typography.labelMedium,
                            color = kc
                        )
                    }
                }
            }
        }
    }

    pickingFor?.let { kind ->
        val today = LocalDate.now()
        DayPickerDialog(
            initialEpochDay = today.minusDays((days[kind] ?: 0).toLong()).toEpochDay(),
            maxMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            onPick = { epochDay ->
                val picked = LocalDate.ofEpochDay(epochDay)
                days[kind] = java.time.temporal.ChronoUnit.DAYS
                    .between(picked, today).toInt().coerceAtLeast(0)
                pickingFor = null
            },
            onDismiss = { pickingFor = null }
        )
    }
}

@Composable
private fun ModeStep(mode: ContentMode, onPick: (ContentMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Eyebrow("الخطوة ٣")
        Text("نبرة المحتوى", style = MaterialTheme.typography.headlineSmall)
        Text(
            "المحرك العلاجي واحد في الحالات الثلاث. ما يتغيّر هو الإطار الذي يُقدَّم به.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        listOf(
            Triple(ContentMode.BOTH, "الاثنان معًا", "علم النفس مع الربط بالقيم الدينية. الوضع الافتراضي."),
            Triple(ContentMode.ISLAMIC, "إسلامي", "الإطار الديني في المقدمة، مع الأساس العلمي تحته."),
            Triple(ContentMode.SCIENTIFIC, "علمي محايد", "المنهج السلوكي وحده، بلا إطار ديني.")
        ).forEach { (m, title, desc) ->
            val on = m == mode
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (on) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        if (on) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onPick(m) }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                "لا خطاب تأنيب في أي وضع. هذا قرار تصميمي: الخجل من أقوى مشغّلات الانتكاسة.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WhyStep(why: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Eyebrow("الخطوة ٤")
        Text("لماذا تريد هذا؟", style = MaterialTheme.typography.headlineSmall)
        Text(
            "جملة واحدة بصيغة المتكلم، بلا تأنيب. ستظهر لك في أضعف لحظة — " +
                "والدافع المكتوب أقوى من الدافع المتذكَّر.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = why,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("أريد أن أستعيد تركيزي وصدقي مع نفسي.") }
        )
        Text(
            "يمكنك تغييرها في أي وقت من الإعدادات.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start
        )
    }
}
