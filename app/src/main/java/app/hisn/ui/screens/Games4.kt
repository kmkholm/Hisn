package app.hisn.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.content.ThoughtBank
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.theme.Accent
import kotlin.random.Random

/**
 * «صائد التبريرات» — لعبة أصلية من هذا التطبيق.
 *
 * الفكرة العلاجية: قبل الانزلاق تمرّ جملة تبرير ثابتة تقريبًا («مرة واحدة فقط»،
 * «سأبدأ غدًا»). من يتعلم التقاطها بسرعة يقطع الطريق بين الفكرة والفعل.
 * اللعبة تدرّب هذا الانعكاس: أفكار تسقط، اصطد المشوَّهة قبل أن تصل القاع
 * واترك السليمة تمرّ بسلام. تدريب على كشف التشويه المعرفي — بشكل لعبة.
 *
 * التصميم بعد الإصلاح: عدّ تنازلي 3 ثوانٍ قبل أول فقاعة، سرعة تبدأ هادئة
 * وتتصاعد، وبطاقة النهاية تظهر فوق الملعب نفسه — لا أسفله حيث قد لا تُرى.
 */
private class Bubble(
    val text: String,
    val distortion: Boolean,
    var x: Float,
    var y: Float,
    val speed: Float,
    var w: Float = 0f
)

private class HunterPhysics {
    val bubbles = mutableListOf<Bubble>()
    var spawnTimer = 0f
    var spawnEvery = 1.9f
    var elapsed = 0f
    var countdown = 3f
    val paint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
}

