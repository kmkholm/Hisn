package app.hisn.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hisn.ui.Eyebrow
import app.hisn.ui.ProgressRing
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.VSpace
import app.hisn.ui.theme.Accent

private data class DhikrPreset(val text: String, val target: Int)

private val PRESETS = listOf(
    DhikrPreset("سبحان الله", 33),
    DhikrPreset("الحمد لله", 33),
    DhikrPreset("الله أكبر", 34),
    DhikrPreset("أستغفر الله", 100),
    DhikrPreset("لا حول ولا قوة إلا بالله", 100),
    DhikrPreset("اللهم صلِّ على محمد", 100)
)

/**
 * سبحة إلكترونية.
 *
 * ليست زخرفة دينية: الإيقاع المتكرر مع العدّ يشغل اللسان واليد والانتباه معًا —
 * وهو عمليًا تمرين تأريض يهدّئ الجهاز العصبي، بمحتوى ذي معنى لمن فعّل الوضع الإسلامي.
 */
@Composable
fun DhikrScreen(onExit: () -> Unit) {
    val view = LocalView.current
    var presetIx by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(0) }
    var rounds by remember { mutableIntStateOf(0) }
    var pressed by remember { mutableStateOf(false) }

    val preset = PRESETS[presetIx]
    val scale by animateFloatAsState(
        targetValue = if (pressed) .93f else 1f,
        animationSpec = spring(dampingRatio = .5f, stiffness = 700f),
        label = "tap"
    )

    StaticBackdrop(tint = Accent.Emerald) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("سبحة")
                TextButton(onClick = onExit) { Text("رجوع") }
            }

            Text(
                preset.text,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                "الهدف: ${preset.target} · أتممت $rounds ${if (rounds == 1) "جولة" else "جولات"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // منطقة اللمس الكبيرة — كل الدائرة قابلة للنقر
            Box(
                Modifier
                    .fillMaxWidth(.82f)
                    .aspectRatio(1f)
                    .scale(scale)
                    .clip(CircleShape)
                    .clickable {
                        pressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        count++
                        if (count >= preset.target) {
                            count = 0
                            rounds++
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                        pressed = false
                    },
                contentAlignment = Alignment.Center
            ) {
                ProgressRing(
                    progress = count.toFloat() / preset.target,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    colors = listOf(Accent.Emerald, Accent.Teal, Accent.Cyan)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize(.82f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Accent.Emerald.copy(alpha = .25f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$count",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "اضغط في أي مكان",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // اختيار الذكر
            SectionCard {
                Eyebrow("اختر الذكر")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PRESETS.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { p ->
                                val ix = PRESETS.indexOf(p)
                                val on = ix == presetIx
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (on) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (on) MaterialTheme.colorScheme.primary
                                            else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            presetIx = ix; count = 0; rounds = 0
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        p.text,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (on) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            if (row.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }

            OutlinedButton(onClick = { count = 0; rounds = 0 }) { Text("تصفير") }
            VSpace(10)
        }
    }
}
