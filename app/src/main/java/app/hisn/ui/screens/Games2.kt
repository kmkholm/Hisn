package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.VSpace
import app.hisn.ui.theme.Accent
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

// ═════════════════ 2048 ═════════════════

private fun addRandomTile(g: MutableList<Int>, rnd: Random) {
    val empty = g.indices.filter { g[it] == 0 }
    if (empty.isEmpty()) return
    g[empty[rnd.nextInt(empty.size)]] = if (rnd.nextInt(10) == 0) 4 else 2
}

/** يدمج صفًا نحو البداية ويعيد (الصف الجديد، النقاط المكتسبة). */
private fun mergeLine(line: List<Int>): Pair<List<Int>, Int> {
    val nums = line.filter { it != 0 }.toMutableList()
    var gained = 0
    var i = 0
    val out = mutableListOf<Int>()
    while (i < nums.size) {
        if (i + 1 < nums.size && nums[i] == nums[i + 1]) {
            out.add(nums[i] * 2); gained += nums[i] * 2; i += 2
        } else {
            out.add(nums[i]); i++
        }
    }
    while (out.size < 4) out.add(0)
    return out to gained
}

private fun canMove(g: List<Int>): Boolean {
    if (g.any { it == 0 }) return true
    for (r in 0 until 4) for (c in 0 until 4) {
        val v = g[r * 4 + c]
        if (c < 3 && g[r * 4 + c + 1] == v) return true
        if (r < 3 && g[(r + 1) * 4 + c] == v) return true
    }
    return false
}

private fun tileColor(v: Int): Color = when (v) {
    2 -> Color(0xFFEEE4DA); 4 -> Color(0xFFEDE0C8)
    8 -> Color(0xFFF2B179); 16 -> Color(0xFFF59563)
    32 -> Color(0xFFF67C5F); 64 -> Color(0xFFF65E3B)
    128 -> Color(0xFFEDCF72); 256 -> Color(0xFFEDCC61)
    512 -> Color(0xFFEDC850); 1024 -> Color(0xFFEDC53F)
    2048 -> Color(0xFFEDC22E); else -> Color(0xFF3C3A32)
}

