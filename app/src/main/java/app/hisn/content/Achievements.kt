package app.hisn.content

import androidx.compose.ui.graphics.Color

/**
 * نظام الكؤوس والرتب.
 *
 * قرار تصميمي: الكؤوس تُبنى على «أطول سلسلة» و«مجموع الأيام» — وكلاهما لا يُمحى
 * بالانتكاسة. فالكأس مكسب دائم، لا شيء يُسحب منك. هذا امتداد لقاعدة التطبيق:
 * لا عقوبة، والتقدّم يتراكم.
 */
enum class Tier(val label: String, val light: Color, val dark: Color) {
    BRONZE("برونزية", Color(0xFFE8A56B), Color(0xFF9C5A21)),
    SILVER("فضية", Color(0xFFE8EAF0), Color(0xFF8E97A8)),
    GOLD("ذهبية", Color(0xFFFFE082), Color(0xFFC79100)),
    PLATINUM("بلاتينية", Color(0xFFB2F5EA), Color(0xFF2A9D8F)),
    DIAMOND("ماسية", Color(0xFFA8D8FF), Color(0xFF1E6FD9)),
    LEGEND("أسطورية", Color(0xFFD1B3FF), Color(0xFF6A35C2))
}

enum class AchCategory(val label: String) {
    STREAK("كؤوس السلسلة"),
    TOTAL("مجموع الأيام النظيفة"),
    URGES("موجات مركوبة"),
    EXERCISES("تمارين منجزة"),
    PROGRAM("أيام المسار"),
    ACTIVITIES("أنشطة بديلة"),
    COMMITMENT("التزام يومي")
}

data class Achievement(
    val id: String,
    val title: String,
    val desc: String,
    val category: AchCategory,
    val threshold: Int,
    val tier: Tier,
    val certEligible: Boolean = false
)

/** لقطة الإحصاءات التي تُقيَّم عليها الكؤوس — كلها تراكمية لا تُصفَّر. */
data class GamifyStats(
    val maxBestStreak: Int,
    val totalCleanDays: Int,
    val urgesSurvived: Int,
    val exercisesDone: Int,
    val programDone: Int,
    val activitiesDone: Int,
    val sessionStreak: Int
)

data class Level(val index: Int, val title: String, val minXp: Int)

object Achievements {

