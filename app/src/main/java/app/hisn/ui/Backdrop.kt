package app.hisn.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.hisn.ui.theme.Accent
import kotlin.math.cos
import kotlin.math.sin

/**
 * خلفية متدرّجة ناعمة.
 *
 * أربع بقع لونية بتدرّج شعاعي تتحرك ببطء شديد خلف المحتوى. لا حدود حادة،
 * وشفافية منخفضة تكفي لإعطاء عمق دون أن تنافس النص على الانتباه.
 */
@Composable
fun Backdrop(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val base = MaterialTheme.colorScheme.background

    val t = rememberInfiniteTransition(label = "backdrop")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val blobs = if (dark) listOf(
        Accent.Emerald to 0.30f,
        Accent.Indigo to 0.26f,
        Accent.Violet to 0.22f,
        Accent.Cyan to 0.18f
    ) else listOf(
        Accent.Teal to 0.24f,
        Accent.Blue to 0.18f,
        Accent.Violet to 0.16f,
        Accent.Amber to 0.14f
    )

    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(base)
            val w = size.width
            val h = size.height
            blobs.forEachIndexed { i, (color, alpha) ->
                val a = phase + i * 1.7f
                val cx = w * (0.2f + 0.6f * ((sin(a + i) + 1f) / 2f))
                val cy = h * (0.08f + 0.5f * ((cos(a * 0.7f + i) + 1f) / 2f))
                val r = (w.coerceAtLeast(h)) * (0.45f + 0.12f * i)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = alpha * intensity),
                            color.copy(alpha = 0f)
                        ),
                        center = Offset(cx, cy),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }
        content()
    }
}

/** تدرّج ثابت خفيف للشاشات التي لا تحتاج حركة (المشغّل، التسجيل). */
@Composable
fun StaticBackdrop(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    val base = MaterialTheme.colorScheme.background
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(base)
            val r = size.width * 1.15f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = .22f), tint.copy(alpha = 0f)),
                    center = Offset(size.width * .5f, size.height * .12f),
                    radius = r
                ),
                radius = r,
                center = Offset(size.width * .5f, size.height * .12f)
            )
        }
        content()
    }
}
