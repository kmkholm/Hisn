package app.hisn.content

import kotlin.random.Random

/**
 * مكتبة الأنشطة البديلة.
 *
 * الأساس العلاجي: التنشيط السلوكي. السلوك لا يُحذف بل يُستبدل — والفراغ الذي
 * يتركه هو أخطر ما في التعافي. كل نشاط هنا محدد وقابل للتنفيذ خلال دقائق،
 * لأن البديل يجب أن يكون أسهل من السلوك القديم في اللحظة، لا أفضل منه نظريًا.
 */
enum class ActCat(val label: String, val emoji: String) {
    MOVE("حركة", "🏃"),
    SOCIAL("اجتماعي", "🤝"),
    MIND("ذهني", "🧠"),
    SPIRIT("روحي", "🕌"),
    SKILL("مهارة", "🛠️"),
    HOME("عملي", "🏠")
}

data class Activity(
    val id: String,
    val title: String,
    val cat: ActCat,
    val minutes: Int,
    val benefit: String
)

object ActivitiesLib {

    val all: List<Activity> = listOf(
        // ── حركة ──
        Activity("walk20", "مشي سريع في الخارج", ActCat.MOVE, 20, "يكسر السياق ويحرق التوتر — أسرع بديل مثبت"),
        Activity("pushups", "تمرين ضغط وسكوات حتى التعب", ActCat.MOVE, 10, "يفرّغ شحنة الطاقة التي تغذّي الرغبة"),
        Activity("coldshower", "دشّ بارد", ActCat.MOVE, 5, "صدمة حسية تقطع الشريط الذهني فورًا"),
        Activity("jumprope", "حبل قفز أو جري في المكان", ActCat.MOVE, 7, "يرفع النبض فيزيح التوتر المتراكم"),
        Activity("stretch", "تمدد وإطالة للجسد كله", ActCat.MOVE, 8, "يفكّ شدّ العضلات الذي يصاحب الضغط"),
        Activity("bike", "دراجة أو صعود درج", ActCat.MOVE, 15, "مجهود متوسط يعيد ضبط المزاج"),

        // ── اجتماعي ──
        Activity("callfriend", "مكالمة صوتية مع صديق", ActCat.SOCIAL, 15, "الاتصال البشري أقوى عوامل الحماية من العزلة"),
        Activity("familysit", "اجلس مع أهلك في نفس الغرفة", ActCat.SOCIAL, 20, "الوجود مع الناس يقطع سياق العزلة الخطر"),
        Activity("helpsomeone", "ساعد شخصًا في شيء الآن", ActCat.SOCIAL, 15, "تحويل التركيز من الذات مسرّع معروف للتعافي"),
        Activity("visitrelative", "زيارة أو رسالة صوتية لقريب", ActCat.SOCIAL, 10, "يبني الشبكة التي تسندك في الأيام الصعبة"),
        Activity("groupprayer", "صلاة جماعة في المسجد", ActCat.SOCIAL, 25, "خروج من البيت واتصال بشري ومعنى — ثلاثة في واحد"),

        // ── ذهني ──
        Activity("read20", "اقرأ 15 صفحة من كتاب", ActCat.MIND, 25, "يشغل الذهن بعمق لا يتيح مساحة للشريط القديم"),
        Activity("podcast", "حلقة بودكاست في موضوع يهمك", ActCat.MIND, 30, "صوت يملأ الرأس ويطرد الفراغ"),
        Activity("puzzle", "لغز أو شطرنج أو سودوكو", ActCat.MIND, 15, "التركيز العميق يستهلك نفس الانتباه الذي تطلبه الرغبة"),
        Activity("language", "تعلّم 10 كلمات من لغة جديدة", ActCat.MIND, 15, "تقدّم صغير ملموس يرفع الكفاءة الذاتية"),
        Activity("journal", "اكتب صفحة عمّا في رأسك", ActCat.MIND, 10, "التفريغ الكتابي يخفض شحنة الأفكار الملحّة"),
        Activity("plan", "خطط ليومك القادم في ثلاث نقاط", ActCat.MIND, 5, "الوقت المهيكل أخطر عدو للفراغ"),

        // ── روحي ──
        Activity("quran", "ورد قرآن — ولو صفحة واحدة", ActCat.SPIRIT, 10, "سكينة مباشرة وارتباط بالمعنى الأكبر"),
        Activity("dhikr", "أذكار أو استغفار مئة مرة", ActCat.SPIRIT, 7, "إيقاع متكرر يهدّئ الجهاز العصبي ويشغل اللسان والقلب"),
        Activity("prayer2", "ركعتان بحضور قلب", ActCat.SPIRIT, 10, "انتقال جسدي وذهني كامل من السياق الخطر"),
        Activity("gratitude", "اكتب ٣ نعم أنت فيها الآن", ActCat.SPIRIT, 5, "الامتنان يخفض الاندفاع نحو التعويض السريع"),
        Activity("meditate", "جلوس صامت وملاحظة التنفس", ActCat.SPIRIT, 10, "تدريب مباشر على الملاحظة دون استجابة"),

        // ── مهارة ──
        Activity("skill20", "20 دقيقة في مهارتك التي تبنيها", ActCat.SKILL, 20, "يعيد توجيه دوائر المكافأة نحو إنجاز حقيقي"),
        Activity("cook", "اطبخ شيئًا بسيطًا بنفسك", ActCat.SKILL, 30, "عمل باليدين ونتيجة تُؤكل — مكافأة طبيعية كاملة"),
        Activity("project", "خطوة واحدة في مشروعك الصغير", ActCat.SKILL, 25, "التقدّم في شيء تملكه يغذّي الهوية الجديدة"),
        Activity("draw", "ارسم أو اكتب شيئًا إبداعيًا", ActCat.SKILL, 15, "الإبداع يصرّف الشحنة العاطفية في قناة نظيفة"),
        Activity("fixthing", "أصلح شيئًا مكسورًا في البيت", ActCat.SKILL, 20, "إنجاز ملموس يرفع الشعور بالسيطرة"),

        // ── عملي ──
        Activity("tidyroom", "رتّب غرفتك أو مكتبك", ActCat.HOME, 15, "ترتيب المكان يرتّب الرأس — والحركة تكسر الجمود"),
        Activity("shower", "اغتسل وغيّر ملابسك", ActCat.HOME, 10, "إعادة تشغيل حسية كاملة للحالة الذهنية"),
        Activity("preptomorrow", "جهّز أغراض الغد الآن", ActCat.HOME, 10, "يقلل قرارات الغد ويعطي شعور التحكم"),
        Activity("declutter", "تخلّص من 10 أشياء لا تحتاجها", ActCat.HOME, 15, "حسم سريع متكرر يبني عضلة اتخاذ القرار"),
        Activity("waterplants", "اسقِ نبتة أو اعتنِ بشيء حي", ActCat.HOME, 5, "العناية بكائن حي تخرجك من الانشغال بالذات")
    )

    fun byId(id: String): Activity? = all.firstOrNull { it.id == id }

    fun byCat(cat: ActCat?): List<Activity> =
        if (cat == null) all else all.filter { it.cat == cat }

    /** اقتراح عشوائي، مع مراعاة الوقت المتاح إن حُدد. */
    fun suggest(maxMinutes: Int? = null, seed: Long = System.nanoTime()): Activity {
        val pool = all.filter { maxMinutes == null || it.minutes <= maxMinutes }
            .ifEmpty { all }
        return pool[Random(seed).nextInt(pool.size)]
    }
}
