package app.hisn.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.hisn.content.Tier
import app.hisn.ui.theme.Accent
import kotlin.math.sin
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * كأس مرسومة بالمسارات — لا صورة ولا إيموجي.
 * الرسم داخل إطار 100×100 ويُحجَّم بحجم المكوّن. المقفلة تُرسم رمادية باهتة.
 */
@Composable
fun TrophyCup(tier: Tier, size: Dp, locked: Boolean = false, modifier: Modifier = Modifier) {
    val light = if (locked) Color(0xFFB9C2BE) else tier.light
    val dark = if (locked) Color(0xFF7C8783) else tier.dark
    val alpha = if (locked) .45f else 1f

    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 100f
        scale(s, s, pivot = Offset.Zero) {
            val grad = Brush.verticalGradient(listOf(light, dark), startY = 10f, endY = 90f)
            val stroke = Stroke(width = 5f, cap = StrokeCap.Round)

            // المقبضان
            drawArc(
                color = dark.copy(alpha = alpha),
                startAngle = 90f, sweepAngle = 200f, useCenter = false,
                topLeft = Offset(2f, 20f), size = Size(26f, 30f), style = stroke
            )
            drawArc(
                color = dark.copy(alpha = alpha),
                startAngle = 250f, sweepAngle = 200f, useCenter = false,
                topLeft = Offset(72f, 20f), size = Size(26f, 30f), style = stroke
            )

            // جسم الكأس
            val bowl = Path().apply {
                moveTo(20f, 16f)
                lineTo(80f, 16f)
                cubicTo(80f, 44f, 68f, 58f, 50f, 58f)
                cubicTo(32f, 58f, 20f, 44f, 20f, 16f)
                close()
            }
            drawPath(bowl, grad, alpha = alpha)
            // حافة عليا
            drawRoundRect(
                brush = grad,
                topLeft = Offset(16f, 12f),
                size = Size(68f, 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
                alpha = alpha
            )
            // لمعة
            drawOval(
                color = Color.White.copy(alpha = .35f * alpha),
                topLeft = Offset(28f, 20f),
                size = Size(12f, 22f)
            )
            // نجمة صغيرة على الجسم
            val star = Path().apply {
                val cx = 50f; val cy = 36f; val r = 8f
                moveTo(cx, cy - r)
                for (i in 1..4) {
                    val outer = Math.toRadians((i * 72 - 90).toDouble())
                    val inner = Math.toRadians((i * 72 - 126).toDouble())
                    lineTo(cx + (r * .42f) * Math.cos(inner).toFloat(), cy + (r * .42f) * Math.sin(inner).toFloat())
                    lineTo(cx + r * Math.cos(outer).toFloat(), cy + r * Math.sin(outer).toFloat())
                }
                close()
            }
            drawPath(star, Color.White.copy(alpha = .85f * alpha))

            // العنق والقاعدة
            drawRoundRect(
                brush = grad,
                topLeft = Offset(44f, 58f), size = Size(12f, 14f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f), alpha = alpha
            )
            drawRoundRect(
                brush = grad,
                topLeft = Offset(36f, 72f), size = Size(28f, 7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f), alpha = alpha
            )
            drawRoundRect(
                brush = grad,
                topLeft = Offset(28f, 79f), size = Size(44f, 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f), alpha = alpha
            )
        }
    }
}

/** حلقة تقدّم دائرية بتدرّج، والمحتوى في المنتصف. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = Color.White.copy(alpha = .18f),
    colors: List<Color> = listOf(Color(0xFFFFE082), Color(0xFF46E0B0)),
    content: @Composable () -> Unit = {}
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val inset = sw / 2
            val arcSize = Size(size.width - sw, size.height - sw)
            drawArc(
                color = trackColor,
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(sw, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(colors + colors.first()),
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(sw, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

/** رقم يعدّ تصاعديًا عند ظهوره — يعطي إحساس المكسب بدل الرقم الجامد. */
@Composable
fun CountUpText(
    target: Int,
    style: TextStyle = MaterialTheme.typography.displaySmall,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    var started by remember(target) { mutableIntStateOf(0) }
    val value by animateIntAsState(
        targetValue = started,
        animationSpec = tween(durationMillis = 900),
        label = "countup"
    )
    LaunchedEffect(target) { started = target }
    Text("$value", style = style, color = color)
}

/**
 * عدّاد تنازلي حي حتى لحظة مستهدفة — يتحدّث كل ثانية.
 * يعرض: "3 أيام و 14:22:05". عند بلوغ الهدف يعرض نص الاكتمال.
 */
@Composable
fun LiveCountdown(
    target: Instant,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.primary,
    doneText: String = "اكتمل! 🎉"
) {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(target) {
        while (true) {
            now = Instant.now()
            delay(1000)
        }
    }
    val d = Duration.between(now, target)
    val text = if (d.isNegative || d.isZero) doneText else {
        val days = d.toDays()
        val h = d.toHours() % 24
        val m = d.toMinutes() % 60
        val sec = d.seconds % 60
        val clock = "%02d:%02d:%02d".format(h, m, sec)
        when (days) {
            0L -> "باقي $clock"
            1L -> "باقي يوم و $clock"
            2L -> "باقي يومان و $clock"
            in 3..10 -> "باقي $days أيام و $clock"
            else -> "باقي $days يومًا و $clock"
        }
    }
    Text(text, style = style, color = color)
}

private data class Particle(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val drift: Float,
    val spin: Float
)

/**
 * كونفيتي احتفالي — يُشغَّل عند فتح كأس جديدة ثم يستدعي [onDone].
 * غيّر [trigger] لأي قيمة موجبة جديدة لإطلاقه.
 */
@Composable
fun ConfettiOverlay(trigger: Int, onDone: () -> Unit) {
    if (trigger <= 0) return
    val palette = listOf(
        Accent.Amber, Accent.Teal, Accent.Violet, Accent.Magenta,
        Accent.Cyan, Accent.Lime, Accent.Coral, Color(0xFFFFD700)
    )
    val particles = remember(trigger) {
        val rnd = Random(trigger)
        List(90) {
            Particle(
                x = rnd.nextFloat(),
                delay = rnd.nextFloat() * .35f,
                speed = .8f + rnd.nextFloat() * .9f,
                size = 8f + rnd.nextFloat() * 12f,
                color = palette[rnd.nextInt(palette.size)],
                drift = (rnd.nextFloat() - .5f) * 140f,
                spin = (rnd.nextFloat() - .5f) * 720f
            )
        }
    }
    val t = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) {
        t.snapTo(0f)
        t.animateTo(1f, tween(3200, easing = LinearEasing))
        onDone()
    }
    Canvas(Modifier.fillMaxSize()) {
        val w = this.size.width
        val h = this.size.height
        particles.forEach { p ->
            val local = ((t.value - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f || local >= 1f) return@forEach
            val y = local * p.speed * (h + 200f) - 100f
            val x = p.x * w + sin(local * 6f) * p.drift
            val fade = if (local > .8f) (1f - local) * 5f else 1f
            translate(x, y) {
                rotate(p.spin * local, pivot = Offset.Zero) {
                    drawRoundRect(
                        color = p.color.copy(alpha = fade),
                        topLeft = Offset(-p.size / 2, -p.size / 3),
                        size = Size(p.size, p.size * .66f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
                    )
                }
            }
        }
    }
}
