package app.hisn

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.hisn.content.Achievements
import app.hisn.content.Exercises
import app.hisn.content.Stories
import app.hisn.data.Prefs
import app.hisn.ui.AppViewModel
import app.hisn.ui.ConfettiOverlay
import app.hisn.ui.screens.CertificateScreen
import app.hisn.ui.screens.DayDetailScreen
import app.hisn.ui.screens.DhikrScreen
import app.hisn.ui.screens.ExcuseHunterScreen
import app.hisn.ui.screens.ExercisePlayerScreen
import app.hisn.ui.screens.ExercisesScreen
import app.hisn.domain.LibType
import app.hisn.ui.screens.BreakoutScreen
import app.hisn.ui.screens.BreathScreen
import app.hisn.ui.screens.JournalScreen
import app.hisn.ui.screens.G2048Screen
import app.hisn.ui.screens.PongScreen
import app.hisn.ui.screens.GameKind
import app.hisn.ui.screens.LibraryShelfScreen
import app.hisn.ui.screens.NovellaReaderScreen
import app.hisn.ui.screens.PdfReaderScreen
import app.hisn.ui.screens.SchulteScreen
import app.hisn.ui.screens.SimonScreen
import app.hisn.ui.screens.TextReaderScreen
import app.hisn.ui.screens.XoScreen
import app.hisn.ui.screens.MathSprintScreen
import app.hisn.ui.screens.MemoryScreen
import app.hisn.ui.screens.SudokuScreen
import app.hisn.ui.screens.HomeScreen
import app.hisn.ui.screens.InsightsScreen
import app.hisn.ui.screens.OnboardingScreen
import app.hisn.ui.screens.ProgramScreen
import app.hisn.ui.screens.RelapseScreen
import app.hisn.ui.screens.SettingsScreen
import app.hisn.ui.screens.StoryReaderScreen
import app.hisn.ui.screens.TimelineScreen
import app.hisn.ui.screens.TrophiesScreen
import app.hisn.ui.screens.UnlockCelebrationDialog
import app.hisn.ui.screens.UrgeScreen
import app.hisn.ui.theme.HisnTheme
import app.hisn.work.Reminders
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    /**
     * التطبيق عربي أولًا. فرض اللغة هنا يجعل مكوّنات النظام نفسها — مثل
     * منتقي التاريخ — تعرض أسماء الأشهر والأيام بالعربية، لا بلغة الجهاز.
     */
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("ar")
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // إعادة جدولة التذكير اليومي بعد إعادة التشغيل أو التحديث.
        Prefs(this).let { p ->
            if (p.onboarded && p.remindersEnabled) Reminders.schedule(this, p.reminderHour)
        }

        setContent {
            HisnTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    App(vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refresh()
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Outlined.Home),
    PROGRAM("المسار", Icons.Outlined.Route),
    TOOLS("الأدوات", Icons.Outlined.GridView),
    TROPHIES("الإنجازات", Icons.Outlined.EmojiEvents),
    INSIGHTS("رؤى", Icons.Outlined.BarChart)
}

private sealed interface Overlay {
    data class Player(val exerciseId: String) : Overlay
    data class Day(val day: Int) : Overlay
    data class StoryRead(val storyId: String) : Overlay
    data class Cert(val achievementId: String) : Overlay
    data object Timeline : Overlay
    data object Urge : Overlay
    data object Relapse : Overlay
    data object Dhikr : Overlay
    data class Game(val kind: GameKind) : Overlay
    data object Shelf : Overlay
    data class LibRead(val itemId: Long) : Overlay
    data object NovellaRead : Overlay
    data object Settings : Overlay
    data object Breath : Overlay
    data object Journal : Overlay
}

