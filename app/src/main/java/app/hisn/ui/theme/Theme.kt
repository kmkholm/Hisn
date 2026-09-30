package app.hisn.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// محايدات مائلة نحو الأخضر المزرقّ — مختارة لا موروثة.
private val Emerald = Color(0xFF0B6B58)
private val EmeraldLight = Color(0xFF46B79B)
private val Clay = Color(0xFFA85A11)
private val ClayLight = Color(0xFFDFA05A)

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBE6),
    onPrimaryContainer = Color(0xFF04241E),
    secondary = Color(0xFF4A6560),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EAE7),
    onSecondaryContainer = Color(0xFF16211F),
    tertiary = Clay,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6E7D4),
    onTertiaryContainer = Color(0xFF33210A),
    background = Color(0xFFF4F7F5),
    onBackground = Color(0xFF10201D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF10201D),
    surfaceVariant = Color(0xFFEDF1EF),
    onSurfaceVariant = Color(0xFF3D504B),
    outline = Color(0xFFB9C6C2),
    outlineVariant = Color(0xFFD9E2DF),
    error = Color(0xFF9B3226),
    onError = Color.White,
    errorContainer = Color(0xFFF8DFDB),
    onErrorContainer = Color(0xFF3A100B)
)

private val DarkColors = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF16332C),
    onPrimaryContainer = Color(0xFFB9E5D9),
    secondary = Color(0xFFA8C4BD),
    onSecondary = Color(0xFF16302A),
    secondaryContainer = Color(0xFF22322E),
    onSecondaryContainer = Color(0xFFD2E6E0),
    tertiary = ClayLight,
    onTertiary = Color(0xFF3D2609),
    tertiaryContainer = Color(0xFF33261A),
    onTertiaryContainer = Color(0xFFF3DCC2),
    background = Color(0xFF0C1614),
    onBackground = Color(0xFFE8EFEC),
    surface = Color(0xFF13201D),
    onSurface = Color(0xFFE8EFEC),
    surfaceVariant = Color(0xFF182926),
    onSurfaceVariant = Color(0xFFAEC0BA),
    outline = Color(0xFF41544F),
    outlineVariant = Color(0xFF2A3C37),
    error = Color(0xFFE08171),
    onError = Color(0xFF44100A),
    errorContainer = Color(0xFF3E1712),
    onErrorContainer = Color(0xFFFAD9D3)
)

private val HisnTypography = Typography(
    displaySmall = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp),
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.SemiBold, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp),
    titleLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 27.sp),
    bodyMedium = TextStyle(fontSize = 14.5.sp, lineHeight = 25.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun HisnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colors, typography = HisnTypography, content = content)
}

/** يمنع تحذير عدم استخدام LocalContext في بعض الإصدارات. */
@Composable
internal fun rememberAppContext() = LocalContext.current
