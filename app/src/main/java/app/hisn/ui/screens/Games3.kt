package app.hisn.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.theme.Accent
import kotlin.math.abs
import kotlin.random.Random

/*
 * لعبتا أتاري كلاسيكيتان بفيزياء حقيقية.
 *
 * نمط الأداء: حالة الفيزياء في حامل عادي (لا State) يتحدّث كل إطار عبر
 * withFrameNanos، وعدّاد إطارات واحد State يعيد رسم الـCanvas — فلا يعاد
 * تركيب الشجرة كلها ستين مرة في الثانية.
 */

// ═════════════════ كسر الطوب (Breakout) ═════════════════

private const val BRICK_COLS = 7
private const val BRICK_ROWS = 6

private class BreakoutPhysics {
    var ballX = .5f
    var ballY = .75f
    var vx = .42f
    var vy = -.62f
    var paddleX = .5f
    val paddleW = .24f
    val bricks = BooleanArray(BRICK_COLS * BRICK_ROWS) { true }
    var stuck = true      // الكرة ملتصقة بالمضرب حتى أول لمسة

    fun resetBall() {
        stuck = true
        ballX = paddleX
        ballY = .88f
        vx = if (Random.nextBoolean()) .42f else -.42f
        vy = -.62f
    }

    fun rebuild(speedMul: Float) {
        bricks.fill(true)
        resetBall()
        vx *= speedMul
        vy *= speedMul
    }
}

private val BRICK_COLORS = listOf(
    Accent.Coral, Accent.Amber, Accent.Lime, Accent.Teal, Accent.Blue, Accent.Violet
)