    val all: List<Achievement> = buildList {
        // ── كؤوس السلسلة (أطول سلسلة وصلت إليها — محفوظة للأبد) ──
        streak(1, "أول يوم", "أصعب خطوة هي الأولى — وقد خطوتها.", Tier.BRONZE)
        streak(3, "ثلاثة أيام", "عبرت ذروة أعراض التكيّف.", Tier.BRONZE)
        streak(7, "أسبوع كامل", "سبعة أيام متتالية. النمط بدأ ينكسر.", Tier.SILVER)
        streak(14, "أسبوعان", "جسدك بدأ يستقر، والنوم يتحسّن.", Tier.SILVER)
        streak(20, "عشرون يومًا", "تجاوزت المنطقة الأخطر للانقطاع.", Tier.GOLD, cert = true)
        streak(30, "شهر كامل", "المسار القديم فقد أفضليته التلقائية.", Tier.GOLD, cert = true)
        streak(50, "خمسون يومًا", "نصف الطريق إلى المئة — والأصعب خلفك.", Tier.PLATINUM, cert = true)
        streak(60, "شهران", "قرب وسيط تكوين العادة الجديدة (~66 يومًا).", Tier.PLATINUM, cert = true)
        streak(90, "تسعون يومًا", "أتممت الإطار الكامل. هذه كأس الكبار.", Tier.DIAMOND, cert = true)
        streak(120, "أربعة أشهر", "ما بعد البرنامج — الاستمرار بلا سقالات.", Tier.DIAMOND, cert = true)
        streak(180, "نصف سنة", "ستة أشهر. قلّة من يصلون إلى هنا.", Tier.LEGEND, cert = true)
        streak(270, "تسعة أشهر", "الهوية الجديدة صارت هي الأصل.", Tier.LEGEND, cert = true)
        streak(365, "سنة كاملة", "عام كامل. أنت الدليل الحي أن التغيير ممكن.", Tier.LEGEND, cert = true)

        // ── مجموع الأيام النظيفة (لا ينقص أبدًا) ──
        total(30, "رصيد ثلاثين", "ثلاثون يومًا نظيفًا في حصالتك الدائمة.", Tier.BRONZE)
        total(100, "نادي المئة", "مئة يوم نظيف مجموعًا — رقم لا يُمحى.", Tier.SILVER)
        total(250, "ربع الألف", "مئتان وخمسون يومًا تراكمت لصالحك.", Tier.GOLD)
        total(500, "نصف الألف", "خمسمئة يوم نظيف. تأمل حجم هذا الرقم.", Tier.PLATINUM)
        total(1000, "الألف يوم", "ألف يوم نظيف. إنجاز يُروى.", Tier.LEGEND)

        // ── موجات الرغبة المركوبة ──
        urge(1, "أول موجة", "راقبتها حتى انكسرت ولم تُطعها. هذا هو التمرين كله.", Tier.BRONZE)
        urge(5, "خمس موجات", "دماغك بدأ يتعلم: الرغبة تمرّ دون طاعة.", Tier.SILVER)
        urge(15, "راكب أمواج", "خمس عشرة موجة انكسرت أمامك.", Tier.GOLD)
        urge(40, "سيد الموج", "أربعون موجة. الذروة لم تعد تخيفك.", Tier.PLATINUM)
        urge(100, "الشاطئ الثابت", "مئة موجة مرّت — وأنت باقٍ.", Tier.DIAMOND)

        // ── التمارين ──
        exercise(1, "أول تمرين", "جرّبت أداة قبل أن تحتاجها.", Tier.BRONZE)
        exercise(10, "عشرة تمارين", "الأدوات بدأت تصير انعكاسًا.", Tier.SILVER)
        exercise(30, "ثلاثون تمرينًا", "لديك الآن عدّة كاملة مجرَّبة.", Tier.GOLD)
        exercise(75, "مثابر", "خمسة وسبعون تمرينًا منجزًا.", Tier.PLATINUM)
        exercise(150, "محترف الأدوات", "مئة وخمسون جلسة تدريب ذهني.", Tier.DIAMOND)

        // ── المسار ──
        program(7, "أسبوع المسار", "سبعة أيام من برنامج التسعين.", Tier.BRONZE)
        program(30, "ثلث الطريق", "أنهيت مرحلتي الفهم والأدوات.", Tier.SILVER)
        program(60, "ثلثا الطريق", "أنهيت إعادة البناء.", Tier.GOLD)
        program(90, "خرّيج المسار", "تسعون يومًا من البرنامج كاملة.", Tier.DIAMOND, cert = true)

        // ── الأنشطة ──
        activity(5, "بدائل أولى", "خمسة أنشطة بديلة منفَّذة.", Tier.BRONZE)
        activity(20, "حياة تمتلئ", "عشرون نشاطًا ملأت الفراغ الخطر.", Tier.SILVER)
        activity(50, "بانٍ للحياة", "خمسون نشاطًا — السلوك استُبدل فعلًا.", Tier.GOLD)

        // ── الالتزام اليومي بفتح الجلسات ──
        commit(7, "أسبوع التزام", "سبعة أيام متتالية من الحضور.", Tier.BRONZE)
        commit(30, "شهر التزام", "ثلاثون يومًا متتالية لم تغب.", Tier.GOLD)
        commit(100, "حاضر دائمًا", "مئة يوم متتالية من الحضور.", Tier.DIAMOND)
    }

