package app.hisn.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.hisn.content.Method
import app.hisn.content.Phase
import app.hisn.domain.HabitKind

/**
 * لوحة الألوان الممتدة.
 *
 * لكل منهج علاجي، ولكل مرحلة، ولكل عادة لونها الخاص — اللون هنا يحمل معلومة
 * (أي منهج؟ أي مرحلة؟) لا زينة. لذلك هو مشبع وواضح، ويُقرأ بلمحة.
 */
object Accent {
    val Violet = Color(0xFF7C4DFF)
    val VioletSoft = Color(0xFFB79CFF)
    val Blue = Color(0xFF2979FF)
    val BlueSoft = Color(0xFF8CB8FF)
    val Teal = Color(0xFF00BFA5)
    val TealSoft = Color(0xFF6FE3D0)
    val Magenta = Color(0xFFE5399B)
    val MagentaSoft = Color(0xFFFF92C8)
    val Amber = Color(0xFFFF9100)
    val AmberSoft = Color(0xFFFFC470)
    val Coral = Color(0xFFFF5C5C)
    val CoralSoft = Color(0xFFFF9E9E)
    val Lime = Color(0xFF7CB342)
    val LimeSoft = Color(0xFFB6E07E)
    val Cyan = Color(0xFF00B8D9)
    val CyanSoft = Color(0xFF6FE0F5)
    val Indigo = Color(0xFF3D5AFE)
    val Emerald = Color(0xFF0FA37F)
    val EmeraldSoft = Color(0xFF5FD9BC)
    val Rose = Color(0xFFF4436C)
    val Sand = Color(0xFFC79A3B)
}

/** لون المنهج العلاجي. */
fun Method.color(dark: Boolean): Color = when (this) {
    Method.ACT -> if (dark) Accent.VioletSoft else Accent.Violet
    Method.CBT -> if (dark) Accent.BlueSoft else Accent.Blue
    Method.MBRP -> if (dark) Accent.TealSoft else Accent.Teal
    Method.DBT -> if (dark) Accent.MagentaSoft else Accent.Magenta
    Method.MI -> if (dark) Accent.AmberSoft else Accent.Amber
    Method.SC -> if (dark) Accent.CoralSoft else Accent.Coral
    Method.BA -> if (dark) Accent.LimeSoft else Accent.Lime
}

fun Method.icon(): ImageVector = when (this) {
    Method.ACT -> Icons.Outlined.Explore
    Method.CBT -> Icons.Outlined.Psychology
    Method.MBRP -> Icons.Outlined.Waves
    Method.DBT -> Icons.Outlined.Bolt
    Method.MI -> Icons.Outlined.RecordVoiceOver
    Method.SC -> Icons.Outlined.Favorite
    Method.BA -> Icons.Outlined.AutoAwesome
}

/** لون المرحلة داخل مسار الـ90 يومًا. */
fun Phase.color(dark: Boolean): Color = when (this) {
    Phase.UNDERSTAND -> if (dark) Accent.CyanSoft else Accent.Cyan
    Phase.TOOLS -> if (dark) Accent.VioletSoft else Accent.Violet
    Phase.REBUILD -> if (dark) Accent.AmberSoft else Accent.Amber
    Phase.ANCHOR -> if (dark) Accent.EmeraldSoft else Accent.Emerald
}

/** لون العادة المتابَعة — يميّز البطاقات حين يتابع المستخدم أكثر من عادة. */
fun HabitKind.color(): Color = when (this) {
    HabitKind.PORN -> Accent.Rose
    HabitKind.MASTURBATION -> Accent.Emerald
    HabitKind.SMOKING -> Accent.Sand
    HabitKind.VAPE -> Accent.Cyan
    HabitKind.GAMING -> Accent.Violet
    HabitKind.SOCIAL -> Accent.Blue
    HabitKind.DRUGS -> Accent.Coral
    HabitKind.ALCOHOL -> Accent.Magenta
    HabitKind.GAMBLING -> Accent.Indigo
    HabitKind.JUNK_FOOD -> Accent.Amber
    HabitKind.PROCRASTINATION -> Accent.Teal
    HabitKind.CUSTOM -> Accent.Lime
}

fun HabitKind.icon(): ImageVector = when (this) {
    HabitKind.PORN -> Icons.Filled.NoAdultContent
    HabitKind.MASTURBATION -> Icons.Filled.Spa
    HabitKind.SMOKING -> Icons.Filled.SmokingRooms
    HabitKind.VAPE -> Icons.Filled.LocalFireDepartment
    HabitKind.GAMING -> Icons.Filled.SportsEsports
    HabitKind.SOCIAL -> Icons.Filled.PhoneAndroid
    HabitKind.DRUGS -> Icons.Filled.Warning
    HabitKind.ALCOHOL -> Icons.Filled.LocalBar
    HabitKind.GAMBLING -> Icons.Filled.Casino
    HabitKind.JUNK_FOOD -> Icons.Filled.Restaurant
    HabitKind.PROCRASTINATION -> Icons.Filled.HourglassEmpty
    HabitKind.CUSTOM -> Icons.Filled.TrackChanges
}

/** لون مساعد للسياق: يُستعمل للكافيين وما شابه إن أُضيف لاحقًا. */
val CaffeineIcon = Icons.Filled.LocalCafe

/** تدرّج قطري من لونين — يُستخدم في البطاقات البطلة. */
fun gradientOf(a: Color, b: Color): Brush = Brush.linearGradient(listOf(a, b))

@Composable
fun heroGradient(dark: Boolean): Brush = if (dark)
    Brush.linearGradient(listOf(Color(0xFF0D4C41), Color(0xFF123E63), Color(0xFF2E1F5E)))
else
    Brush.linearGradient(listOf(Color(0xFF0FA37F), Color(0xFF0E8FA8), Color(0xFF5B54D6)))
