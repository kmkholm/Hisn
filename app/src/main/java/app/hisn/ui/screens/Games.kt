package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Filter4
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.VSpace
import app.hisn.ui.theme.Accent
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * ألعاب ذهنية داخل التطبيق.
 *
 * الغرض علاجي لا ترفيهي فقط: لحظة الفراغ الخطر تحتاج ما يخطف الانتباه كله
 * خلال ثوانٍ. اللعبة الذهنية تستهلك «نفس الانتباه» الذي تطلبه الرغبة —
 * وكل جولة منتهية تُحتسب في نقاطك وكؤوس الأنشطة.
 */
enum class GameKind(val id: String, val title: String, val desc: String, val minutes: Int) {
    SUDOKU("game_sudoku", "سودوكو", "الكلاسيكية الأقوى لشغل الذهن بالكامل", 10),
    MEMORY("game_memory", "مطابقة الذاكرة", "اقلب البطاقات وطابق الأزواج بأقل محاولات", 4),
    MATH("game_math", "الحساب السريع", "60 ثانية من العمليات — كم إجابة تصيب؟", 2),
    G2048("game_2048", "2048", "اسحب وادمج البلاطات حتى 2048", 6),
    XO("game_xo", "إكس-أو", "تحدَّ جهازًا لا يخطئ — هل تحقق التعادل؟", 3),
    SCHULTE("game_schulte", "طارد الأرقام", "1 إلى 25 بالترتيب بأسرع وقت — تدريب انتباه", 3),
    SIMON("game_simon", "تسلسل الألوان", "كرر تسلسلًا يطول — تدريب ذاكرة عاملة", 4),
    BREAKOUT("game_breakout", "كسر الطوب", "أتاري الأصلية: مضرب وكرة وجدار طوب يتحطم", 5),
    PONG("game_pong", "تنس الطاولة", "طابة الأتاري ضد الجهاز — أول من يصل 5 يفوز", 4),
    EXCUSE("game_excuse", "صائد التبريرات", "لعبتنا الأصلية: اصطد التبريرات المشوَّهة قبل أن تصل القاع", 2)
}

@Composable
fun GamesList(onOpen: (GameKind) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    "الرغبة تطلب انتباهك كله — واللعبة الذهنية تخطفه منها. " +
                        "دقائق من التركيز الكامل تكفي غالبًا لتنكسر الموجة، " +
                        "وكل جولة تنهيها تُحتسب في نقاطك.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            GameCard(
                GameKind.EXCUSE, Icons.Outlined.Psychology,
                listOf(Accent.Magenta, Accent.Violet), onOpen
            )
        }
        item {
            GameCard(
                GameKind.SUDOKU, Icons.Outlined.GridOn,
                listOf(Accent.Indigo, Accent.Violet), onOpen
            )
        }
        item {
            GameCard(
                GameKind.MEMORY, Icons.Outlined.Style,
                listOf(Accent.Magenta, Accent.Coral), onOpen
            )
        }
        item {
            GameCard(
                GameKind.MATH, Icons.Outlined.Calculate,
                listOf(Accent.Teal, Accent.Cyan), onOpen
            )
        }
        item {
            GameCard(
                GameKind.G2048, Icons.Outlined.Filter4,
                listOf(Accent.Amber, Accent.Sand), onOpen
            )
        }
        item {
            GameCard(
                GameKind.XO, Icons.Outlined.Tag,
                listOf(Accent.Violet, Accent.Indigo), onOpen
            )
        }
        item {
            GameCard(
                GameKind.SCHULTE, Icons.Outlined.Speed,
                listOf(Accent.Cyan, Accent.Blue), onOpen
            )
        }
        item {
            GameCard(
                GameKind.SIMON, Icons.Outlined.Palette,
                listOf(Accent.Blue, Accent.Violet), onOpen
            )
        }
        item {
            GameCard(
                GameKind.BREAKOUT, Icons.Outlined.ViewModule,
                listOf(Accent.Coral, Accent.Amber), onOpen
            )
        }
        item {
            GameCard(
                GameKind.PONG, Icons.Outlined.SportsTennis,
                listOf(Accent.Emerald, Accent.Teal), onOpen
            )
        }
    }
}

