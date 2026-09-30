package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SaveAlt
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.hisn.domain.ContentMode
import app.hisn.domain.Habit
import app.hisn.domain.HabitKind
import app.hisn.ui.Eyebrow
import app.hisn.ui.SectionCard
import app.hisn.ui.UiState
import app.hisn.ui.VSpace
import app.hisn.ui.dayWord
import app.hisn.ui.theme.color
import app.hisn.ui.theme.icon
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: UiState,
    onSetMode: (ContentMode) -> Unit,
    onSetWhy: (String) -> Unit,
    onSetPlan: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetReminder: (Boolean, Int) -> Unit,
    onAddHabit: (HabitKind) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onEditCounters: (Long, Long, Int, Int) -> Unit,
    onWipe: () -> Unit,
    onExport: ((String?) -> Unit) -> Unit,
    onImport: (String, (Boolean) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var why by remember(state.whyStatement) { mutableStateOf(state.whyStatement) }
    var plan by remember(state.copingPlan) { mutableStateOf(state.copingPlan) }
    var confirmWipe by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Habit?>(null) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الإعدادات", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = onBack) { Text("رجوع") }
            }
        }

        // ── ما تتابعه + تعديل العدّاد ──
        item {
            SectionCard {
                SectionHead(Icons.Outlined.Shield, "ما تتابعه", MaterialTheme.colorScheme.primary)
                state.habits.forEach { h ->
                    val hc = h.kind.color()
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(38.dp).clip(CircleShape).background(hc.copy(alpha = .16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(h.kind.icon(), null, tint = hc, modifier = Modifier.size(20.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(h.displayName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${dayWord(h.totalCleanDays())} نظيفة · السلسلة ${h.currentStreakDays()} · القياسي ${h.bestStreakDays()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { editing = h }) {
                            Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(17.dp))
                            Text(" تعديل")
                        }
                    }
                }
                OutlinedButton(onClick = { adding = !adding }) {
                    Text(if (adding) "إلغاء" else "إضافة عادة")
                }
                if (adding) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HabitKind.entries.forEach { k ->
                            val kc = k.color()
                            Row(
                                Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(kc.copy(alpha = .12f))
                                    .border(1.dp, kc.copy(alpha = .4f), RoundedCornerShape(999.dp))
                                    .clickable { onAddHabit(k); adding = false }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(k.icon(), null, tint = kc, modifier = Modifier.size(15.dp))
                                Text(
                                    k.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = kc
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── نبرة المحتوى ──
        item {
            SectionCard {
                SectionHead(
                    Icons.Outlined.Palette, "نبرة المحتوى",
                    MaterialTheme.colorScheme.tertiary
                )
                Text(
                    "المحرك العلاجي واحد في الحالات الثلاث؛ ما يتغيّر هو الإطار.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ContentMode.BOTH to "الاثنان",
                        ContentMode.ISLAMIC to "إسلامي",
                        ContentMode.SCIENTIFIC to "علمي محايد"
                    ).forEach { (m, label) ->
                        Chip(label, m == state.mode) { onSetMode(m) }
                    }
                }
            }
        }

        // ── لماذا بدأت ──
        item {
            SectionCard {
                Eyebrow("لماذا بدأت")
                OutlinedTextField(
                    value = why,
                    onValueChange = { why = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("جملة واحدة بلا تأنيب") }
                )
                OutlinedButton(onClick = { onSetWhy(why) }) { Text("حفظ") }
            }
        }

        // ── الخطة البديلة ──
        item {
            SectionCard {
                Eyebrow("خطتك وقت الرغبة")
                Text(
                    "اكتبها الآن وأنت هادئ. ستُعرض عليك في لحظة الرغبة، حين لا يكون التفكير حرًّا.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = plan,
                    onValueChange = { plan = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("أقوم من مكاني، أخرج من الغرفة، وأتصل بـ…") }
                )
                OutlinedButton(onClick = { onSetPlan(plan) }) { Text("حفظ") }
            }
        }

        // ── التذكير اليومي ──
        item {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            var pendingHour by remember { mutableStateOf(state.reminderHour) }
            val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { granted -> if (granted) onSetReminder(true, pendingHour) }

            fun enableReminder(hour: Int) {
                pendingHour = hour
                if (android.os.Build.VERSION.SDK_INT >= 33 &&
                    ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onSetReminder(true, hour)
                }
            }

            SectionCard {
                SectionHead(
                    Icons.Outlined.Notifications, "التذكير اليومي",
                    MaterialTheme.colorScheme.primary
                )
                Text(
                    "إشعار واحد في اليوم يذكّرك بجلستك. العودة اليومية هي العمود " +
                        "الفقري للبرنامج كله.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مفعّل", style = MaterialTheme.typography.titleMedium)
                    androidx.compose.material3.Switch(
                        checked = state.remindersEnabled,
                        onCheckedChange = { on ->
                            if (on) enableReminder(state.reminderHour)
                            else onSetReminder(false, state.reminderHour)
                        }
                    )
                }
                if (state.remindersEnabled) {
                    Eyebrow("وقت التذكير")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(6, 12, 18, 20, 21, 22).forEach { h ->
                            Chip(
                                app.hisn.content.Guidance.fmtHour(h),
                                state.reminderHour == h
                            ) { enableReminder(h) }
                        }
                    }
                }
            }
        }

        // ── اسم الشهادة ──
        item {
            var name by remember(state.userName) { mutableStateOf(state.userName) }
            SectionCard {
                Eyebrow("اسمك على الشهادات")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("اختياري — يظهر على شهادات الإنجاز فقط") }
                )
                OutlinedButton(onClick = { onSetName(name) }) { Text("حفظ") }
            }
        }

        // ── النسخ الاحتياطي ──
        item { BackupSection(onExport, onImport) }

        // ── الخصوصية ──
        item {
            SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                SectionHead(
                    Icons.Outlined.Lock, "الخصوصية",
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "كل ما تكتبه وتسجّله يبقى على هذا الجهاز وحده. لا حساب، ولا خادم، " +
                        "ولا مزامنة، ولا إعلانات، ولا تتبّع. سجل الانتكاسات والمحفزات بيانات " +
                        "شديدة الحساسية، والتعامل الوحيد الآمن معها ألا تغادر جهازك.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── السلامة ──
        item {
            SectionCard(
                tone = MaterialTheme.colorScheme.tertiaryContainer,
                border = MaterialTheme.colorScheme.tertiary
            ) {
                SectionHead(
                    Icons.Outlined.HealthAndSafety, "متى تطلب مساعدة مختص",
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "هذا التطبيق أداة دعم، وليس بديلًا عن معالج نفسي. اطلب مساعدة مهنية فورًا إن " +
                        "كان لديك اكتئاب مستمر، أو أفكار إيذاء للنفس، أو فقدان سيطرة رغم المحاولة " +
                        "المتكررة، أو إدمان مادة يحتاج سحبًا طبيًا.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            VSpace(8)
            OutlinedButton(
                onClick = { confirmWipe = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Outlined.DeleteForever, null, modifier = Modifier.size(18.dp))
                Text("  حذف كل البيانات")
            }
        }

        item {
            Text(
                "حِصن · الإصدار ${app.hisn.BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    editing?.let { h ->
        CounterEditDialog(
            habit = h,
            onDismiss = { editing = null },
            onSave = { day, banked, longest ->
                onEditCounters(h.id, day, banked, longest)
                editing = null
            },
            onDelete = { onDeleteHabit(h.id); editing = null }
        )
    }

    if (confirmWipe) {
        AlertDialog(
            onDismissRequest = { confirmWipe = false },
            title = { Text("حذف كل شيء؟") },
            text = {
                Text(
                    "سيُحذف كل شيء نهائيًا: العادات، السجل، التمارين، وتقدّم المسار. " +
                        "لا يمكن التراجع."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmWipe = false; onWipe() }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmWipe = false }) { Text("إلغاء") }
            }
        )
    }
}

/**
 * تعديل العدّاد.
 *
 * كثيرون يبدأون قبل تثبيت أي تطبيق، أو ينسون التسجيل، أو يخطئون في الإدخال.
 * إجبارهم على رقم لا يعكس واقعهم يجعل العدّاد بلا معنى — فيُترك.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CounterEditDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (startEpochDay: Long, banked: Int, longest: Int) -> Unit,
    onDelete: () -> Unit
) {
    val todayMs = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val startMs = habit.streakStart.toEpochMilli()
    var startEpochDay by remember { mutableStateOf(startMs / 86_400_000L) }
    var banked by remember { mutableStateOf(habit.bankedCleanDays.toString()) }
    var longest by remember { mutableStateOf(habit.longestStreakDays.toString()) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }

    val chosen = LocalDate.ofEpochDay(startEpochDay)
    val daysSince = java.time.temporal.ChronoUnit.DAYS
        .between(chosen, LocalDate.now()).toInt().coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل عدّاد ${habit.displayName}") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "متى بدأت سلسلتك الحالية؟ اختر التاريخ الحقيقي حتى لو كان قبل تثبيت التطبيق.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${chosen.dayOfMonth}/${chosen.monthValue}/${chosen.year}  ·  $daysSince يومًا")
                }

                OutlinedTextField(
                    value = banked,
                    onValueChange = { banked = it.filter { c -> c.isDigit() }.take(5) },
                    label = { Text("رصيد الأيام النظيفة السابقة") },
                    supportingText = { Text("مجموع ما جمعته في محاولات سابقة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = longest,
                    onValueChange = { longest = it.filter { c -> c.isDigit() }.take(5) },
                    label = { Text("الرقم القياسي المحفوظ") },
                    supportingText = { Text("أطول سلسلة وصلت إليها يومًا") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (!confirmDelete) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text("حذف هذه العادة", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("تأكيد الحذف") }
                        TextButton(onClick = { confirmDelete = false }) { Text("تراجع") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(startEpochDay, banked.toIntOrNull() ?: 0, longest.toIntOrNull() ?: 0)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )

    if (showPicker) {
        DayPickerDialog(
            initialEpochDay = startEpochDay,
            maxMillis = todayMs,
            onPick = { startEpochDay = it; showPicker = false },
            onDismiss = { showPicker = false }
        )
    }
}

/**
 * منتقي تاريخ بعرض كامل.
 *
 * DatePickerDialog المخصص يمنح الشبكة عرضها الطبيعي — وضعها داخل AlertDialog
 * عام يقصّ عمود اليوم الأخير في الاتجاه من اليمين لليسار.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPickerDialog(
    initialEpochDay: Long,
    maxMillis: Long,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialEpochDay * 86_400_000L
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val ms = (state.selectedDateMillis ?: initialEpochDay * 86_400_000L)
                    .coerceAtMost(maxMillis)
                onPick(ms / 86_400_000L)
            }) { Text("تأكيد") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    ) {
        DatePicker(state = state, showModeToggle = false)
    }
}

/**
 * نسخة احتياطية بملف JSON واحد يختار المستخدم مكانه بنفسه (منتقي ملفات النظام).
 * لا يمرّ بأي خادم. الاستعادة تستبدل كل البيانات الحالية — لذلك تأكيد صريح.
 */
@Composable
private fun BackupSection(
    onExport: ((String?) -> Unit) -> Unit,
    onImport: (String, (Boolean) -> Unit) -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var pendingJson by remember { mutableStateOf<String?>(null) }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        onExport { json ->
            if (json == null) { status = "تعذّر إنشاء النسخة."; return@onExport }
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val ok = runCatching {
                    ctx.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }.isSuccess
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    status = if (ok) "حُفظت النسخة ✓" else "تعذّر حفظ الملف."
                }
            }
        }
    }
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val text = runCatching {
                ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            }.getOrNull()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (text.isNullOrBlank()) status = "تعذّرت قراءة الملف." else pendingJson = text
            }
        }
    }

    SectionCard {
        SectionHead(Icons.Outlined.SaveAlt, "النسخ الاحتياطي", MaterialTheme.colorScheme.primary)
        Text(
            "ملف واحد يحوي كل شيء: العادات، السجل، اليوميات، الكبسولات، المكتبة والإعدادات. " +
                "احفظه حيث تشاء واستعده على أي هاتف. لا يمرّ بأي خادم.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val d = LocalDate.now()
                exportLauncher.launch("hisn-backup-${d.year}${"%02d".format(java.util.Locale.US, d.monthValue)}${"%02d".format(java.util.Locale.US, d.dayOfMonth)}.json")
            }) {
                Icon(Icons.Outlined.SaveAlt, null, modifier = Modifier.size(17.dp))
                Text(" تصدير نسخة")
            }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }) {
                Icon(Icons.Outlined.Restore, null, modifier = Modifier.size(17.dp))
                Text(" استعادة")
            }
        }
        status?.let {
            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }

    pendingJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingJson = null },
            title = { Text("استعادة النسخة؟") },
            text = { Text("ستُستبدل كل بياناتك الحالية بما في الملف. لا يمكن التراجع.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingJson = null
                    onImport(json) { ok -> status = if (ok) "تمت الاستعادة ✓" else "الملف ليس نسخة حِصن صالحة." }
                }) { Text("استعادة", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingJson = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun SectionHead(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Eyebrow(title, color)
    }
}

@Composable
private fun Chip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (on) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                1.dp,
                if (on) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (on) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