@Composable
fun BreakoutScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val p = remember(round) { BreakoutPhysics() }
    var tick by remember { mutableLongStateOf(0L) }
    var score by remember(round) { mutableIntStateOf(0) }
    var lives by remember(round) { mutableIntStateOf(3) }
    var level by remember(round) { mutableIntStateOf(1) }
    var gameOver by remember(round) { mutableStateOf(false) }
    var rewarded by remember(round) { mutableStateOf(false) }

    // حلقة الفيزياء — إطار بإطار
    LaunchedEffect(round, gameOver) {
        if (gameOver) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (!gameOver) {
            withFrameNanos { now ->
                val dt = ((now - last) / 1_000_000_000f).coerceAtMost(.032f)
                last = now
                if (!p.stuck) {
                    p.ballX += p.vx * dt
                    p.ballY += p.vy * dt
                } else {
                    p.ballX = p.paddleX
                }
                val r = .018f
                // الجدران
                if (p.ballX < r) { p.ballX = r; p.vx = abs(p.vx) }
                if (p.ballX > 1 - r) { p.ballX = 1 - r; p.vx = -abs(p.vx) }
                if (p.ballY < r) { p.ballY = r; p.vy = abs(p.vy) }
                // المضرب
                val py = .93f
                if (p.vy > 0 && p.ballY + r >= py && p.ballY + r <= py + .035f &&
                    abs(p.ballX - p.paddleX) <= p.paddleW / 2 + r
                ) {
                    p.vy = -abs(p.vy)
                    // زاوية حسب موضع الارتطام على المضرب
                    p.vx += (p.ballX - p.paddleX) / (p.paddleW / 2) * .35f
                    p.vx = p.vx.coerceIn(-.85f, .85f)
                }
                // الطوب (منطقة 0.10..0.40 من الارتفاع)
                val top = .10f; val bh = .05f
                if (p.ballY - r < top + BRICK_ROWS * bh && p.ballY - r > top) {
                    val row = ((p.ballY - top) / bh).toInt()
                    val col = (p.ballX * BRICK_COLS).toInt().coerceIn(0, BRICK_COLS - 1)
                    if (row in 0 until BRICK_ROWS) {
                        val idx = row * BRICK_COLS + col
                        if (p.bricks[idx]) {
                            p.bricks[idx] = false
                            p.vy = -p.vy
                            score += 10
                            if (p.bricks.none { it }) {
                                level++
                                score += 50
                                p.rebuild(1.12f)
                            }
                        }
                    }
                }
                // سقوط الكرة
                if (p.ballY > 1.05f) {
                    lives--
                    if (lives <= 0) gameOver = true else p.resetBall()
                }
                tick++
            }
        }
    }

    if (gameOver && !rewarded && score > 0) {
        rewarded = true
        onDone(GameKind.BREAKOUT.id)
    }

    StaticBackdrop(tint = Accent.Coral) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("كسر الطوب")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Pill("المرحلة $level")
                    Pill("❤ $lives")
                    Pill("$score")
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }
            Text(
                "اسحب لتحريك المضرب — واضغط لإطلاق الكرة. حطّم الطوب كله!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            var widthPx by remember { mutableStateOf(1f) }
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(.72f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF101A2B), Color(0xFF1B2A45))
                        )
                    )
                    .pointerInput(round) {
                        detectDragGestures { change, amount ->
                            change.consume()
                            p.paddleX = (p.paddleX + amount.x / size.width)
                                .coerceIn(p.paddleW / 2, 1 - p.paddleW / 2)
                            p.stuck = false
                        }
                    }
                    .pointerInput(round) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                                p.stuck = false
                            }
                        }
                    }
            ) {
                widthPx = size.width
                tick // قراءة العدّاد = إعادة رسم كل إطار
                val w = size.width; val h = size.height

                // الطوب
                val top = .10f * h; val bh = .05f * h
                val bw = w / BRICK_COLS
                for (row in 0 until BRICK_ROWS) {
                    for (col in 0 until BRICK_COLS) {
                        if (!p.bricks[row * BRICK_COLS + col]) continue
                        drawRoundRect(
                            color = BRICK_COLORS[row % BRICK_COLORS.size],
                            topLeft = Offset(col * bw + 3f, top + row * bh + 3f),
                            size = Size(bw - 6f, bh - 6f),
                            cornerRadius = CornerRadius(6f)
                        )
                    }
                }
                // المضرب
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset((p.paddleX - p.paddleW / 2) * w, .93f * h),
                    size = Size(p.paddleW * w, .022f * h),
                    cornerRadius = CornerRadius(10f)
                )
                // الكرة
                drawCircle(
                    color = Accent.Amber,
                    radius = .018f * w * 1.4f,
                    center = Offset(p.ballX * w, p.ballY * h)
                )
            }

            if (gameOver) {
                SectionCard(
                    tone = Accent.Coral.copy(alpha = .12f),
                    border = Accent.Coral.copy(alpha = .5f)
                ) {
                    Text(
                        "انتهت الكرات — النتيجة: $score (المرحلة $level)",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "سُجّلت في نقاطك وكؤوس الأنشطة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { round++ }) { Text("جولة جديدة") }
                }
            }
        }
    }
}

// ═════════════════ تنس الطاولة (Pong) ضد الجهاز ═════════════════

private class PongPhysics {
    var ballX = .5f
    var ballY = .5f
    var vx = .45f
    var vy = .6f
    var playerX = .5f   // المضرب السفلي (اللاعب)
    var aiX = .5f       // المضرب العلوي (الجهاز)
    val paddleW = .22f

    fun serve(towardPlayer: Boolean) {
        ballX = .5f; ballY = .5f
        vx = (if (Random.nextBoolean()) 1 else -1) * (.35f + Random.nextFloat() * .2f)
        vy = if (towardPlayer) .6f else -.6f
    }
}