@Composable
private fun GameCard(
    game: GameKind,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: List<Color>,
    onOpen: (GameKind) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors))
            .clickable { onOpen(game) }
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = .18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                game.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                game.desc,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = .85f)
            )
        }
        Pill("~${game.minutes} د", bg = Color.White.copy(alpha = .18f), fg = Color.White)
    }
}

// ═════════════════ سودوكو ═════════════════

/**
 * مولّد سودوكو صحيح بالبناء: شبكة أساس صالحة رياضيًا
 * value(r,c) = (r*3 + r/3 + c) mod 9، ثم تبديل أرقام وصفوف وأعمدة
 * ضمن الحزم — كل التحويلات تحفظ صحة السودوكو.
 */
private fun generateSudoku(seed: Long, holes: Int): Pair<IntArray, BooleanArray> {
    val rnd = Random(seed)
    val digits = (1..9).shuffled(rnd)

    fun shuffledGroups(): IntArray {
        val bands = (0..2).shuffled(rnd)
        val out = IntArray(9)
        var i = 0
        bands.forEach { b ->
            (0..2).shuffled(rnd).forEach { r -> out[i++] = b * 3 + r }
        }
        return out
    }

    val rowMap = shuffledGroups()
    val colMap = shuffledGroups()
    val solution = IntArray(81)
    for (r in 0 until 9) for (c in 0 until 9) {
        val br = rowMap[r]; val bc = colMap[c]
        solution[r * 9 + c] = digits[(br * 3 + br / 3 + bc) % 9]
    }
    val given = BooleanArray(81) { true }
    (0 until 81).shuffled(rnd).take(holes).forEach { given[it] = false }
    return solution to given
}

private fun sudokuConflicts(cells: IntArray): Set<Int> {
    val bad = mutableSetOf<Int>()
    fun check(indices: List<Int>) {
        val seen = mutableMapOf<Int, MutableList<Int>>()
        indices.forEach { i ->
            val v = cells[i]
            if (v != 0) seen.getOrPut(v) { mutableListOf() }.add(i)
        }
        seen.values.filter { it.size > 1 }.forEach { bad.addAll(it) }
    }
    for (r in 0 until 9) check((0 until 9).map { r * 9 + it })
    for (c in 0 until 9) check((0 until 9).map { it * 9 + c })
    for (br in 0 until 3) for (bc in 0 until 3) {
        check((0 until 9).map { (br * 3 + it / 3) * 9 + bc * 3 + it % 3 })
    }
    return bad
}

