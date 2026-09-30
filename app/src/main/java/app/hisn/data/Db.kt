package app.hisn.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import app.hisn.domain.Capsule
import app.hisn.domain.DayCompletion
import app.hisn.domain.ExerciseSession
import app.hisn.domain.Habit
import app.hisn.domain.HabitKind
import app.hisn.domain.LibItem
import app.hisn.domain.LibType
import app.hisn.domain.MoodEntry
import app.hisn.domain.SlipKind
import app.hisn.domain.Place
import app.hisn.domain.Relapse
import app.hisn.domain.Trigger
import app.hisn.domain.UrgeRide
import java.time.Instant

/**
 * تخزين محلي بالكامل. لا خادم، ولا مزامنة، ولا حساب مستخدم.
 * سجل الانتكاسات والمحفزات بيانات شديدة الحساسية — تبقى على الجهاز وحده.
 */
class Db(context: Context) : SQLiteOpenHelper(context.applicationContext, NAME, null, VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE habits(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              kind TEXT NOT NULL,
              custom_name TEXT,
              streak_start INTEGER NOT NULL,
              banked_days INTEGER NOT NULL DEFAULT 0,
              longest_days INTEGER NOT NULL DEFAULT 0,
              relapse_count INTEGER NOT NULL DEFAULT 0,
              created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE relapses(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              habit_id INTEGER NOT NULL,
              at INTEGER NOT NULL,
              trigger_key TEXT NOT NULL,
              place_key TEXT NOT NULL,
              intensity INTEGER NOT NULL,
              note TEXT,
              streak_lost INTEGER NOT NULL DEFAULT 0,
              kind TEXT NOT NULL DEFAULT 'RELAPSE'
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE exercise_sessions(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              exercise_id TEXT NOT NULL,
              at INTEGER NOT NULL,
              before_level INTEGER NOT NULL,
              after_level INTEGER NOT NULL,
              completed INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE urge_rides(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              habit_id INTEGER NOT NULL,
              at INTEGER NOT NULL,
              peak INTEGER NOT NULL,
              ending INTEGER NOT NULL,
              seconds INTEGER NOT NULL,
              survived INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE day_completions(
              day INTEGER PRIMARY KEY,
              at INTEGER NOT NULL,
              reflection TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE unlocks(
              id TEXT PRIMARY KEY,
              at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE activity_log(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              activity_id TEXT NOT NULL,
              at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE library(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              title TEXT NOT NULL,
              type TEXT NOT NULL,
              uri TEXT,
              content TEXT,
              added_at INTEGER NOT NULL,
              last_page INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(CAPSULES_SQL)
        db.execSQL(MOODS_SQL)
        db.execSQL("CREATE INDEX idx_relapse_habit ON relapses(habit_id, at)")
        db.execSQL("CREATE INDEX idx_urge_habit ON urge_rides(habit_id, at)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // ما قبل نظام الكؤوس: لا ترحيل — إعادة إنشاء.
            db.execSQL("DROP TABLE IF EXISTS habits")
            db.execSQL("DROP TABLE IF EXISTS relapses")
            db.execSQL("DROP TABLE IF EXISTS exercise_sessions")
            db.execSQL("DROP TABLE IF EXISTS urge_rides")
            db.execSQL("DROP TABLE IF EXISTS day_completions")
            db.execSQL("DROP TABLE IF EXISTS unlocks")
            db.execSQL("DROP TABLE IF EXISTS activity_log")
            onCreate(db)
            return
        }
        if (oldVersion < 5) {
            // v4 → v5: يوميات المزاج.
            db.execSQL(MOODS_SQL)
        }
        if (oldVersion < 4) {
            // v3 → v4: كبسولات الزمن.
            db.execSQL(CAPSULES_SQL)
        }
        if (oldVersion < 3) {
            // v2 → v3: ترحيل حقيقي يحفظ بيانات المستخدم.
            db.execSQL("ALTER TABLE relapses ADD COLUMN kind TEXT NOT NULL DEFAULT 'RELAPSE'")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS library(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  title TEXT NOT NULL,
                  type TEXT NOT NULL,
                  uri TEXT,
                  content TEXT,
                  added_at INTEGER NOT NULL,
                  last_page INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

    // ---------- العادات ----------

    fun insertHabit(kind: HabitKind, customName: String?, start: Instant): Long {
        val v = ContentValues().apply {
            put("kind", kind.name)
            put("custom_name", customName)
            put("streak_start", start.toEpochMilli())
            put("banked_days", 0)
            put("longest_days", 0)
            put("relapse_count", 0)
            put("created_at", start.toEpochMilli())
        }
        return writableDatabase.insert("habits", null, v)
    }

    fun habits(): List<Habit> {
        val out = mutableListOf<Habit>()
        readableDatabase.query("habits", null, null, null, null, null, "created_at ASC")
            .use { c ->
                while (c.moveToNext()) out += c.toHabit()
            }
        return out
    }

    fun habit(id: Long): Habit? =
        readableDatabase.query("habits", null, "id=?", arrayOf(id.toString()), null, null, null)
            .use { c -> if (c.moveToFirst()) c.toHabit() else null }

    fun deleteHabit(id: Long) {
        writableDatabase.run {
            delete("relapses", "habit_id=?", arrayOf(id.toString()))
            delete("urge_rides", "habit_id=?", arrayOf(id.toString()))
            delete("habits", "id=?", arrayOf(id.toString()))
        }
    }

    /**
     * تسجيل انتكاسة. السلسلة الحالية تُضاف إلى الرصيد الدائم قبل تصفيرها،
     * والرقم القياسي يُحدَّث ثم يُحفظ — فلا يضيع شيء من التقدّم السابق.
     */
    fun recordRelapse(
        habitId: Long,
        at: Instant,
        trigger: Trigger,
        place: Place,
        intensity: Int,
        note: String?,
        kind: SlipKind = SlipKind.RELAPSE
    ) {
        val h = habit(habitId) ?: return
        val lost = if (kind == SlipKind.RELAPSE) h.currentStreakDays(at) else 0
        writableDatabase.beginTransaction()
        try {
            writableDatabase.insert("relapses", null, ContentValues().apply {
                put("habit_id", habitId)
                put("at", at.toEpochMilli())
                put("trigger_key", trigger.name)
                put("place_key", place.name)
                put("intensity", intensity)
                put("note", note)
                put("streak_lost", lost)
                put("kind", kind.name)
            })
            // الزلة المتدارَكة لا تكسر السلسلة — تُسجَّل كإشارة إنذار فقط.
            if (kind == SlipKind.RELAPSE) {
                writableDatabase.update("habits", ContentValues().apply {
                    put("banked_days", h.bankedCleanDays + lost)
                    put("longest_days", maxOf(h.longestStreakDays, lost))
                    put("relapse_count", h.relapseCount + 1)
                    put("streak_start", at.toEpochMilli())
                }, "id=?", arrayOf(habitId.toString()))
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    /** تعديل تاريخ البداية يدويًا (مثلًا عند تسجيل تاريخ سابق للإقلاع). */
    fun setStreakStart(habitId: Long, start: Instant) {
        writableDatabase.update(
            "habits",
            ContentValues().apply { put("streak_start", start.toEpochMilli()) },
            "id=?", arrayOf(habitId.toString())
        )
    }

    /**
     * تصحيح العدّاد يدويًا.
     *
     * المستخدم قد يكون بدأ قبل تثبيت التطبيق، أو نسي التسجيل، أو أخطأ.
     * إجباره على رقم لا يعكس واقعه يجعل العدّاد بلا معنى — فيتركه.
     * [banked] هو رصيد الأيام النظيفة السابقة، و[longest] الرقم القياسي المحفوظ.
     */
    fun setCounters(habitId: Long, start: Instant, banked: Int, longest: Int) {
        writableDatabase.update(
            "habits",
            ContentValues().apply {
                put("streak_start", start.toEpochMilli())
                put("banked_days", banked.coerceAtLeast(0))
                put("longest_days", longest.coerceAtLeast(0))
            },
            "id=?", arrayOf(habitId.toString())
        )
    }

    fun relapses(habitId: Long? = null, limit: Int = 200): List<Relapse> {
        val out = mutableListOf<Relapse>()
        val where = habitId?.let { "habit_id=?" }
        val args = habitId?.let { arrayOf(it.toString()) }
        readableDatabase.query(
            "relapses", null, where, args, null, null, "at DESC", limit.toString()
        ).use { c ->
            while (c.moveToNext()) {
                out += Relapse(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    habitId = c.getLong(c.getColumnIndexOrThrow("habit_id")),
                    at = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("at"))),
                    trigger = Trigger.from(c.getString(c.getColumnIndexOrThrow("trigger_key"))),
                    place = Place.from(c.getString(c.getColumnIndexOrThrow("place_key"))),
                    intensity = c.getInt(c.getColumnIndexOrThrow("intensity")),
                    note = c.getStringOrNull("note"),
                    streakLostDays = c.getInt(c.getColumnIndexOrThrow("streak_lost")),
                    kind = SlipKind.from(c.getStringOrNull("kind"))
                )
            }
        }
        return out
    }

    // ---------- التمارين ----------

    fun logExercise(exerciseId: String, before: Int, after: Int, at: Instant = Instant.now()) {
        writableDatabase.insert("exercise_sessions", null, ContentValues().apply {
            put("exercise_id", exerciseId)
            put("at", at.toEpochMilli())
            put("before_level", before)
            put("after_level", after)
            put("completed", 1)
        })
    }

    fun exerciseSessions(limit: Int = 300): List<ExerciseSession> {
        val out = mutableListOf<ExerciseSession>()
        readableDatabase.query(
            "exercise_sessions", null, null, null, null, null, "at DESC", limit.toString()
        ).use { c ->
            while (c.moveToNext()) {
                out += ExerciseSession(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    exerciseId = c.getString(c.getColumnIndexOrThrow("exercise_id")),
                    at = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("at"))),
                    before = c.getInt(c.getColumnIndexOrThrow("before_level")),
                    after = c.getInt(c.getColumnIndexOrThrow("after_level")),
                    completed = c.getInt(c.getColumnIndexOrThrow("completed")) == 1
                )
            }
        }
        return out
    }

    // ---------- موجات الرغبة ----------

    fun logUrgeRide(
        habitId: Long,
        peak: Int,
        ending: Int,
        seconds: Int,
        survived: Boolean,
        at: Instant = Instant.now()
    ) {
        writableDatabase.insert("urge_rides", null, ContentValues().apply {
            put("habit_id", habitId)
            put("at", at.toEpochMilli())
            put("peak", peak)
            put("ending", ending)
            put("seconds", seconds)
            put("survived", if (survived) 1 else 0)
        })
    }

    fun urgeRides(limit: Int = 300): List<UrgeRide> {
        val out = mutableListOf<UrgeRide>()
        readableDatabase.query(
            "urge_rides", null, null, null, null, null, "at DESC", limit.toString()
        ).use { c ->
            while (c.moveToNext()) {
                out += UrgeRide(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    habitId = c.getLong(c.getColumnIndexOrThrow("habit_id")),
                    at = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("at"))),
                    peakIntensity = c.getInt(c.getColumnIndexOrThrow("peak")),
                    endIntensity = c.getInt(c.getColumnIndexOrThrow("ending")),
                    secondsRidden = c.getInt(c.getColumnIndexOrThrow("seconds")),
                    survived = c.getInt(c.getColumnIndexOrThrow("survived")) == 1
                )
            }
        }
        return out
    }

    // ---------- مسار الـ90 يومًا ----------

    fun completeDay(day: Int, reflection: String?, at: Instant = Instant.now()) {
        writableDatabase.insertWithOnConflict(
            "day_completions", null,
            ContentValues().apply {
                put("day", day)
                put("at", at.toEpochMilli())
                put("reflection", reflection)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun completions(): List<DayCompletion> {
        val out = mutableListOf<DayCompletion>()
        readableDatabase.query("day_completions", null, null, null, null, null, "day ASC")
            .use { c ->
                while (c.moveToNext()) {
                    out += DayCompletion(
                        day = c.getInt(c.getColumnIndexOrThrow("day")),
                        at = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("at"))),
                        reflection = c.getStringOrNull("reflection")
                    )
                }
            }
        return out
    }

    // ---------- الكؤوس المفتوحة ----------

    fun unlockedIds(): Map<String, Instant> {
        val out = mutableMapOf<String, Instant>()
        readableDatabase.query("unlocks", null, null, null, null, null, null).use { c ->
            while (c.moveToNext()) {
                out[c.getString(c.getColumnIndexOrThrow("id"))] =
                    Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("at")))
            }
        }
        return out
    }

    fun addUnlocks(ids: Collection<String>, at: Instant = Instant.now()) {
        if (ids.isEmpty()) return
        writableDatabase.beginTransaction()
        try {
            ids.forEach { id ->
                writableDatabase.insertWithOnConflict(
                    "unlocks", null,
                    ContentValues().apply { put("id", id); put("at", at.toEpochMilli()) },
                    SQLiteDatabase.CONFLICT_IGNORE
                )
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    // ---------- سجل الأنشطة ----------

    fun logActivity(activityId: String, at: Instant = Instant.now()) {
        writableDatabase.insert("activity_log", null, ContentValues().apply {
            put("activity_id", activityId)
            put("at", at.toEpochMilli())
        })
    }

    /** كل المعرّفات المسجَّلة — لتحديد أيام التحدي المكتملة دون جدول إضافي. */
    fun activityIds(): Set<String> {
        val out = mutableSetOf<String>()
        readableDatabase.rawQuery("SELECT DISTINCT activity_id FROM activity_log", null)
            .use { c -> while (c.moveToNext()) out += c.getString(0) }
        return out
    }

    // ---------- كبسولات الزمن ----------

    fun addCapsule(body: String, unlockAt: Instant): Long =
        writableDatabase.insert("capsules", null, ContentValues().apply {
            put("body", body)
            put("created_at", Instant.now().toEpochMilli())
            put("unlock_at", unlockAt.toEpochMilli())
            put("opened", 0)
        })

    fun capsules(): List<Capsule> {
        val out = mutableListOf<Capsule>()
        readableDatabase.query("capsules", null, null, null, null, null, "unlock_at ASC")
            .use { c ->
                while (c.moveToNext()) {
                    out += Capsule(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        body = c.getString(c.getColumnIndexOrThrow("body")),
                        createdAt = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("created_at"))),
                        unlockAt = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("unlock_at"))),
                        opened = c.getInt(c.getColumnIndexOrThrow("opened")) == 1
                    )
                }
            }
        return out
    }

    fun openCapsule(id: Long) {
        writableDatabase.update(
            "capsules", ContentValues().apply { put("opened", 1) },
            "id=?", arrayOf(id.toString())
        )
    }

    fun deleteCapsule(id: Long) {
        writableDatabase.delete("capsules", "id=?", arrayOf(id.toString()))
    }

    // ---------- يوميات المزاج ----------

    /** تسجيل واحد لكل يوم — التسجيل الثاني في اليوم نفسه يستبدل الأول. */
    fun upsertMood(epochDay: Long, mood: Int, energy: Int, note: String?) {
        writableDatabase.insertWithOnConflict(
            "moods", null,
            ContentValues().apply {
                put("day", epochDay)
                put("at", Instant.now().toEpochMilli())
                put("mood", mood.coerceIn(1, 5))
                put("energy", energy.coerceIn(1, 5))
                put("note", note?.takeIf { it.isNotBlank() })
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun moods(limit: Int = 120): List<MoodEntry> {
        val out = mutableListOf<MoodEntry>()
        readableDatabase.query("moods", null, null, null, null, null, "day DESC", limit.toString())
            .use { c ->
                while (c.moveToNext()) {
                    out += MoodEntry(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        epochDay = c.getLong(c.getColumnIndexOrThrow("day")),
                        at = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("at"))),
                        mood = c.getInt(c.getColumnIndexOrThrow("mood")),
                        energy = c.getInt(c.getColumnIndexOrThrow("energy")),
                        note = c.getStringOrNull("note")
                    )
                }
            }
        return out
    }

    fun activityCount(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM activity_log", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }

    // ---------- المكتبة الشخصية ----------

    fun addLibraryItem(title: String, type: LibType, uri: String?, content: String?): Long =
        writableDatabase.insert("library", null, ContentValues().apply {
            put("title", title)
            put("type", type.name)
            put("uri", uri)
            put("content", content)
            put("added_at", Instant.now().toEpochMilli())
            put("last_page", 0)
        })

    fun libraryItems(): List<LibItem> {
        val out = mutableListOf<LibItem>()
        readableDatabase.query("library", null, null, null, null, null, "added_at DESC")
            .use { c ->
                while (c.moveToNext()) {
                    out += LibItem(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        type = runCatching {
                            LibType.valueOf(c.getString(c.getColumnIndexOrThrow("type")))
                        }.getOrDefault(LibType.TEXT),
                        uri = c.getStringOrNull("uri"),
                        content = c.getStringOrNull("content"),
                        addedAt = Instant.ofEpochMilli(c.getLong(c.getColumnIndexOrThrow("added_at"))),
                        lastPage = c.getInt(c.getColumnIndexOrThrow("last_page"))
                    )
                }
            }
        return out
    }

    fun setLibraryProgress(id: Long, page: Int) {
        writableDatabase.update(
            "library",
            ContentValues().apply { put("last_page", page.coerceAtLeast(0)) },
            "id=?", arrayOf(id.toString())
        )
    }

    fun deleteLibraryItem(id: Long) {
        writableDatabase.delete("library", "id=?", arrayOf(id.toString()))
    }

    fun wipeAll() {
        writableDatabase.run {
            delete("moods", null, null)
            delete("capsules", null, null)
            delete("library", null, null)
            delete("unlocks", null, null)
            delete("activity_log", null, null)
            delete("relapses", null, null)
            delete("urge_rides", null, null)
            delete("exercise_sessions", null, null)
            delete("day_completions", null, null)
            delete("habits", null, null)
        }
    }

    private fun Cursor.toHabit() = Habit(
        id = getLong(getColumnIndexOrThrow("id")),
        kind = HabitKind.from(getString(getColumnIndexOrThrow("kind"))),
        customName = getStringOrNull("custom_name"),
        streakStart = Instant.ofEpochMilli(getLong(getColumnIndexOrThrow("streak_start"))),
        bankedCleanDays = getInt(getColumnIndexOrThrow("banked_days")),
        longestStreakDays = getInt(getColumnIndexOrThrow("longest_days")),
        relapseCount = getInt(getColumnIndexOrThrow("relapse_count")),
        createdAt = Instant.ofEpochMilli(getLong(getColumnIndexOrThrow("created_at")))
    )

    private fun Cursor.getStringOrNull(col: String): String? {
        val i = getColumnIndexOrThrow(col)
        return if (isNull(i)) null else getString(i)
    }

    companion object {
        private const val CAPSULES_SQL = """
            CREATE TABLE IF NOT EXISTS capsules(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              body TEXT NOT NULL,
              created_at INTEGER NOT NULL,
              unlock_at INTEGER NOT NULL,
              opened INTEGER NOT NULL DEFAULT 0
            )
        """
        private const val MOODS_SQL = """
            CREATE TABLE IF NOT EXISTS moods(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              day INTEGER NOT NULL UNIQUE,
              at INTEGER NOT NULL,
              mood INTEGER NOT NULL,
              energy INTEGER NOT NULL,
              note TEXT
            )
        """

        /** الجداول التي تدخل في النسخة الاحتياطية، بترتيب الاستعادة. */
        val BACKUP_TABLES = listOf(
            "habits", "relapses", "exercise_sessions", "urge_rides", "day_completions",
            "unlocks", "activity_log", "library", "capsules", "moods"
        )

        private const val NAME = "hisn.db"
        private const val VERSION = 5
    }
}
