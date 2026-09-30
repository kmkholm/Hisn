package app.hisn.data

import android.content.Context
import app.hisn.domain.ContentMode
import app.hisn.domain.HabitCost

/** إعدادات خفيفة. كل شيء محلي؛ لا شيء يُرسل إلى أي خادم. */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("hisn_prefs", Context.MODE_PRIVATE)

    var onboarded: Boolean
        get() = sp.getBoolean(K_ONBOARDED, false)
        set(v) = sp.edit().putBoolean(K_ONBOARDED, v).apply()

    var mode: ContentMode
        get() = runCatching { ContentMode.valueOf(sp.getString(K_MODE, null) ?: "BOTH") }
            .getOrDefault(ContentMode.BOTH)
        set(v) = sp.edit().putString(K_MODE, v.name).apply()

    /** الخطة البديلة التي يكتبها المستخدم وهو هادئ، لتُعرض عليه وقت الرغبة. */
    var copingPlan: String
        get() = sp.getString(K_PLAN, "") ?: ""
        set(v) = sp.edit().putString(K_PLAN, v).apply()

    /** لماذا بدأ أصلًا. أقوى من أي تذكير خارجي وقت الضعف. */
    var whyStatement: String
        get() = sp.getString(K_WHY, "") ?: ""
        set(v) = sp.edit().putString(K_WHY, v).apply()

    /** اسم اختياري يظهر على الشهادات فقط. يبقى محليًا ككل شيء. */
    var userName: String
        get() = sp.getString(K_NAME, "") ?: ""
        set(v) = sp.edit().putString(K_NAME, v).apply()

    /** آخر فصل وصل إليه في الرواية المدمجة. */
    var novellaChapter: Int
        get() = sp.getInt(K_NOVELLA, 0)
        set(v) = sp.edit().putInt(K_NOVELLA, v).apply()

    var reminderHour: Int
        get() = sp.getInt(K_REMINDER_HOUR, 21)
        set(v) = sp.edit().putInt(K_REMINDER_HOUR, v).apply()

    var remindersEnabled: Boolean
        get() = sp.getBoolean(K_REMINDERS, true)
        set(v) = sp.edit().putBoolean(K_REMINDERS, v).apply()

    /** آخر يوم فُتح فيه التطبيق، لحساب سلسلة الالتزام بالجلسات. */
    var lastOpenEpochDay: Long
        get() = sp.getLong(K_LAST_OPEN, 0L)
        set(v) = sp.edit().putLong(K_LAST_OPEN, v).apply()

    var sessionStreak: Int
        get() = sp.getInt(K_SESSION_STREAK, 0)
        set(v) = sp.edit().putInt(K_SESSION_STREAK, v).apply()

    // ---------- تكلفة العادة (حاسبة التوفير) ----------

    fun habitCost(habitId: Long): HabitCost = HabitCost(
        costPerDay = sp.getFloat("cost_$habitId", 0f),
        minutesPerDay = sp.getInt("minutes_$habitId", 0)
    )

    fun setHabitCost(habitId: Long, cost: HabitCost) {
        sp.edit()
            .putFloat("cost_$habitId", cost.costPerDay.coerceAtLeast(0f))
            .putInt("minutes_$habitId", cost.minutesPerDay.coerceAtLeast(0))
            .apply()
    }

    var currency: String
        get() = sp.getString(K_CURRENCY, "ريال") ?: "ريال"
        set(v) = sp.edit().putString(K_CURRENCY, v.ifBlank { "ريال" }).apply()

    // ---------- النسخ الاحتياطي ----------

    fun all(): Map<String, *> = sp.all

    /** استعادة كل المفاتيح من نسخة. الأنواع تُستنتج من القيمة. */
    fun restore(values: Map<String, Any?>) {
        val e = sp.edit().clear()
        values.forEach { (k, v) ->
            when (v) {
                is Boolean -> e.putBoolean(k, v)
                is Int -> e.putInt(k, v)
                is Long -> e.putLong(k, v)
                is Float -> e.putFloat(k, v)
                is Double -> e.putFloat(k, v.toFloat())
                is String -> e.putString(k, v)
                else -> Unit
            }
        }
        e.apply()
    }

    fun clear() = sp.edit().clear().apply()

    private companion object {
        const val K_ONBOARDED = "onboarded"
        const val K_MODE = "mode"
        const val K_PLAN = "coping_plan"
        const val K_WHY = "why"
        const val K_REMINDER_HOUR = "reminder_hour"
        const val K_REMINDERS = "reminders"
        const val K_LAST_OPEN = "last_open_day"
        const val K_SESSION_STREAK = "session_streak"
        const val K_NAME = "user_name"
        const val K_NOVELLA = "novella_chapter"
        const val K_CURRENCY = "currency"
    }
}
