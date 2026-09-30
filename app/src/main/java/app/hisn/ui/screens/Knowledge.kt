package app.hisn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.hisn.content.Evidence
import app.hisn.content.Exercises
import app.hisn.content.Stories
import app.hisn.content.Story
import app.hisn.content.StoryKind
import app.hisn.content.Timeline
import app.hisn.ui.Eyebrow
import app.hisn.ui.Pill
import app.hisn.ui.SectionCard
import app.hisn.ui.StaticBackdrop
import app.hisn.ui.UiState
import app.hisn.ui.VSpace
import app.hisn.ui.theme.Accent
import app.hisn.ui.theme.color
import app.hisn.ui.theme.icon

/** قائمة القصص والاستعارات. */
@Composable
fun StoriesList(onOpen: (String) -> Unit, onOpenShelf: () -> Unit = {}) {
    val dark = isSystemInDarkTheme()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard(
                modifier = Modifier.clickable { onOpenShelf() },
                tone = Accent.Sand.copy(alpha = .12f),
                border = Accent.Sand.copy(alpha = .5f)
            ) {
                Text("📚 مكتبتي", style = MaterialTheme.typography.titleLarge)
                Text(
                    "قصص طويلة وروايات وكتبك أنت: أضف PDF أو نصًا بعنوانه وعُد " +
                        "إليه من حيث توقفت — وبداخلها رواية «العائد» بفصولها الستة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    "القصص الواقعية عن أشخاص حقيقيين تحدثوا عن تجاربهم علنًا — والمصدر " +
                        "مذكور داخل كل واحدة. الاستعارات أدوات علاجية، ونماذج التعافي " +
                        "تمثيلية تجمع أنماطًا متكررة لا شهادات أشخاص بأعينهم.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        StoryKind.entries.forEach { kind ->
            val list = Stories.byKind(kind)
            if (list.isEmpty()) return@forEach
            item { VSpace(2); Eyebrow(kind.label) }
            items(list.size) { i ->
                val s = list[i]
                val c = s.method?.color(dark) ?: Accent.Teal
                SectionCard(modifier = Modifier.clickable { onOpen(s.id) }) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(c.copy(alpha = .16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (kind) {
                                    StoryKind.REAL -> Icons.Outlined.Verified
                                    StoryKind.METAPHOR -> Icons.Outlined.AutoStories
                                    else -> Icons.Outlined.Insights
                                },
                                contentDescription = null,
                                tint = c,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(s.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                s.hook,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Pill(
                            "${s.minutes} د",
                            bg = c.copy(alpha = .16f),
                            fg = c
                        )
                    }
                }
            }
        }
    }
}

/** قارئ القصة. */
@Composable
fun StoryReaderScreen(
    story: Story,
    onOpenExercise: (String) -> Unit,
    onBack: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val c = story.method?.color(dark) ?: Accent.Teal

    StaticBackdrop(tint = c) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Pill(story.kind.label, bg = c.copy(alpha = .18f), fg = c)
                TextButton(onClick = onBack) { Text("رجوع") }
            }

            Text(story.title, style = MaterialTheme.typography.headlineLarge)
            Text(
                story.hook,
                style = MaterialTheme.typography.titleMedium,
                color = c
            )

            story.body.forEach { para ->
                Text(para, style = MaterialTheme.typography.bodyLarge)
            }

            SectionCard(
                tone = c.copy(alpha = .12f),
                border = c.copy(alpha = .45f)
            ) {
                Eyebrow("الخلاصة", c)
                Text(story.takeaway, style = MaterialTheme.typography.titleMedium)
            }

            story.source?.let { src ->
                SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                    Eyebrow("المصدر")
                    Text(
                        src,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "الوقائع كما رُويت في مصادرها العلنية، والصياغة صياغتنا.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .75f)
                    )
                }
            }

            story.relatedExerciseId?.let { id ->
                Exercises.byId(id)?.let { ex ->
                    OutlinedButton(
                        onClick = { onOpenExercise(id) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("التمرين المرتبط: ${ex.title}") }
                }
            }
            VSpace(50)
        }
    }
}

/** ماذا يحدث خلال 90 يومًا — بفصل صريح بين المثبت والمتداول. */
@Composable
fun TimelineScreen(state: UiState, onBack: () -> Unit) {
    val currentDay = state.programDay
    val kind = state.selected?.kind

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
                Eyebrow("خط زمني")
                TextButton(onClick = onBack) { Text("رجوع") }
            }
            Text("ماذا يحدث خلال 90 يومًا", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            SectionCard(
                tone = Accent.Amber.copy(alpha = .12f),
                border = Accent.Amber.copy(alpha = .5f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Science,
                        contentDescription = null,
                        tint = Accent.Amber,
                        modifier = Modifier.size(20.dp)
                    )
                    Eyebrow("قاعدة هذه الصفحة", Accent.Amber)
                }
                Text(
                    "كل بند هنا مصنَّف بمستوى الدليل الذي يسنده. لن نبيع لك رواية " +
                        "«إعادة ضبط الدوبامين خلال 90 يومًا» — فهي غير مثبتة، والوعد " +
                        "المبالغ فيه يبني توقعًا يُخلَف، وخيبة التوقع سبب معروف للانقطاع.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        kind?.let { k ->
            Timeline.specificNote(k)?.let { note ->
                item {
                    SectionCard(tone = MaterialTheme.colorScheme.surfaceVariant) {
                        Eyebrow("خاص بـ ${k.label}")
                        Text(note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        items(Timeline.windows.size) { i ->
            val w = Timeline.windows[i]
            val here = currentDay in w.range
            SectionCard(
                tone = if (here) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                border = if (here) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Eyebrow(w.label)
                    if (here) Pill("أنت هنا")
                }
                Text(w.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    w.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                VSpace(2)
                w.items.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EvidenceTag(item.evidence)
                        Text(
                            item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item { VSpace(6); Eyebrow("ما يُتداول ولا يصمد", Accent.Coral) }
        items(Timeline.myths.size) { i ->
            val m = Timeline.myths[i]
            SectionCard(border = Accent.Coral.copy(alpha = .45f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        m.claim,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Pill(
                    m.verdict,
                    bg = Accent.Coral.copy(alpha = .16f),
                    fg = Accent.Coral
                )
                Text(
                    m.truth,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EvidenceTag(e: Evidence) {
    val c = when (e) {
        Evidence.STRONG -> Accent.Emerald
        Evidence.MODERATE -> Accent.Blue
        Evidence.REPORTED -> Accent.Amber
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(c.copy(alpha = .16f))
            .border(1.dp, c.copy(alpha = .4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(e.label, style = MaterialTheme.typography.labelSmall, color = c)
    }
}