@Composable
fun PongScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val p = remember(round) { PongPhysics() }
    var tick by remember { mutableLongStateOf(0L) }
    var playerScore by remember(round) { mutableIntStateOf(0) }
    var aiScore by remember(round) { mutableIntStateOf(0) }
    var matchOver by remember(round) { mutableStateOf(false) }
    var rewarded by remember(round) { mutableStateOf(false) }
    val target = 5

    LaunchedEffect(round, matchOver) {
        if (matchOver) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (!matchOver) {
            withFrameNanos { now ->
                val dt = ((now - last) / 1_000_000_000f).coerceAtMost(.032f)
                last = now
                p.ballX += p.vx * dt
                p.ballY += p.vy * dt
                val r = .02f
                // الجدران الجانبية
                if (p.ballX < r) { p.ballX = r; p.vx = abs(p.vx) }
                if (p.ballX > 1 - r) { p.ballX = 1 - r; p.vx = -abs(p.vx) }
                // الجهاز يطارد الكرة بسرعة محدودة — قابل للهزيمة عند الأطراف
                val aiSpeed = .5f
                if (p.aiX < p.ballX) p.aiX = (p.aiX + aiSpeed * dt).coerceAtMost(p.ballX)
                else p.aiX = (p.aiX - aiSpeed * dt).coerceAtLeast(p.ballX)
                p.aiX = p.aiX.coerceIn(p.paddleW / 2, 1 - p.paddleW / 2)
                // مضرب اللاعب (أسفل عند 0.94)
                if (p.vy > 0 && p.ballY + r >= .94f && p.ballY + r <= .975f &&
                    abs(p.ballX - p.playerX) <= p.paddleW / 2 + r
                ) {
                    p.vy = -abs(p.vy) * 1.03f
                    p.vx += (p.ballX - p.playerX) / (p.paddleW / 2) * .3f
                    p.vx = p.vx.coerceIn(-.8f, .8f)
                }
                // مضرب الجهاز (أعلى عند 0.06)
                if (p.vy < 0 && p.ballY - r <= .06f + .015f && p.ballY - r >= .03f &&
                    abs(p.ballX - p.aiX) <= p.paddleW / 2 + r
                ) {
                    p.vy = abs(p.vy) * 1.03f
                    p.vx += (p.ballX - p.aiX) / (p.paddleW / 2) * .3f
                    p.vx = p.vx.coerceIn(-.8f, .8f)
                }
                // نقاط
                if (p.ballY < -.05f) {
                    playerScore++
                    if (playerScore >= target) matchOver = true else p.serve(false)
                }
                if (p.ballY > 1.05f) {
                    aiScore++
                    if (aiScore >= target) matchOver = true else p.serve(true)
                }
                tick++
            }
        }
    }

    if (matchOver && !rewarded && playerScore > 0) {
        rewarded = true
        onDone(GameKind.PONG.id)
    }

    StaticBackdrop(tint = Accent.Teal) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("تنس الطاولة")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Pill(
                        "أنت $playerScore — $aiScore الجهاز",
                        bg = Accent.Teal.copy(alpha = .16f), fg = Accent.Teal
                    )
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }
            Text(
                "اسحب لتحريك مضربك السفلي. أول من يصل $target يفوز — " +
                    "اضرب بطرف المضرب لتغيير الزاوية!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Canvas(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(.72f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0B2B26), Color(0xFF123E36))
                        )
                    )
                    .pointerInput(round) {
                        detectDragGestures { change, amount ->
                            change.consume()
                            p.playerX = (p.playerX + amount.x / size.width)
                                .coerceIn(p.paddleW / 2, 1 - p.paddleW / 2)
                        }
                    }
            ) {
                tick
                val w = size.width; val h = size.height
                // خط المنتصف المنقّط
                var x = 0f
                while (x < w) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = .25f),
                        topLeft = Offset(x, h / 2 - 2f),
                        size = Size(18f, 4f),
                        cornerRadius = CornerRadius(2f)
                    )
                    x += 34f
                }
                // مضرب الجهاز
                drawRoundRect(
                    color = Accent.Coral,
                    topLeft = Offset((p.aiX - p.paddleW / 2) * w, .045f * h),
                    size = Size(p.paddleW * w, .02f * h),
                    cornerRadius = CornerRadius(10f)
                )
                // مضرب اللاعب
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset((p.playerX - p.paddleW / 2) * w, .94f * h),
                    size = Size(p.paddleW * w, .02f * h),
                    cornerRadius = CornerRadius(10f)
                )
                // الكرة
                drawCircle(
                    color = Accent.Amber,
                    radius = .02f * w * 1.3f,
                    center = Offset(p.ballX * w, p.ballY * h)
                )
            }

            if (matchOver) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .12f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    Text(
                        if (playerScore >= target) "🏆 فزت $playerScore–$aiScore !"
                        else "خسرت $aiScore–$playerScore — قربها المرة القادمة!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "سُجّلت في نقاطك وكؤوس الأنشطة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { round++ }) { Text("مباراة جديدة") }
                }
            }
        }
    }
}
