package app.hisn.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** بطاقة قسم بحدود خفيفة — لا ظلال ثقيلة ولا ألوان صارخة. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    tone: Color = MaterialTheme.colorScheme.surface,
    border: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tone),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}

/** عنوان صغير فوق القسم. */
@Composable
fun Eyebrow(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

/** شارة صغيرة للمنهج أو الحالة. */
@Composable
fun Pill(
    text: String,
    bg: Color = MaterialTheme.colorScheme.primaryContainer,
    fg: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

/** خانة رقم — الرقم أولًا ثم التسمية، لأن الرقم هو ما يُقرأ بلمحة. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    emphasis: Boolean = false,
    hint: String? = null
) {
    val fg = if (emphasis) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = if (emphasis) MaterialTheme.typography.displaySmall
            else MaterialTheme.typography.headlineMedium,
            color = fg,
            textAlign = TextAlign.Center
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (hint != null) {
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** شريط تقدّم أفقي بسيط. */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, height: Int = 8) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

/** صف مفتاح/قيمة للرؤى. */
@Composable
fun StatRow(label: String, value: String, accent: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = if (accent) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun VSpace(dp: Int) = Spacer(Modifier.height(dp.dp))

@Composable
fun Dot(color: Color, size: Int = 8) =
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(999.dp)).background(color))

/** تحويل الرقم إلى نص عربي مقروء للأيام. */
fun dayWord(n: Int): String = when {
    n == 0 -> "لم يبدأ بعد"
    n == 1 -> "يوم واحد"
    n == 2 -> "يومان"
    n in 3..10 -> "$n أيام"
    else -> "$n يومًا"
}
