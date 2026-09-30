package app.hisn.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * نبرة المحتوى. المحرك العلاجي واحد في الحالتين — يتغيّر الإطار فقط.
 * لا خطاب تأنيب في أي وضع: الخجل من أقوى مشغّلات الانتكاسة.
 */
enum class ContentMode { ISLAMIC, SCIENTIFIC, BOTH }

/** نوع الإدمان أو العادة المتابَعة. القاعدة العلاجية مشتركة، والمحتوى يتخصص. */
enum class HabitKind(
    val label: String,
    val verb: String,
    val emoji: String
) {
    PORN("الإباحية", "المشاهدة", "🚫"),
    MASTURBATION("العادة السرية", "الممارسة", "🌱"),
    SMOKING("التدخين", "التدخين", "🚬"),
    VAPE("الفيب", "الاستخدام", "💨"),
    GAMING("الألعاب", "اللعب", "🎮"),
    SOCIAL("مواقع التواصل", "التصفّح", "📱"),
    DRUGS("المخدرات", "التعاطي", "⚠️"),
    ALCOHOL("الكحول", "الشرب", "🍷"),
    GAMBLING("المراهنات", "المراهنة", "🎲"),
    JUNK_FOOD("الإفراط في الطعام", "الإفراط", "🍔"),
    PROCRASTINATION("التأجيل", "التأجيل", "⏳"),
    CUSTOM("عادة أخرى", "الانزلاق", "🎯");

    companion object {
        fun from(name: String): HabitKind = entries.firstOrNull { it.name == name } ?: CUSTOM
    }
}

/**
 * عادة متابَعة، بنموذج القياس المزدوج.
 *
 * [bankedCleanDays] مجموع كل السلاسل المكتملة السابقة — لا ينقص أبدًا.
 * [longestStreakDays] الرقم القياسي — يُحفظ ولا يُمحى بعد الانتكاسة.
 * السلسلة الحالية وحدها هي ما يُصفَّر، وهي أصغر رقم في الواجهة لا أكبره.
 */
data class Habit(
    val id: Long = 0,
    val kind: HabitKind,
    val customName: String? = null,
    val streakStart: Instant,
    val bankedCleanDays: Int = 0,
    val longestStreakDays: Int = 0,
    val relapseCount: Int = 0,
    val createdAt: Instant = Instant.now()
) {
    val displayName: String get() = customName?.takeIf { it.isNotBlank() } ?: kind.label

    /** السلسلة الحالية بالأيام الكاملة. */
    fun currentStreakDays(now: Instant = Instant.now()): Int =
        Duration.between(streakStart, now).toDays().toInt().coerceAtLeast(0)

    /** الرقم الرئيسي في الواجهة: لا ينقص أبدًا مهما تكرّرت الانتكاسة. */
    fun totalCleanDays(now: Instant = Instant.now()): Int =
        bankedCleanDays + currentStreakDays(now)

    /** الرقم القياسي بعد احتساب السلسلة الجارية. */
    fun bestStreakDays(now: Instant = Instant.now()): Int =
        maxOf(longestStreakDays, currentStreakDays(now))

    fun currentStreakHours(now: Instant = Instant.now()): Long =
        Duration.between(streakStart, now).toHours().coerceAtLeast(0)

    /** يوم المستخدم داخل مسار الـ90 يومًا، محسوب من إنشاء العادة لا من آخر انتكاسة. */
    fun programDay(now: Instant = Instant.now()): Int =
        (Duration.between(createdAt, now).toDays().toInt() + 1).coerceIn(1, 90)
}

/** محفّز الانتكاسة — الأسئلة الثلاثة التي تُسأل بعد التسجيل. */
enum class Trigger(val label: String) {
    STRESS("ضغط أو توتر"),
    BOREDOM("ملل أو فراغ"),
    LONELINESS("وحدة"),
    FATIGUE("إرهاق أو قلة نوم"),
    ANGER("غضب أو إحباط"),
    SADNESS("حزن أو ضيق"),
    HABIT_CUE("إشارة معتادة (مكان/وقت)"),
    EXPOSURE("تعرّض مفاجئ لمحتوى"),
    CELEBRATION("مكافأة أو احتفال"),
    UNKNOWN("لا أعرف");

    companion object {
        fun from(name: String): Trigger = entries.firstOrNull { it.name == name } ?: UNKNOWN
    }
}

enum class Place(val label: String) {
    BEDROOM("غرفة النوم"),
    ALONE_HOME("وحدي في البيت"),
    PHONE_BED("الهاتف قبل النوم"),
    OUTSIDE("خارج البيت"),
    WORK_STUDY("العمل أو الدراسة"),
    WITH_FRIENDS("مع أصدقاء"),
    OTHER("مكان آخر");