@Composable
fun G2048Screen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val rnd = remember(round) { Random(System.nanoTime()) }
    val grid = remember(round) {
        mutableStateListOf<Int>().apply {
            repeat(16) { add(0) }
            addRandomTile(this, rnd); addRandomTile(this, rnd)
        }
    }
    var score by remember(round) { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(0) }
    var rewarded by remember(round) { mutableStateOf(false) }
    val over = !canMove(grid)
    val reached2048 = grid.any { it >= 2048 }

    fun swipe(dx: Float, dy: Float) {
        if (over) return
        val horizontal = abs(dx) > abs(dy)
        var gained = 0
        var changed = false
        val old = grid.toList()
        if (horizontal) {
            for (r in 0 until 4) {
                val row = (0 until 4).map { grid[r * 4 + it] }
                val src = if (dx < 0) row else row.reversed()
                val (merged, g) = mergeLine(src)
                gained += g
                val newRow = if (dx < 0) merged else merged.reversed()
                for (c in 0 until 4) grid[r * 4 + c] = newRow[c]
            }
        } else {
            for (c in 0 until 4) {
                val col = (0 until 4).map { grid[it * 4 + c] }
                val src = if (dy < 0) col else col.reversed()
                val (merged, g) = mergeLine(src)
                gained += g
                val newCol = if (dy < 0) merged else merged.reversed()
                for (r in 0 until 4) grid[r * 4 + c] = newCol[r]
            }
        }
        changed = old != grid.toList()
        if (changed) {
            score += gained
            if (score > best) best = score
            addRandomTile(grid, rnd)
        }
    }

    if (over && !rewarded && score > 0) {
        rewarded = true
        onDone(GameKind.G2048.id)
    }

    StaticBackdrop(tint = Accent.Amber) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("2048")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill("النقاط: $score")
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }
            Text(
                "اسحب بإصبعك لدمج البلاطات المتساوية. الهدف: بلاطة 2048.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            var dragX by remember { mutableStateOf(0f) }
            var dragY by remember { mutableStateOf(0f) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFBBADA0))
                        .pointerInput(round) {
                            detectDragGestures(
                                onDragStart = { dragX = 0f; dragY = 0f },
                                onDrag = { _, amount ->
                                    dragX += amount.x; dragY += amount.y
                                },
                                onDragEnd = {
                                    if (abs(dragX) > 60 || abs(dragY) > 60) swipe(dragX, dragY)
                                }
                            )
                        }
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (r in 0 until 4) {
                        Row(
                            Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until 4) {
                                val v = grid[r * 4 + c]
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (v == 0) Color(0xFFCDC1B4) else tileColor(v)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (v != 0) {
                                        Text(
                                            "$v",
                                            fontSize = if (v < 100) 26.sp
                                            else if (v < 1000) 22.sp else 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (v <= 4) Color(0xFF776E65)
                                            else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (reached2048) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .14f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    Text("🏆 وصلت 2048! يمكنك المواصلة أو بدء جولة جديدة.",
                        style = MaterialTheme.typography.titleMedium)
                }
            }
            if (over) {
                SectionCard(
                    tone = Accent.Coral.copy(alpha = .12f),
                    border = Accent.Coral.copy(alpha = .5f)
                ) {
                    Text("انتهت الحركات — نتيجتك: $score",
                        style = MaterialTheme.typography.titleLarge)
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

// ═════════════════ إكس-أو ضد الجهاز ═════════════════

private fun xoWinner(b: List<Int>): Int {
    val lines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    for (l in lines) {
        if (b[l[0]] != 0 && b[l[0]] == b[l[1]] && b[l[1]] == b[l[2]]) return b[l[0]]
    }
    return 0
}

/** ميني-ماكس كامل — الجهاز (2) لا يخسر أبدًا؛ أفضل ما يناله اللاعب التعادل. */
private fun minimax(b: MutableList<Int>, player: Int): Pair<Int, Int> {
    val w = xoWinner(b)
    if (w == 2) return 10 to -1
    if (w == 1) return -10 to -1
    if (b.none { it == 0 }) return 0 to -1

    var bestScore = if (player == 2) -100 else 100
    var bestMove = -1
    for (i in b.indices) {
        if (b[i] != 0) continue
        b[i] = player
        val (s, _) = minimax(b, 3 - player)
        b[i] = 0
        if (player == 2 && s > bestScore) { bestScore = s; bestMove = i }
        if (player == 1 && s < bestScore) { bestScore = s; bestMove = i }
    }
    return bestScore to bestMove
}

@Composable
fun XoScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val board = remember(round) { mutableStateListOf(0, 0, 0, 0, 0, 0, 0, 0, 0) }
    var thinking by remember(round) { mutableStateOf(false) }
    var wins by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }
    var losses by remember { mutableIntStateOf(0) }
    var rewarded by remember(round) { mutableStateOf(false) }

    val winner = xoWinner(board)
    val full = board.none { it == 0 }
    val overRound = winner != 0 || full

    LaunchedEffect(overRound) {
        if (overRound && !rewarded) {
            rewarded = true
            when (winner) {
                1 -> wins++
                2 -> losses++
                else -> draws++
            }
            // التعادل مع خصم لا يخطئ إنجاز — يُسجَّل كنشاط.
            if (winner != 2) onDone(GameKind.XO.id)
        }
    }

    LaunchedEffect(board.count { it != 0 }) {
        // دور الجهاز بعد لحظة تفكير قصيرة
        if (!overRound && board.count { it != 0 } % 2 == 1) {
            thinking = true
            delay(400)
            val (_, move) = minimax(board.toMutableList(), 2)
            if (move >= 0) board[move] = 2
            thinking = false
        }
    }

    StaticBackdrop(tint = Accent.Violet) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("إكس-أو ضد الجهاز")
                TextButton(onClick = onExit) { Text("خروج") }
            }
            Text(
                "أنت ✕ — والجهاز يلعب بلا أخطاء. هل تحقق التعادل؟",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("تعادل: $draws", bg = Accent.Emerald.copy(alpha = .16f), fg = Accent.Emerald)
                Pill("خسارة: $losses", bg = Accent.Coral.copy(alpha = .16f), fg = Accent.Coral)
            }

            Column(
                Modifier
                    .fillMaxWidth(.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent.Violet.copy(alpha = .25f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0 until 3) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0 until 3) {
                            val i = r * 3 + c
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable(
                                        enabled = board[i] == 0 && !overRound && !thinking
                                    ) { board[i] = 1 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (board[i]) {
                                        1 -> "✕"; 2 -> "◯"; else -> ""
                                    },
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (board[i] == 1) Accent.Violet else Accent.Coral
                                )
                            }
                        }
                    }
                }
            }

            if (overRound) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .12f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    Text(
                        when (winner) {
                            1 -> "🎉 فزت؟! هذا لا يحدث نظريًا — أحسنت!"
                            2 -> "خسرت هذه — الجهاز لا يرحم. حاول التعادل!"
                            else -> "🤝 تعادل — وهذا أفضل الممكن ضد خصم مثالي!"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = { round++ }) { Text("جولة جديدة") }
                }
            } else if (thinking) {
                Text(
                    "الجهاز يفكر…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═════════════════ طارد الأرقام (جدول شولت) ═════════════════

@Composable
fun SchulteScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val numbers = remember(round) { (1..25).shuffled() }
    var next by remember(round) { mutableIntStateOf(1) }
    var millis by remember(round) { mutableIntStateOf(0) }
    var wrongFlash by remember { mutableStateOf(-1) }
    var rewarded by remember(round) { mutableStateOf(false) }
    val doneRound = next > 25

    LaunchedEffect(round, doneRound) {
        while (!doneRound) { delay(100); millis += 100 }
    }
    if (doneRound && !rewarded) {
        rewarded = true
        onDone(GameKind.SCHULTE.id)
    }

    StaticBackdrop(tint = Accent.Cyan) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("طارد الأرقام")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill("%.1f ث".format(millis / 1000f))
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }
            Text(
                "اضغط الأرقام بالترتيب من 1 إلى 25 بأسرع ما تستطيع — " +
                    "تدريب انتباه معروف يشدّ التركيز كله إلى نقطة واحدة.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "التالي: $next",
                style = MaterialTheme.typography.titleLarge,
                color = Accent.Cyan
            )

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (r in 0 until 5) {
                        Row(
                            Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (c in 0 until 5) {
                                val v = numbers[r * 5 + c]
                                val cleared = v < next
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                cleared -> Accent.Emerald.copy(alpha = .25f)
                                                wrongFlash == v -> Accent.Coral.copy(alpha = .4f)
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = !doneRound) {
                                            if (v == next) next++
                                            else wrongFlash = v
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (cleared) "✓" else "$v",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (cleared) Accent.Emerald
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (doneRound) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .14f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    val secs = millis / 1000f
                    Text(
                        "⚡ أنهيتها في %.1f ثانية".format(secs) + when {
                            secs < 30 -> " — تركيز نخبة!"
                            secs < 50 -> " — ممتاز!"
                            else -> " — والقادم أسرع"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(onClick = { round++ }) { Text("جولة جديدة") }
                }
            }
        }
    }
}

// ═════════════════ تسلسل الألوان (سايمون) ═════════════════

private val SIMON_COLORS = listOf(
    Accent.Emerald, Accent.Coral, Accent.Amber, Accent.Blue
)

@Composable
fun SimonScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val sequence = remember(round) { mutableStateListOf<Int>() }
    var inputIndex by remember(round) { mutableIntStateOf(0) }
    var showing by remember(round) { mutableStateOf(true) }
    var lit by remember { mutableIntStateOf(-1) }
    var gameOver by remember(round) { mutableStateOf(false) }
    var rewarded by remember(round) { mutableStateOf(false) }
    val level = sequence.size

    LaunchedEffect(round, sequence.size, gameOver) {
        if (gameOver) return@LaunchedEffect
        if (sequence.isEmpty()) sequence.add(Random.nextInt(4))
        // عرض التسلسل
        showing = true
        delay(600)
        sequence.forEach { pad ->
            lit = pad; delay(520)
            lit = -1; delay(180)
        }
        showing = false
        inputIndex = 0
    }

    if (gameOver && !rewarded && level >= 5) {
        rewarded = true
        onDone(GameKind.SIMON.id)
    }

    StaticBackdrop(tint = Accent.Blue) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("تسلسل الألوان")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill("المستوى: $level")
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }
            Text(
                if (gameOver) "انتهت الجولة"
                else if (showing) "راقب التسلسل…"
                else "كرر التسلسل (${inputIndex + 1}/${sequence.size})",
                style = MaterialTheme.typography.titleMedium,
                color = if (showing) Accent.Blue else MaterialTheme.colorScheme.onSurface
            )

            Column(
                Modifier
                    .fillMaxWidth(.85f)
                    .aspectRatio(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (r in 0 until 2) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (c in 0 until 2) {
                            val i = r * 2 + c
                            val base = SIMON_COLORS[i]
                            val isLit = lit == i
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .scale(if (isLit) 1.04f else 1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(base.copy(alpha = if (isLit) 1f else .35f))
                                    .clickable(enabled = !showing && !gameOver) {
                                        lit = i
                                        if (i == sequence[inputIndex]) {
                                            inputIndex++
                                            if (inputIndex == sequence.size) {
                                                sequence.add(Random.nextInt(4))
                                            }
                                        } else {
                                            gameOver = true
                                        }
                                    }
                            )
                        }
                    }
                }
            }
            // إطفاء الإضاءة بعد نقرة اللاعب
            LaunchedEffect(lit, showing) {
                if (!showing && lit >= 0) { delay(220); lit = -1 }
            }

            if (gameOver) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .12f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    Text(
                        "وصلت المستوى $level" + when {
                            level >= 12 -> " — ذاكرة فولاذية! 🧠"
                            level >= 8 -> " — قوي جدًا!"
                            level >= 5 -> " — جيد، وسُجّل في نقاطك"
                            else -> " — الإحماء انتهى"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { round++ }) { Text("جولة جديدة") }
                }
            }
        }
    }
}