@Composable
fun SudokuScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var difficulty by remember { mutableIntStateOf(1) } // 0 سهل 1 وسط 2 صعب
    var seed by remember { mutableStateOf(System.nanoTime()) }
    val holes = when (difficulty) { 0 -> 36; 1 -> 45; else -> 52 }
    val puzzle = remember(seed, difficulty) { generateSudoku(seed, holes) }
    val solution = puzzle.first
    val given = puzzle.second

    val cells = remember(seed, difficulty) {
        mutableStateListOf<Int>().apply {
            for (i in 0 until 81) add(if (given[i]) solution[i] else 0)
        }
    }
    var selected by remember(seed, difficulty) { mutableIntStateOf(-1) }
    var seconds by remember(seed, difficulty) { mutableIntStateOf(0) }
    var won by remember(seed, difficulty) { mutableStateOf(false) }
    var rewarded by remember { mutableStateOf(false) }

    LaunchedEffect(seed, difficulty, won) {
        while (!won) { delay(1000); seconds++ }
    }

    val conflicts = sudokuConflicts(cells.toIntArray())
    if (!won && cells.none { it == 0 } && conflicts.isEmpty()) {
        won = true
        if (!rewarded) { rewarded = true; onDone(GameKind.SUDOKU.id) }
    }

    StaticBackdrop(tint = Accent.Indigo) {
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
                Eyebrow("سودوكو")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%d:%02d".format(seconds / 60, seconds % 60),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("سهل", "وسط", "صعب").forEachIndexed { i, label ->
                    val on = difficulty == i
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (on) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                difficulty = i; seed = System.nanoTime()
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (on) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { seed = System.nanoTime() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        "🔄 جديدة",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // الشبكة — اتجاه ثابت من اليسار حتى لا تنعكس الأرقام
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.outline)
                        .padding(2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    for (r in 0 until 9) {
                        Row(
                            Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            for (c in 0 until 9) {
                                val i = r * 9 + c
                                val v = cells[i]
                                val isSel = i == selected
                                val sameBox = selected >= 0 &&
                                    (selected / 9 == r || selected % 9 == c)
                                val bg = when {
                                    isSel -> MaterialTheme.colorScheme.primary.copy(alpha = .3f)
                                    conflicts.contains(i) -> Accent.Coral.copy(alpha = .3f)
                                    sameBox -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f)
                                    (r / 3 + c / 3) % 2 == 0 -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .8f)
                                }
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .background(bg)
                                        .clickable(enabled = !given[i]) { selected = i },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (v != 0) {
                                        Text(
                                            "$v",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (given[i]) FontWeight.Bold
                                            else FontWeight.Normal,
                                            color = when {
                                                given[i] -> MaterialTheme.colorScheme.onSurface
                                                conflicts.contains(i) -> Accent.Coral
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (won) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .14f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    Text(
                        "🎉 أحسنت! حللتها في ${seconds / 60}:${"%02d".format(seconds % 60)}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "سُجّلت في نقاطك وكؤوس الأنشطة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { seed = System.nanoTime(); rewarded = false }) {
                        Text("لغز جديد")
                    }
                }
            } else {
                // لوحة الأرقام
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (n in 1..9) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(.8f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        if (selected >= 0 && !given[selected]) cells[selected] = n
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$n",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { if (selected >= 0 && !given[selected]) cells[selected] = 0 },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("مسح الخانة") }
            }
        }
    }
}

// ═════════════════ مطابقة الذاكرة ═════════════════

private val MEMORY_EMOJIS =
    listOf("🍎", "🌙", "⭐", "🌊", "🔥", "🌸", "⚽", "🎈", "🐪", "🗝️", "🎯", "💎")

@Composable
fun MemoryScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val cards = remember(round) {
        (MEMORY_EMOJIS.shuffled().take(8) + MEMORY_EMOJIS.shuffled().take(8))
            .let { it.take(8) + it.take(8) }
            .shuffled()
    }
    val matched = remember(round) { mutableStateListOf<Int>() }
    val revealed = remember(round) { mutableStateListOf<Int>() }
    var moves by remember(round) { mutableIntStateOf(0) }
    var lock by remember(round) { mutableStateOf(false) }
    var rewarded by remember(round) { mutableStateOf(false) }
    val won = matched.size == 16

    if (won && !rewarded) {
        rewarded = true
        onDone(GameKind.MEMORY.id)
    }

    LaunchedEffect(revealed.size) {
        if (revealed.size == 2) {
            lock = true
            val a = revealed[0]; val b = revealed[1]
            if (cards[a] == cards[b]) {
                matched.add(a); matched.add(b)
                revealed.clear()
            } else {
                delay(750)
                revealed.clear()
            }
            lock = false
        }
    }

    StaticBackdrop(tint = Accent.Magenta) {
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
                Eyebrow("مطابقة الذاكرة")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill("المحاولات: $moves")
                    TextButton(onClick = onExit) { Text("خروج") }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0 until 4) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0 until 4) {
                            val i = r * 4 + c
                            val faceUp = matched.contains(i) || revealed.contains(i)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (faceUp) MaterialTheme.colorScheme.surface
                                        else Accent.Magenta.copy(alpha = .8f)
                                    )
                                    .border(
                                        2.dp,
                                        if (matched.contains(i)) Accent.Emerald
                                        else Color.Transparent,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(
                                        enabled = !lock && !faceUp && !won
                                    ) {
                                        if (revealed.size < 2) {
                                            revealed.add(i)
                                            if (revealed.size == 2) moves++
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (faceUp) cards[i] else "؟",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = if (faceUp) MaterialTheme.colorScheme.onSurface
                                    else Color.White
                                )
                            }
                        }
                    }
                }
            }

            if (won) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .14f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    Text(
                        "🎉 اكتملت في $moves محاولة" +
                            if (moves <= 12) " — ذاكرة ممتازة!" else "!",
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

// ═════════════════ الحساب السريع ═════════════════

private data class MathQ(val text: String, val answer: Int, val options: List<Int>)

private fun nextQuestion(rnd: Random): MathQ {
    val op = rnd.nextInt(3)
    val (a, b, ans, sym) = when (op) {
        0 -> { val x = rnd.nextInt(12, 60); val y = rnd.nextInt(12, 40); listOf(x, y, x + y, 0) }
        1 -> { val x = rnd.nextInt(30, 90); val y = rnd.nextInt(10, x - 5); listOf(x, y, x - y, 1) }
        else -> { val x = rnd.nextInt(3, 13); val y = rnd.nextInt(3, 13); listOf(x, y, x * y, 2) }
    }
    val symbol = listOf("+", "−", "×")[sym]
    val opts = mutableSetOf(ans)
    while (opts.size < 4) {
        opts.add(ans + listOf(-10, -5, -3, -2, -1, 1, 2, 3, 5, 10).random(rnd))
    }
    return MathQ("$a $symbol $b = ؟", ans, opts.shuffled(rnd))
}

@Composable
fun MathSprintScreen(onDone: (String) -> Unit, onExit: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    val rnd = remember(round) { Random(System.nanoTime()) }
    var q by remember(round) { mutableStateOf(nextQuestion(rnd)) }
    var timeLeft by remember(round) { mutableIntStateOf(60) }
    var score by remember(round) { mutableIntStateOf(0) }
    var streak by remember(round) { mutableIntStateOf(0) }
    var flash by remember { mutableStateOf<Boolean?>(null) }
    var rewarded by remember(round) { mutableStateOf(false) }
    val over = timeLeft <= 0

    LaunchedEffect(round) {
        while (timeLeft > 0) { delay(1000); timeLeft-- }
    }
    if (over && !rewarded && score > 0) {
        rewarded = true
        onDone(GameKind.MATH.id)
    }

    StaticBackdrop(tint = Accent.Teal) {
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
                Eyebrow("الحساب السريع")
                TextButton(onClick = onExit) { Text("خروج") }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$timeLeft",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (timeLeft <= 10) Accent.Coral
                        else MaterialTheme.colorScheme.primary
                    )
                    Text("ثانية", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", style = MaterialTheme.typography.headlineLarge)
                    Text("إجابة صحيحة", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$streak",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Accent.Amber
                    )
                    Text("متتالية", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (over) {
                SectionCard(
                    tone = Accent.Emerald.copy(alpha = .14f),
                    border = Accent.Emerald.copy(alpha = .5f)
                ) {
                    Text(
                        "⏱ انتهى الوقت — نتيجتك: $score",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        when {
                            score >= 25 -> "سرعة صاروخية! 🚀"
                            score >= 15 -> "أداء قوي جدًا 💪"
                            score >= 8 -> "جيد — والقادم أفضل"
                            else -> "الإحماء انتهى. جولة أخرى؟"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (score > 0) {
                        Text(
                            "سُجّلت في نقاطك وكؤوس الأنشطة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = { round++ }) { Text("جولة جديدة") }
                }
            } else {
                VSpace(10)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        q.text,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        color = when (flash) {
                            true -> Accent.Emerald
                            false -> Accent.Coral
                            null -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                VSpace(6)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    q.options.chunked(2).forEach { rowOpts ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowOpts.forEach { opt ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            if (opt == q.answer) {
                                                score++; streak++; flash = true
                                            } else {
                                                streak = 0; flash = false
                                            }
                                            q = nextQuestion(rnd)
                                        }
                                        .padding(vertical = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "$opt",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                            if (rowOpts.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