    companion object {
        fun from(name: String): Place = entries.firstOrNull { it.name == name } ?: OTHER
    }
}

/**
 * نوع الحدث المسجَّل. التفريق بينهما ممارسة سريرية معتمدة:
 * الزلة القصيرة المتدارَكة فورًا لا تُعامل معاملة الانتكاسة الكاملة —
 * ومعاملتها كذلك هي بالضبط ما يحوّلها إلى واحدة (أثر انتهاك الامتناع).
 */
enum class SlipKind(val label: String) {
    LAPSE("زلة تداركتها"),
    RELAPSE("انتكاسة");

    companion object {
        fun from(name: String?): SlipKind =
            entries.firstOrNull { it.name == name } ?: RELAPSE
    }
}

/** سجل حدث. نقطة بيانات للتحليل — لا عقوبة ولا تقييم أخلاقي. */
data class Relapse(
    val id: Long = 0,
    val habitId: Long,
    val at: Instant,
    val trigger: Trigger,
    val place: Place,
    val intensity: Int,
    val note: String? = null,
    val streakLostDays: Int = 0,
    val kind: SlipKind = SlipKind.RELAPSE
) {
    fun hourOfDay(zone: ZoneId = ZoneId.systemDefault()): Int =
        at.atZone(zone).hour

    fun date(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        at.atZone(zone).toLocalDate()
}

/** جلسة تمرين ذهني منتهية، مع قياس الأثر قبل/بعد. */
data class ExerciseSession(
    val id: Long = 0,
    val exerciseId: String,
    val at: Instant,
    val before: Int,
    val after: Int,
    val completed: Boolean = true
) {
    val delta: Int get() = before - after
}

/** موجة رغبة تم تجاوزها دون انتكاسة — أهم مؤشر تقدّم في المنهج السلوكي. */
data class UrgeRide(
    val id: Long = 0,
    val habitId: Long,
    val at: Instant,
    val peakIntensity: Int,
    val endIntensity: Int,
    val secondsRidden: Int,
    val survived: Boolean
)

/** عنصر في المكتبة الشخصية: ملف PDF يضيفه المستخدم أو نص طويل يحفظه. */
enum class LibType { PDF, TEXT }

data class LibItem(
    val id: Long = 0,
    val title: String,
    val type: LibType,
    val uri: String? = null,
    val content: String? = null,
    val addedAt: Instant = Instant.now(),
    val lastPage: Int = 0
)

/**
 * كبسولة زمن: رسالة تكتبها نفسك الحاضرة لنفسك المستقبلية وتُختم حتى موعدها.
 * الأساس: بحوث «استمرارية الذات المستقبلية» — من يشعر بقربه من ذاته
 * المستقبلية يضبط اندفاعه أكثر.
 */
data class Capsule(
    val id: Long = 0,
    val body: String,
    val createdAt: Instant,
    val unlockAt: Instant,
    val opened: Boolean = false
) {
    fun isUnlockable(now: Instant = Instant.now()): Boolean = !now.isBefore(unlockAt)
}

/**
 * تسجيل مزاج يومي. المزاج والطاقة من 1 إلى 5.
 * الغرض ليس «كيف تشعر» فقط، بل رصد الارتباط بين المزاج المنخفض والزلات —
 * وهو من أقوى المؤشرات المسبقة للانزلاق في الأدبيات.
 */
data class MoodEntry(
    val id: Long = 0,
    val epochDay: Long,
    val at: Instant,
    val mood: Int,
    val energy: Int,
    val note: String? = null
) {
    val emoji: String get() = moodEmoji(mood)

    companion object {
        fun moodEmoji(m: Int) = when (m) {
            1 -> "😞"; 2 -> "😕"; 3 -> "😐"; 4 -> "🙂"; else -> "😄"
        }
        fun moodLabel(m: Int) = when (m) {
            1 -> "سيّئ"; 2 -> "منخفض"; 3 -> "عادي"; 4 -> "جيد"; else -> "ممتاز"
        }
    }
}

/** تكلفة العادة اليومية: مال ووقت. تُستخدم في حاسبة ما وفّرته. */
data class HabitCost(
    val costPerDay: Float = 0f,
    val minutesPerDay: Int = 0
) {
    val isSet: Boolean get() = costPerDay > 0f || minutesPerDay > 0
}

/** تسجيل إتمام يوم من مسار الـ90 يومًا. */
data class DayCompletion(
    val day: Int,
    val at: Instant,
    val reflection: String? = null
)
