package app.hisn.content

import app.hisn.domain.Relapse
import app.hisn.domain.SlipKind
import app.hisn.ui.dayWord
import app.hisn.domain.Trigger

/**
 * محرّك الإرشاد.
 *
 * ليس اقتباسات عشوائية. كل نصيحة مشروطة بحالة المستخدم الفعلية:
 * يومه في المسار، الساعة، نمط محفّزاته، وقربه من نافذته الخطرة.
 * النصيحة التي تصل في وقتها تُقرأ؛ والعامة تُتجاهل.
 */
data class Tip(
    val text: String,
    val why: String? = null,
    val exerciseId: String? = null,
    val priority: Int = 0
)

data class GuidanceContext(
    val currentStreakDays: Int,
    val totalCleanDays: Int,
    val hourOfDay: Int,
    val recentRelapses: List<Relapse>,
    val riskiestHour: Int?,
    val topTrigger: Trigger?,
    val daysSinceLastExercise: Int,
    val programDay: Int
)

object Guidance {

    fun forContext(c: GuidanceContext): Tip = candidates(c).maxByOrNull { it.priority } ?: DEFAULT

    private fun candidates(c: GuidanceContext): List<Tip> {
        val out = mutableListOf<Tip>()

        // ── أولوية قصوى: أنت داخل نافذتك الخطرة الآن ──
        if (c.riskiestHour != null && withinTwoHours(c.hourOfDay, c.riskiestHour)) {
            out += Tip(
                "أنت داخل نافذتك الأخطر الآن — بياناتك تقول إن معظم انزلاقك يقع قرب الساعة ${fmtHour(c.riskiestHour)}.",
                "الاستعداد قبل الموجة أسهل من مقاومتها أثناءها.",
                "if_then",
                priority = 100
            )
        }

        // ── ساعات متأخرة: أكثر الأوقات خطرًا إحصائيًا ──
        if (c.hourOfDay >= 23 || c.hourOfDay <= 3) {
            out += Tip(
                "الساعة متأخرة. الإرهاق يضعف الكبح أكثر مما تضعفه الرغبة نفسها.",
                "قلة النوم ترفع الاندفاعية قياسًا — النوم الآن قرار علاجي لا راحة.",
                "breath478",
                priority = 85
            )
        }

        // ── بعد زلة قريبة (لم تكسر السلسلة) ──
        val lastLapse = c.recentRelapses.firstOrNull { it.kind == SlipKind.LAPSE }
        val hoursSinceLapse = lastLapse?.let {
            java.time.Duration.between(it.at, java.time.Instant.now()).toHours()
        }
        if (hoursSinceLapse != null && hoursSinceLapse < 24) {
            out += Tip(
                "سجّلت زلة وتداركتها — هذا وعي لا فشل. سلسلتك مستمرة.",
                "الحذر الآن: الزلة الواحدة تفتح شهية التكرار في نفس اليوم. ابتعد عن السياق ساعة.",
                "urge_surf",
                priority = 88
            )
        }

        // ── بعد انتكاسة قريبة ──
        val hoursSinceRelapse = c.recentRelapses.firstOrNull { it.kind == SlipKind.RELAPSE }?.let {
            java.time.Duration.between(it.at, java.time.Instant.now()).toHours()
        }
        if (hoursSinceRelapse != null && hoursSinceRelapse < 24) {
            out += Tip(
                "أمس لا يلغي ما قبله. مجموع أيامك النظيفة ورقمك القياسي لم يتغيّرا.",
                "الخجل يرفع احتمال التكرار لا يخفضه — وهذا ليس تهوينًا بل نتيجة متكررة.",
                "self_compassion",
                priority = 95
            )
        } else if (hoursSinceRelapse != null && hoursSinceRelapse in 24..72) {
            out += Tip(
                "الآن وقت التحليل لا اللوم. ماذا حدث في الساعات التي سبقت؟",
                "الانتكاسة المُحلَّلة تخفض احتمال التالية؛ غير المُحلَّلة تكرّرها.",
                "chain",
                priority = 80
            )
        }

        // ── نمط محفّز غالب ──
        c.topTrigger?.let { t ->
            triggerTip(t)?.let { out += it.copy(priority = 70) }
        }

        // ── مراحل زمنية ──
        when {
            c.currentStreakDays == 0 -> out += Tip(
                "اليوم الأول ليس عودة إلى الصفر — أنت تحمل كل ما تعلّمته.",
                null, "values", 60
            )
            c.currentStreakDays in 1..3 -> out += Tip(
                "الأيام الثلاثة الأولى هي الأصعب فسيولوجيًا. التقلّب الآن علامة تكيّف لا فشل.",
                "أعراض التكيّف تخفّ تدريجيًا خلال الأسبوع الأول.",
                "urge_surf", 55
            )
            c.currentStreakDays in 4..7 -> out += Tip(
                "الأسبوع الأول: ركّز على النوم والحركة قبل أي شيء آخر.",
                "هما أسرع رافعتين تخفضان عدد لحظات الضعف يوميًا.",
                null, 50
            )
            c.currentStreakDays in 8..20 -> out += Tip(
                "المرحلة الآن تحتاج أدوات لا عزيمة. اختر أداة واحدة وأتقنها.",
                null, "if_then", 45
            )
            c.currentStreakDays in 21..45 -> out += Tip(
                "بدأت حساسية المكافأة تتحسّن: أشياء عادية تعود لتُشعرك بشيء. لاحظها.",
                "هذه أول علامة تعافٍ ملموسة عند أغلب الناس.",
                null, 45
            )
            c.currentStreakDays > 45 -> out += Tip(
                "الخطر الآن ليس الرغبة بل الثقة الزائدة. معظم الانتكاسات المتأخرة سببها ترك الأدوات.",
                null, "if_then", 50
            )
        }

        // ── انقطاع عن التمارين ──
        if (c.daysSinceLastExercise >= 3) {
            out += Tip(
                "مرّت ${dayWord(c.daysSinceLastExercise)} بلا تمرين. الأدوات تضعف بالإهمال لا بالوقت.",
                null, null, 65
            )
        }

        return out
    }