@Composable
fun ExcuseHunterScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val p = remember(round) { HunterPhysics() }
    var tick by remember { mutableLongStateOf(0L) }
    var score by remember(round) { mutableIntStateOf(0) }
    var lives by remember(round) { mutableIntStateOf(5) }
    var timeLeft by remember(round) { mutableIntStateOf(60) }
    var caught by remember(round) { mutableIntStateOf(0) }
    var countdownShown by remember(round) { mutableIntStateOf(3) }
    var flash by remember { mutableStateOf<Color?>(null) }
    var gameOver by remember(round) { mutableStateOf(false) }
    var rewarded by remember(round) { mutableStateOf(false) }
    val rnd = remember(round) { Random(System.nanoTime()) }

    LaunchedEffect(round, gameOver) {
        if (gameOver) return@LaunchedEffect
        var last = withFrameNanos { it }
        var secAcc = 0f
        while (!gameOver) {
            withFrameNanos { now ->
                val dt = ((now - last) / 1_000_000_000f).coerceAtMost(.032f)
                last = now

                // ── عدّ تنازلي قبل البدء ──
                if (p.countdown > 0f) {
                    p.countdown -= dt
                    val shown = kotlin.math.ceil(p.countdown.toDouble()).toInt().coerceAtLeast(0)
                    if (shown != countdownShown) countdownShown = shown
                    tick++
                    return@withFrameNanos
                }

                p.elapsed += dt
                secAcc += dt
                if (secAcc >= 1f) {
                    secAcc -= 1f
                    timeLeft--
                    if (timeLeft <= 0) gameOver = true
                }
                // تسارع تدريجي: هادئ في البداية
                p.spawnEvery = (1.9f - p.elapsed * .014f).coerceAtLeast(.8f)
                p.spawnTimer += dt
                if (p.spawnTimer >= p.spawnEvery) {
                    p.spawnTimer = 0f
                    val isDist = rnd.nextFloat() < .58f
                    val text = if (isDist) ThoughtBank.distortions.random(rnd)
                    else ThoughtBank.healthy.random(rnd)
                    p.bubbles += Bubble(
                        text = text, distortion = isDist,
                        x = .18f + rnd.nextFloat() * .64f, y = -.05f,
                        speed = .085f + rnd.nextFloat() * .05f + p.elapsed * .0012f
                    )
                }
                val iter = p.bubbles.iterator()
                while (iter.hasNext()) {
                    val b = iter.next()
                    b.y += b.speed * dt
                    if (b.y > 1.02f) {
                        iter.remove()
                        if (b.distortion) {
                            lives--
                            flash = Accent.Coral
                            if (lives <= 0) gameOver = true
                        } else {
                            score += 2
                        }
                    }
                }
                tick++
            }
        }
        // عند النهاية: يُفرَّغ الملعب حتى لا تبدو الفقاعات «معلّقة»
        p.bubbles.clear()
        tick++
    }

    LaunchedEffect(flash) { if (flash != null) { kotlinx.coroutines.delay(180); flash = null } }

    LaunchedEffect(gameOver) {
        if (gameOver && !rewarded && score > 0) {
            rewarded = true
            onDone(GameKind.EXCUSE.id)
        }
    }

    StaticBackdrop(tint = Accent.Magenta) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("صائد التبريرات")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Pill("⏱ $timeLeft")
                    Pill("❤ $lives")
                    Pill("$score")
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }
            Text(
                "اصطد التبريرات المشوَّهة (الحمراء) قبل أن تصل القاع — واترك الأفكار السليمة (الخضراء) تمرّ. " +
                    "لمس فكرة سليمة يخصم، وتبرير يفلت يكلّفك قلبًا.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(.78f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1C1030), Color(0xFF2B1A48))))
            ) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(round) {
                            detectTapGestures { pos ->
                                if (gameOver || p.countdown > 0f) return@detectTapGestures
                                val w = size.width.toFloat(); val h = size.height.toFloat()
                                // الأحدث فوق — نفحص من الأخير
                                val hit = p.bubbles.lastOrNull { b ->
                                    val bx = b.x * w; val by = b.y * h
                                    val half = (b.w / 2 + 34f)
                                    pos.x in (bx - half)..(bx + half) && pos.y in (by - 56f)..(by + 40f)
                                }
                                if (hit != null) {
                                    p.bubbles.remove(hit)
                                    if (hit.distortion) {
                                        score += 10; caught++; flash = Accent.Emerald
                                    } else {
                                        score = (score - 5).coerceAtLeast(0); flash = Accent.Amber
                                    }
                                    tick++
                                }
                            }
                        }
                ) {
                    tick
                    val w = size.width; val h = size.height
                    drawLine(
                        Color.White.copy(alpha = .25f),
                        Offset(0f, h * .98f), Offset(w, h * .98f), strokeWidth = 3f
                    )
                    p.paint.textSize = w * .052f
                    if (!gameOver) p.bubbles.forEach { b ->
                        val tw = p.paint.measureText(b.text)
                        b.w = tw
                        val bx = b.x * w; val by = b.y * h
                        val color = if (b.distortion) Accent.Coral else Accent.Emerald
                        drawRoundRect(
                            color = color.copy(alpha = .92f),
                            topLeft = Offset(bx - tw / 2 - 22f, by - 40f),
                            size = Size(tw + 44f, 62f),
                            cornerRadius = CornerRadius(30f)
                        )
                        p.paint.color = Color.White.toArgb()
                        drawContext.canvas.nativeCanvas.drawText(b.text, bx, by + 6f, p.paint)
                    }
                    flash?.let { drawRect(it.copy(alpha = .18f)) }
                }

                // ── عدّ تنازلي ──
                if (!gameOver && countdownShown > 0) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "$countdownShown",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                        Text(
                            "استعد… اصطد الأحمر فقط",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = .85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ── بطاقة النهاية فوق الملعب ──
                if (gameOver) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = .45f))
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SectionCard(
                            tone = MaterialTheme.colorScheme.surface,
                            border = Accent.Emerald.copy(alpha = .6f)
                        ) {
                            Text(
                                (if (lives <= 0) "أفلتت خمسة تبريرات — " else "انتهى الوقت — ") + "النتيجة: $score",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "اصطدت $caught تبريرًا. " + when {
                                    caught >= 25 -> "انعكاسك في كشف التشويه صار سريعًا جدًا 🎯"
                                    caught >= 12 -> "عين خبيرة تتشكّل — الفكرة المشوَّهة لم تعد تمرّ خفية."
                                    else -> "التبريرات تتنكّر جيدًا. ركّز على «مرة واحدة» و«غدًا» — أخطرها."
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (score > 0) Text(
                                "سُجّلت في نقاطك وكؤوس الأنشطة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { round++ }) { Text("جولة جديدة") }
                                TextButton(onClick = onExit) { Text("خروج") }
                            }
                        }
                    }
                }
            }
        }
    }
}