    private fun MutableList<Achievement>.streak(d: Int, t: String, s: String, tier: Tier, cert: Boolean = false) =
        add(Achievement("streak_$d", t, s, AchCategory.STREAK, d, tier, cert))
    private fun MutableList<Achievement>.total(d: Int, t: String, s: String, tier: Tier) =
        add(Achievement("total_$d", t, s, AchCategory.TOTAL, d, tier))
    private fun MutableList<Achievement>.urge(n: Int, t: String, s: String, tier: Tier) =
        add(Achievement("urge_$n", t, s, AchCategory.URGES, n, tier))
    private fun MutableList<Achievement>.exercise(n: Int, t: String, s: String, tier: Tier) =
        add(Achievement("ex_$n", t, s, AchCategory.EXERCISES, n, tier))
    private fun MutableList<Achievement>.program(n: Int, t: String, s: String, tier: Tier, cert: Boolean = false) =
        add(Achievement("prog_$n", t, s, AchCategory.PROGRAM, n, tier, cert))
    private fun MutableList<Achievement>.activity(n: Int, t: String, s: String, tier: Tier) =
        add(Achievement("act_$n", t, s, AchCategory.ACTIVITIES, n, tier))
    private fun MutableList<Achievement>.commit(n: Int, t: String, s: String, tier: Tier) =
        add(Achievement("commit_$n", t, s, AchCategory.COMMITMENT, n, tier))

    fun byId(id: String): Achievement? = all.firstOrNull { it.id == id }

    fun metric(a: Achievement, s: GamifyStats): Int = when (a.category) {
        AchCategory.STREAK -> s.maxBestStreak
        AchCategory.TOTAL -> s.totalCleanDays
        AchCategory.URGES -> s.urgesSurvived
        AchCategory.EXERCISES -> s.exercisesDone
        AchCategory.PROGRAM -> s.programDone
        AchCategory.ACTIVITIES -> s.activitiesDone
        AchCategory.COMMITMENT -> s.sessionStreak
    }

    fun earned(s: GamifyStats): List<Achievement> = all.filter { metric(it, s) >= it.threshold }

    /** الكأس القادمة في فئة السلسلة — لعرض حلقة التقدّم على الرئيسية. */
    fun nextStreakCup(currentStreak: Int): Achievement? =
        all.filter { it.category == AchCategory.STREAK }
            .sortedBy { it.threshold }
            .firstOrNull { it.threshold > currentStreak }

    fun lastStreakCup(currentStreak: Int): Achievement? =
        all.filter { it.category == AchCategory.STREAK && it.threshold <= currentStreak }
            .maxByOrNull { it.threshold }

    // ─────────── نظام النقاط والرتب ───────────

    fun xpOf(s: GamifyStats): Int =
        s.totalCleanDays * 10 +
            s.maxBestStreak * 5 +
            s.urgesSurvived * 15 +
            s.exercisesDone * 8 +
            s.programDone * 12 +
            s.activitiesDone * 6 +
            s.sessionStreak * 4

    val levels: List<Level> = listOf(
        Level(1, "عازم", 0),
        Level(2, "مجاهد", 200),
        Level(3, "صابر", 500),
        Level(4, "صامد", 1_000),
        Level(5, "متمكّن", 2_000),
        Level(6, "محارب", 3_500),
        Level(7, "منتصر", 5_500),
        Level(8, "قائد", 8_000),
        Level(9, "بطل", 11_500),
        Level(10, "أسطورة", 16_000)
    )

    fun levelFor(xp: Int): Level = levels.last { xp >= it.minXp }

    fun nextLevel(xp: Int): Level? = levels.firstOrNull { it.minXp > xp }

    /** تقدّم داخل الرتبة الحالية 0..1. */
    fun levelProgress(xp: Int): Float {
        val cur = levelFor(xp)
        val next = nextLevel(xp) ?: return 1f
        return ((xp - cur.minXp).toFloat() / (next.minXp - cur.minXp)).coerceIn(0f, 1f)
    }
}