@Composable
private fun App(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    var tab by remember { mutableStateOf(Tab.HOME) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    var confetti by remember { mutableIntStateOf(0) }

    if (state.loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    if (!state.onboarded) {
        Box(Modifier.systemBarsPadding()) {
            OnboardingScreen { habits, mode, why ->
                vm.completeOnboarding(habits, mode, why)
            }
        }
        return
    }

    Box(Modifier.fillMaxSize()) {

        // ── الطبقات المنبثقة فوق التبويبات ──
        val ov = overlay
        if (ov != null) {
            BackHandler { overlay = null }
            Box(Modifier.systemBarsPadding()) {
                when (ov) {
                    is Overlay.Player -> {
                        val ex = Exercises.byId(ov.exerciseId)
                        if (ex == null) overlay = null
                        else ExercisePlayerScreen(
                            exercise = ex,
                            mode = state.mode,
                            onFinish = { before, after ->
                                vm.logExercise(ex.id, before, after)
                                overlay = null
                            },
                            onExit = { overlay = null }
                        )
                    }

                    is Overlay.StoryRead -> {
                        val story = Stories.byId(ov.storyId)
                        if (story == null) overlay = null
                        else StoryReaderScreen(
                            story = story,
                            onOpenExercise = { overlay = Overlay.Player(it) },
                            onBack = { overlay = null }
                        )
                    }

                    is Overlay.Cert -> {
                        val ach = Achievements.byId(ov.achievementId)
                        if (ach == null) overlay = null
                        else CertificateScreen(
                            achievement = ach,
                            state = state,
                            onSetName = vm::setUserName,
                            onBack = { overlay = null }
                        )
                    }

                    Overlay.Timeline -> TimelineScreen(state) { overlay = null }

                    is Overlay.Day -> DayDetailScreen(
                        day = ov.day,
                        state = state,
                        onComplete = { r -> vm.completeDay(ov.day, r); overlay = null },
                        onOpenExercise = { overlay = Overlay.Player(it) },
                        onBack = { overlay = null }
                    )

                    Overlay.Urge -> UrgeScreen(
                        state = state,
                        onOpenExercise = { overlay = Overlay.Player(it) },
                        onOpenBreath = { overlay = Overlay.Breath },
                        onLogRide = { p, e, s, survived -> vm.logUrgeRide(p, e, s, survived) },
                        onExit = { overlay = null }
                    )

                    Overlay.Relapse -> RelapseScreen(
                        onSave = { t, p, i, n, k ->
                            state.selected?.let { vm.recordRelapse(it.id, t, p, i, n, k) }
                        },
                        onOpenExercise = { overlay = Overlay.Player(it) },
                        onExit = { overlay = null }
                    )

                    Overlay.Dhikr -> DhikrScreen { overlay = null }

                    Overlay.Breath -> BreathScreen(
                        onDone = vm::logActivity,
                        onExit = { overlay = null }
                    )

                    Overlay.Journal -> JournalScreen(
                        state = state,
                        onLog = vm::logMood,
                        onBack = { overlay = null }
                    )

                    is Overlay.Game -> when (ov.kind) {
                        GameKind.SUDOKU -> SudokuScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.MEMORY -> MemoryScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.MATH -> MathSprintScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.G2048 -> G2048Screen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.XO -> XoScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.SCHULTE -> SchulteScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.SIMON -> SimonScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.BREAKOUT -> BreakoutScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.PONG -> PongScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                        GameKind.EXCUSE -> ExcuseHunterScreen(
                            onDone = vm::logActivity,
                            onExit = { overlay = null }
                        )
                    }

                    Overlay.Shelf -> LibraryShelfScreen(
                        state = state,
                        novellaChapter = state.novellaChapter,
                        onAddPdf = vm::addLibraryPdf,
                        onAddText = vm::addLibraryText,
                        onDelete = vm::deleteLibraryItem,
                        onOpen = { overlay = Overlay.LibRead(it.id) },
                        onOpenNovella = { overlay = Overlay.NovellaRead },
                        onBack = { overlay = null }
                    )

                    is Overlay.LibRead -> {
                        val item = state.libraryItems.firstOrNull { it.id == ov.itemId }
                        if (item == null) overlay = Overlay.Shelf
                        else if (item.type == LibType.PDF) PdfReaderScreen(
                            item = item,
                            onSaveProgress = vm::setLibraryProgress,
                            onBack = { overlay = Overlay.Shelf }
                        )
                        else TextReaderScreen(item) { overlay = Overlay.Shelf }
                    }

                    Overlay.NovellaRead -> NovellaReaderScreen(
                        startChapter = state.novellaChapter,
                        onProgress = vm::setNovellaChapter,
                        onBack = { overlay = Overlay.Shelf }
                    )

                    Overlay.Settings -> SettingsScreen(
                        state = state,
                        onSetMode = vm::setMode,
                        onSetWhy = vm::setWhy,
                        onSetPlan = vm::setCopingPlan,
                        onSetName = vm::setUserName,
                        onSetReminder = vm::setReminder,
                        onAddHabit = { vm.addHabit(it, null, 0) },
                        onDeleteHabit = vm::deleteHabit,
                        onEditCounters = vm::editCounters,
                        onWipe = { vm.wipeEverything(); overlay = null },
                        onExport = vm::exportBackup,
                        onImport = vm::importBackup,
                        onBack = { overlay = null }
                    )
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        Tab.entries.forEach { t ->
                            NavigationBarItem(
                                selected = tab == t,
                                onClick = { tab = t },
                                icon = { Icon(t.icon, contentDescription = t.label) },
                                label = { Text(t.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            ) { pad ->
                Box(Modifier.padding(pad)) {
                    when (tab) {
                        Tab.HOME -> HomeScreen(
                            state = state,
                            onSelectHabit = vm::select,
                            onOpenExercise = { overlay = Overlay.Player(it) },
                            onOpenStory = { overlay = Overlay.StoryRead(it) },
                            onOpenUrge = { overlay = Overlay.Urge },
                            onLogRelapse = { overlay = Overlay.Relapse },
                            onOpenDay = { overlay = Overlay.Day(it) },
                            onOpenDhikr = { overlay = Overlay.Dhikr },
                            onOpenTrophies = { tab = Tab.TROPHIES },
                            onOpenSettings = { overlay = Overlay.Settings },
                            onPrepare = { overlay = Overlay.Player("if_then") },
                            onOpenBreath = { overlay = Overlay.Breath },
                            onOpenJournal = { overlay = Overlay.Journal },
                            onLogMood = vm::logMood,
                            onSetCost = vm::setHabitCost
                        )

                        Tab.PROGRAM -> ProgramScreen(
                            state = state,
                            onOpenDay = { overlay = Overlay.Day(it) },
                            onOpenTimeline = { overlay = Overlay.Timeline },
                            onChallengeDone = vm::logActivity,
                            onAddCapsule = vm::addCapsule,
                            onOpenCapsule = vm::openCapsule,
                            onDeleteCapsule = vm::deleteCapsule
                        )

                        Tab.TOOLS -> ExercisesScreen(
                            onOpen = { overlay = Overlay.Player(it) },
                            onOpenStory = { overlay = Overlay.StoryRead(it) },
                            activityCount = state.activityCount,
                            onLogActivity = vm::logActivity,
                            onOpenGame = { overlay = Overlay.Game(it) },
                            onOpenShelf = { overlay = Overlay.Shelf },
                            onOpenBreath = { overlay = Overlay.Breath }
                        )

                        Tab.TROPHIES -> TrophiesScreen(
                            state = state,
                            onOpenCert = { overlay = Overlay.Cert(it) }
                        )

                        Tab.INSIGHTS -> InsightsScreen(state)
                    }
                }
            }
        }

        // ── احتفال فتح كؤوس جديدة: كونفيتي + بطاقة ──
        if (state.newUnlocks.isNotEmpty()) {
            if (confetti == 0) confetti = state.newUnlocks.hashCode().takeIf { it != 0 } ?: 1
            UnlockCelebrationDialog(
                unlocks = state.newUnlocks,
                onOpenCert = { overlay = Overlay.Cert(it) },
                onDismiss = { vm.consumeUnlocks(); confetti = 0 }
            )
        }
        ConfettiOverlay(trigger = confetti) { }
    }
}
