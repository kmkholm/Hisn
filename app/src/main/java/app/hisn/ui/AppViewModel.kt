package app.hisn.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.hisn.content.Achievement
import app.hisn.content.Achievements
import app.hisn.content.GamifyStats
import app.hisn.content.Guidance
import app.hisn.content.GuidanceContext
import app.hisn.content.Tip
import app.hisn.BuildConfig
import app.hisn.data.Backup
import app.hisn.data.Db
import app.hisn.data.Prefs
import app.hisn.domain.HabitCost
import app.hisn.domain.MoodEntry
import app.hisn.widget.CounterWidget
import app.hisn.domain.ContentMode
import app.hisn.domain.DayCompletion
import app.hisn.domain.ExerciseSession
import app.hisn.domain.Habit
import app.hisn.domain.Capsule
import app.hisn.domain.HabitKind
import app.hisn.domain.LibItem
import app.hisn.domain.LibType
import app.hisn.domain.SlipKind
import app.hisn.domain.Place
import app.hisn.domain.Relapse
import app.hisn.domain.Trigger
import app.hisn.domain.UrgeRide
import app.hisn.work.Reminders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class UiState(
    val loading: Boolean = true,
    val onboarded: Boolean = false,
    val mode: ContentMode = ContentMode.BOTH,
    val habits: List<Habit> = emptyList(),
    val selectedHabitId: Long? = null,
    val relapses: List<Relapse> = emptyList(),
    val sessions: List<ExerciseSession> = emptyList(),
    val rides: List<UrgeRide> = emptyList(),
    val completions: List<DayCompletion> = emptyList(),
    val copingPlan: String = "",
    val whyStatement: String = "",
    val userName: String = "",
    val sessionStreak: Int = 0,
    val activityCount: Int = 0,
    val libraryItems: List<LibItem> = emptyList(),
    val novellaChapter: Int = 0,
    val activityIds: Set<String> = emptySet(),
    val capsules: List<Capsule> = emptyList(),
    val moods: List<MoodEntry> = emptyList(),
    val costs: Map<Long, HabitCost> = emptyMap(),
    val currency: String = "ريال",
    val unlocked: Map<String, Instant> = emptyMap(),
    val newUnlocks: List<Achievement> = emptyList(),
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 21,
    val tip: Tip? = null
) {
    val selected: Habit? get() = habits.firstOrNull { it.id == selectedHabitId } ?: habits.firstOrNull()

    /** أكثر ساعة يتكرر فيها الانزلاق — أساس التنبيه الاستباقي. */
    val riskiestHour: Int?
        get() = relapses.takeIf { it.size >= 3 }
            ?.groupingBy { it.hourOfDay() }?.eachCount()
            ?.maxByOrNull { it.value }?.key

    val topTrigger: Trigger?
        get() = relapses.takeIf { it.isNotEmpty() }
            ?.groupingBy { it.trigger }?.eachCount()
            ?.maxByOrNull { it.value }?.key

    val urgesSurvived: Int get() = rides.count { it.survived }

    val programDay: Int get() = selected?.programDay() ?: 1

    val daysSinceLastExercise: Int
        get() = sessions.firstOrNull()
            ?.let { Duration.between(it.at, Instant.now()).toDays().toInt() } ?: -1

    /** متوسط انخفاض الشدة بعد التمارين — دليل ملموس على أن الأداة تعمل. */
    val avgExerciseDrop: Double?
        get() = sessions.filter { it.completed }.takeIf { it.isNotEmpty() }
            ?.map { it.delta }?.average()

    fun completed(day: Int): Boolean = completions.any { it.day == day }

    /** لقطة إحصاءات نظام الكؤوس — كلها تراكمية. */
    val gamifyStats: GamifyStats
        get() = GamifyStats(
            maxBestStreak = habits.maxOfOrNull { it.bestStreakDays() } ?: 0,
            totalCleanDays = habits.sumOf { it.totalCleanDays() },
            urgesSurvived = urgesSurvived,
            exercisesDone = sessions.size,
            programDone = completions.size,
            activitiesDone = activityCount,
            sessionStreak = sessionStreak
        )

    val xp: Int get() = Achievements.xpOf(gamifyStats)
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = Db(app)
    private val prefs = Prefs(app)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        touchSessionStreak()
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val snapshot = withContext(Dispatchers.IO) {
            val habits = db.habits()
            val base = UiState(
                loading = false,
                onboarded = prefs.onboarded,
                mode = prefs.mode,
                habits = habits,
                selectedHabitId = _state.value.selectedHabitId ?: habits.firstOrNull()?.id,
                relapses = db.relapses(),
                sessions = db.exerciseSessions(),
                rides = db.urgeRides(),
                completions = db.completions(),
                copingPlan = prefs.copingPlan,
                whyStatement = prefs.whyStatement,
                userName = prefs.userName,
                sessionStreak = prefs.sessionStreak,
                activityCount = db.activityCount(),
                libraryItems = db.libraryItems(),
                novellaChapter = prefs.novellaChapter,
                activityIds = db.activityIds(),
                capsules = db.capsules(),
                moods = db.moods(),
                costs = habits.associate { it.id to prefs.habitCost(it.id) },
                currency = prefs.currency,
                remindersEnabled = prefs.remindersEnabled,
                reminderHour = prefs.reminderHour,
                newUnlocks = _state.value.newUnlocks
            )

            // ── تقييم الكؤوس: ما استُحق ولم يُسجَّل بعد يُفتح الآن ──
            val stored = db.unlockedIds()
            val earnedNow = Achievements.earned(base.gamifyStats)
            val fresh = earnedNow.filter { it.id !in stored }
            if (fresh.isNotEmpty()) db.addUnlocks(fresh.map { it.id })

            base.copy(
                unlocked = stored + fresh.associate { it.id to Instant.now() },
                newUnlocks = (base.newUnlocks + fresh).distinctBy { it.id }
            )
        }
        _state.value = snapshot.copy(tip = buildTip(snapshot))
        // ودجت الشاشة الرئيسية يعكس الحالة نفسها
        withContext(Dispatchers.IO) {
            runCatching { CounterWidget.refreshAll(getApplication()) }
        }
    }

    // ---------- يوميات المزاج ----------

    fun logMood(mood: Int, energy: Int, note: String?) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.upsertMood(LocalDate.now().toEpochDay(), mood, energy, note)
        }
        refresh()
    }

    // ---------- حاسبة التوفير ----------

    fun setHabitCost(habitId: Long, cost: HabitCost, currency: String) {
        prefs.setHabitCost(habitId, cost)
        prefs.currency = currency
        _state.value = _state.value.copy(
            costs = _state.value.costs + (habitId to cost),
            currency = prefs.currency
        )
    }

    // ---------- النسخ الاحتياطي ----------

    fun exportBackup(onResult: (String?) -> Unit) = viewModelScope.launch {
        val json = withContext(Dispatchers.IO) {
            runCatching {
                Backup.export(db.readableDatabase, prefs.all(), BuildConfig.VERSION_NAME)
            }.getOrNull()
        }
        onResult(json)
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            runCatching {
                val p = Backup.import(db.writableDatabase, json)
                prefs.restore(p)
                true
            }.getOrDefault(false)
        }
        if (ok) {
            val app = getApplication<Application>()
            if (prefs.onboarded && prefs.remindersEnabled) Reminders.schedule(app, prefs.reminderHour)
            else Reminders.cancel(app)
            _state.value = _state.value.copy(selectedHabitId = null)
            refresh()
        }
        onResult(ok)
    }

    /** بعد عرض احتفال الفتح. */
    fun consumeUnlocks() {
        _state.value = _state.value.copy(newUnlocks = emptyList())
    }

    private fun buildTip(s: UiState): Tip {
        val h = s.selected
        return Guidance.forContext(
            GuidanceContext(
                currentStreakDays = h?.currentStreakDays() ?: 0,
                totalCleanDays = h?.totalCleanDays() ?: 0,
                hourOfDay = Instant.now().atZone(ZoneId.systemDefault()).hour,
                recentRelapses = s.relapses,
                riskiestHour = s.riskiestHour,
                topTrigger = s.topTrigger,
                daysSinceLastExercise = s.daysSinceLastExercise,
                programDay = s.programDay
            )
        )
    }

    private fun touchSessionStreak() {
        val today = LocalDate.now().toEpochDay()
        val last = prefs.lastOpenEpochDay
        if (last != today) {
            prefs.sessionStreak = if (last == today - 1) prefs.sessionStreak + 1 else 1
            prefs.lastOpenEpochDay = today
        }
    }

    // ---------- إجراءات ----------

    fun addHabit(kind: HabitKind, customName: String?, daysAlreadyClean: Int = 0) =
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val start = Instant.now().minus(Duration.ofDays(daysAlreadyClean.toLong()))
                db.insertHabit(kind, customName, start)
            }
            refresh()
        }

    fun deleteHabit(id: Long) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.deleteHabit(id) }
        if (_state.value.selectedHabitId == id) {
            _state.value = _state.value.copy(selectedHabitId = null)
        }
        refresh()
    }

    /**
     * تصحيح العدّاد يدويًا: تاريخ البداية، ورصيد الأيام السابقة، والرقم القياسي.
     * [startEpochDay] يوم التقويم الذي بدأت فيه السلسلة الحالية.
     */
    fun editCounters(habitId: Long, startEpochDay: Long, banked: Int, longest: Int) =
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val start = LocalDate.ofEpochDay(startEpochDay)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()
                    .coerceAtMost(Instant.now())
                db.setCounters(habitId, start, banked, longest)
            }
            refresh()
        }

    fun select(id: Long) {
        _state.value = _state.value.copy(selectedHabitId = id)
        viewModelScope.launch { refresh() }
    }

    fun recordRelapse(
        habitId: Long,
        trigger: Trigger,
        place: Place,
        intensity: Int,
        note: String?,
        kind: SlipKind
    ) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            db.recordRelapse(habitId, Instant.now(), trigger, place, intensity, note, kind)
        }
        refresh()
    }

    // ---------- المكتبة الشخصية ----------

    fun addLibraryPdf(title: String, uri: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.addLibraryItem(title, LibType.PDF, uri, null) }
        refresh()
    }

    fun addLibraryText(title: String, content: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.addLibraryItem(title, LibType.TEXT, null, content) }
        refresh()
    }

    fun deleteLibraryItem(id: Long) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.deleteLibraryItem(id) }
        refresh()
    }

    // ---------- كبسولات الزمن ----------

    fun addCapsule(body: String, unlockAt: Instant) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.addCapsule(body, unlockAt) }
        refresh()
    }

    fun openCapsule(id: Long) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.openCapsule(id) }
        refresh()
    }

    fun deleteCapsule(id: Long) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.deleteCapsule(id) }
        refresh()
    }

    fun setNovellaChapter(chapter: Int) {
        prefs.novellaChapter = chapter.coerceAtLeast(prefs.novellaChapter)
        _state.value = _state.value.copy(novellaChapter = prefs.novellaChapter)
    }

    fun setLibraryProgress(id: Long, page: Int) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.setLibraryProgress(id, page) }
        // بلا refresh — يُستدعى أثناء التقليب ولا يغيّر الواجهة الحالية.
    }

    fun logExercise(exerciseId: String, before: Int, after: Int) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.logExercise(exerciseId, before, after) }
        refresh()
    }

    fun logUrgeRide(peak: Int, ending: Int, seconds: Int, survived: Boolean) =
        viewModelScope.launch {
            val id = _state.value.selected?.id ?: return@launch
            withContext(Dispatchers.IO) { db.logUrgeRide(id, peak, ending, seconds, survived) }
            refresh()
        }

    fun completeDay(day: Int, reflection: String?) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.completeDay(day, reflection) }
        refresh()
    }

    fun logActivity(activityId: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.logActivity(activityId) }
        refresh()
    }

    fun setMode(mode: ContentMode) {
        prefs.mode = mode
        _state.value = _state.value.copy(mode = mode)
    }

    fun setCopingPlan(text: String) {
        prefs.copingPlan = text
        _state.value = _state.value.copy(copingPlan = text)
    }

    fun setWhy(text: String) {
        prefs.whyStatement = text
        _state.value = _state.value.copy(whyStatement = text)
    }

    fun setUserName(name: String) {
        prefs.userName = name.trim()
        _state.value = _state.value.copy(userName = prefs.userName)
    }

    /** تفعيل/تعطيل التذكير اليومي وجدولته عبر WorkManager. */
    fun setReminder(enabled: Boolean, hour: Int) {
        prefs.remindersEnabled = enabled
        prefs.reminderHour = hour
        _state.value = _state.value.copy(remindersEnabled = enabled, reminderHour = hour)
        val app = getApplication<Application>()
        if (enabled) Reminders.schedule(app, hour) else Reminders.cancel(app)
    }

    /**
     * إنهاء التهيئة في عملية واحدة متسلسلة.
     * الإدراج غير المتزامن المنفصل كان يسابق التحديث فتظهر الرئيسية بلا عادة.
     */
    fun completeOnboarding(
        habits: List<Pair<HabitKind, Int>>,
        mode: ContentMode,
        why: String
    ) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            habits.forEach { (kind, days) ->
                val start = Instant.now().minus(Duration.ofDays(days.toLong()))
                db.insertHabit(kind, null, start)
            }
            prefs.mode = mode
            if (why.isNotBlank()) prefs.whyStatement = why
            prefs.onboarded = true
        }
        val app = getApplication<Application>()
        if (prefs.remindersEnabled) Reminders.schedule(app, prefs.reminderHour)
        refresh()
    }

    fun finishOnboarding() {
        prefs.onboarded = true
        _state.value = _state.value.copy(onboarded = true)
        val app = getApplication<Application>()
        if (prefs.remindersEnabled) Reminders.schedule(app, prefs.reminderHour)
        viewModelScope.launch { refresh() }
    }

    fun wipeEverything() = viewModelScope.launch {
        withContext(Dispatchers.IO) { db.wipeAll() }
        prefs.clear()
        Reminders.cancel(getApplication())
        _state.value = UiState(loading = false, onboarded = false)
    }
}
