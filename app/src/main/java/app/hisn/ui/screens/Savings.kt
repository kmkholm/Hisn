package app.hisn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.hisn.domain.HabitCost
import app.hisn.domain.HabitKind
import app.hisn.ui.Eyebrow
import app.hisn.ui.SectionCard
import app.hisn.ui.StatTile
import app.hisn.ui.UiState
import app.hisn.ui.theme.Accent

/**
 * حاسبة ما وفّرته.
 *
 * المكافأة البعيدة (صحة، صفاء) لا تنافس المكافأة الفورية للعادة. تحويل
 * الامتناع إلى رقم ملموس يتراكم كل يوم — مال ووقت — يعطي الدماغ مكافأة
 * قريبة يراها. وهي تُحسب من العدّاد الذي لا ينقص، فلا تنقص هي أيضًا.
 */
@Composable
fun SavingsCard(
    state: UiState,
    onSetCost: (habitId: Long, cost: HabitCost, currency: String) -> Unit
) {
    val habit = state.selected ?: return
    val cost = state.costs[habit.id] ?: HabitCost()
    var editing by remember { mutableStateOf(false) }
    val days = habit.totalCleanDays()

    val money = days * cost.costPerDay
    val hours = days * cost.minutesPerDay / 60f
    val yearMoney = 365 * cost.costPerDay
    val yearDays = 365 * cost.minutesPerDay / 60f / 24f

    SectionCard(border = Accent.Amber.copy(alpha = .5f)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow("💰 ما وفّرته", Accent.Amber)
            TextButton(onClick = { editing = true }) { Text(if (cost.isSet) "تعديل" else "احسب") }
        }
        if (!cost.isSet) {
            Text(
                defaultPitch(habit.kind),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = { editing = true }) { Text("كم كانت تكلّفك يوميًا؟") }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                if (cost.costPerDay > 0f) StatTile(
                    fmt(money), state.currency, Modifier.weight(1f), emphasis = true
                )
                if (cost.minutesPerDay > 0) StatTile(
                    fmt(hours), "ساعة استرددتها", Modifier.weight(1f), emphasis = cost.costPerDay <= 0f
                )
            }
            Text(
                buildString {
                    append("خلال سنة كاملة: ")
                    if (cost.costPerDay > 0f) append("${fmt(yearMoney)} ${state.currency}")
                    if (cost.costPerDay > 0f && cost.minutesPerDay > 0) append(" و")
                    if (cost.minutesPerDay > 0) append("${fmt(yearDays)} يومًا كاملًا من عمرك")
                    append(".")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (editing) {
        CostDialog(
            initial = cost,
            currency = state.currency,
            kind = habit.kind,
            onDismiss = { editing = false },
            onSave = { c, cur -> onSetCost(habit.id, c, cur); editing = false }
        )
    }
}

private fun fmt(v: Float): String =
    if (v >= 100f) "%,.0f".format(java.util.Locale.US, v) else if (v == v.toInt().toFloat()) "${v.toInt()}" else "%.1f".format(java.util.Locale.US, v)

private fun defaultPitch(kind: HabitKind) = when (kind) {
    HabitKind.SMOKING, HabitKind.VAPE ->
        "علبة في اليوم تساوي راتبًا كاملًا في السنة. أدخل تكلفتك اليومية لترى الرقم يكبر مع عدّادك."
    HabitKind.GAMBLING, HabitKind.ALCOHOL, HabitKind.DRUGS ->
        "أدخل متوسط ما كنت تنفقه يوميًا — الرقم المتراكم هنا أقوى من أي محاضرة."
    HabitKind.GAMING, HabitKind.SOCIAL, HabitKind.PORN, HabitKind.MASTURBATION, HabitKind.PROCRASTINATION ->
        "الثمن هنا وقت. أدخل الدقائق التي كانت تذهب يوميًا لترى كم ساعة من عمرك استرددت."
    else ->
        "أدخل ما كانت العادة تكلّفك يوميًا: مالًا، أو دقائق، أو الاثنين."
}

@Composable
private fun CostDialog(
    initial: HabitCost,
    currency: String,
    kind: HabitKind,
    onDismiss: () -> Unit,
    onSave: (HabitCost, String) -> Unit
) {
    var money by remember { mutableStateOf(if (initial.costPerDay > 0f) fmt(initial.costPerDay) else "") }
    var minutes by remember { mutableStateOf(if (initial.minutesPerDay > 0) "${initial.minutesPerDay}" else "") }
    var cur by remember { mutableStateOf(currency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تكلفة ${kind.label} يوميًا") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = money,
                        onValueChange = { money = it.filter { c -> c.isDigit() || c == '.' }.take(8) },
                        label = { Text("المال في اليوم") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.4f), singleLine = true
                    )
                    OutlinedTextField(
                        value = cur, onValueChange = { cur = it.take(8) },
                        label = { Text("العملة") },
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                }
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("الدقائق في اليوم") },
                    supportingText = { Text("الوقت الذي كان يذهب للعادة في يوم عادي") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Text(
                    "يُحسب من مجموع أيامك النظيفة — الرقم الذي لا ينقص.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    HabitCost(money.toFloatOrNull() ?: 0f, minutes.toIntOrNull() ?: 0),
                    cur.ifBlank { "ريال" }
                )
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