    private fun triggerTip(t: Trigger): Tip? = when (t) {
        Trigger.STRESS -> Tip(
            "الضغط هو محفّزك الأول. عالج التوتر مباشرة بدل انتظار أن يتحوّل إلى رغبة.",
            "التوتر من أكثر محفّزات الانتكاسة شيوعًا عبر كل أنواع الإدمان.",
            "breath478"
        )
        Trigger.BOREDOM -> Tip(
            "الملل هو محفّزك الأول. الفراغ غير المنظّم أخطر عليك من الضغط.",
            "الحل ليس مقاومة الملل بل ملء الوقت الخطر مسبقًا.",
            "replace"
        )
        Trigger.LONELINESS -> Tip(
            "الوحدة هي محفّزك الأول. ساعة مع إنسان حقيقي أنفع من ساعة مقاومة.",
            "العزلة من أقوى عوامل الخطر، والاتصال من أقوى عوامل الحماية.",
            null
        )
        Trigger.FATIGUE -> Tip(
            "الإرهاق هو محفّزك الأول. النوم عندك ليس رفاهية بل الأداة الأولى.",
            "قلة النوم تضعف القشرة الجبهية — مركز الكبح.",
            null
        )
        Trigger.ANGER -> Tip(
            "الغضب هو محفّزك الأول. تحتاج منفّسًا جاهزًا قبل أن يأتي.",
            "الغضب غير المعالَج يبحث عن تفريغ، فيجد السلوك القديم.",
            "grounding"
        )
        Trigger.SADNESS -> Tip(
            "الحزن هو محفّزك الأول. المطلوب تخفيف الألم لا تجاهله.",
            null, "self_compassion"
        )
        Trigger.HABIT_CUE -> Tip(
            "محفّزك إشارة معتادة — مكان أو وقت. غيّر السياق تغيّر السلوك.",
            "السياق يقود السلوك أكثر مما تقود النية.",
            "if_then"
        )
        Trigger.EXPOSURE -> Tip(
            "التعرّض المفاجئ هو محفّزك الأول. قلّل احتمال اللقاء قبل أن تحتاج مقاومته.",
            null, null
        )
        else -> null
    }

    private fun withinTwoHours(now: Int, target: Int): Boolean {
        val d = kotlin.math.abs(now - target)
        return minOf(d, 24 - d) <= 2
    }

    fun fmtHour(h: Int): String = when {
        h == 0 -> "١٢ منتصف الليل"
        h < 12 -> "$h صباحًا"
        h == 12 -> "١٢ ظهرًا"
        else -> "${h - 12} مساءً"
    }

    private val DEFAULT = Tip(
        "أصغر خطوة اليوم أفضل من خطة مثالية غدًا.",
        null, null, 0
    )
}
