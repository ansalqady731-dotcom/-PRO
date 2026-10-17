package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DataManager
import com.example.data.model.Ayah
import com.example.data.model.Bookmark
import com.example.data.model.TadabburNote
import com.example.data.quran.QuranAudioPlayer
import com.example.data.quran.QuranRepository
import com.example.data.quran.QuranWordMeanings
import com.example.ui.theme.*
import java.util.UUID

/**
 * Bottom Sheet for Ayah Action & Tafsir Display
 * Matches Screenshot 2 (إضافة الفواصل، الختمة، الخواطر والمختصر في التفسير)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahActionBottomSheet(
    ayah: Ayah,
    surahName: String,
    dataManager: DataManager,
    onDismiss: () -> Unit,
    onOpenShare: (Ayah) -> Unit,
    onOpenAddNote: (Ayah) -> Unit
) {
    val context = LocalContext.current
    val tafsirText = remember(ayah) {
        QuranRepository.getTafsir(context, ayah.surahNumber, ayah.numberInSurah)
    }
    val wordMeanings: List<com.example.data.model.WordMeaning> = remember(ayah) {
        QuranWordMeanings.extractWordMeaningsForAyah(ayah.textArabic)
    }

    var isFavorite by remember(ayah) {
        mutableStateOf(dataManager.isAyahFavorite(ayah.surahNumber, ayah.numberInSurah))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header: Ayah reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سورة $surahName • الآية ${ayah.numberInSurah}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary
                    )
                    Text(
                        text = "صفحة ${ayah.page} • الجزء ${ayah.juz}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Grid / Row (Matching Screenshot 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 1. إضافة فاصل (Bookmark)
                ActionItemButton(
                    icon = Icons.Outlined.BookmarkBorder,
                    label = "إضافة فاصل",
                    onClick = {
                        dataManager.saveBookmark(
                            Bookmark(
                                surahNumber = ayah.surahNumber,
                                surahName = surahName,
                                ayahNumber = ayah.numberInSurah,
                                note = "صفحة ${ayah.page}"
                            )
                        )
                        Toast.makeText(context, "تم حفظ موضع القراءة (فاصل)", Toast.LENGTH_SHORT).show()
                    }
                )

                // 2. إضافة إلى المفضلة (Favorite)
                ActionItemButton(
                    icon = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    label = if (isFavorite) "في المفضلة" else "إلى المفضلة",
                    iconTint = if (isFavorite) IslamicGold else MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        val next = dataManager.toggleFavoriteAyah(ayah.surahNumber, ayah.numberInSurah)
                        isFavorite = next
                        val msg = if (next) "تمت إضافة الآية للمفضلة" else "تمت إزالة الآية من المفضلة"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )

                // 3. إضافة خاطرة (Reflection / Note)
                ActionItemButton(
                    icon = Icons.Outlined.EditNote,
                    label = "إضافة خاطرة",
                    onClick = {
                        onDismiss()
                        onOpenAddNote(ayah)
                    }
                )

                // 4. مشاركة (Share)
                ActionItemButton(
                    icon = Icons.Outlined.Share,
                    label = "مشاركة",
                    onClick = {
                        onDismiss()
                        onOpenShare(ayah)
                    }
                )

                // 5. استماع (Play Audio)
                ActionItemButton(
                    icon = Icons.Outlined.PlayCircleOutline,
                    label = "استماع",
                    onClick = {
                        QuranAudioPlayer.playAyah(ayah.surahNumber, ayah.numberInSurah)
                        Toast.makeText(context, "جارٍ تشغيل تلاوة الآية...", Toast.LENGTH_SHORT).show()
                    }
                )

                // 6. نسخ (Copy)
                ActionItemButton(
                    icon = Icons.Outlined.ContentCopy,
                    label = "نسخ",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ayah Text", "${ayah.textArabic} [سورة $surahName: ${ayah.numberInSurah}]")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ نص الآية", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Tafsir Card (المختصر في التفسير)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المختصر في تفسير القرآن الكريم",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IslamicGreenPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "تفسير موثوق",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${ayah.numberInSurah}- $tafsirText",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = dataManager.tafsirFontSize.sp,
                                lineHeight = (dataManager.tafsirFontSize * 1.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Word meanings if available
                    if (wordMeanings.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "معاني بعض الكلمات:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(wordMeanings) { wm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "• ${wm.word}: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreenPrimary
                                )
                                Text(
                                    text = wm.meaning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionItemButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Dialog for Sharing Ayah as Text or Image Card
 * Matches Screenshot 1 (إمكانية نسخ أو مشاركة الآية مع التفسير بشكل نصي أو كصورة)
 */
@Composable
fun AyahShareDialog(
    ayah: Ayah,
    surahName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tafsirText = remember(ayah) {
        QuranRepository.getTafsir(context, ayah.surahNumber, ayah.numberInSurah)
    }

    var selectedShareMode by remember { mutableStateOf("image_card") } // "image_card", "plain", "tashkeel", "with_tafsir"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title (Matching Screenshot 1)
                Text(
                    text = "مشاركة ( $surahName )",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = IslamicGreenPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Range pickers row: من الفاتحة: 2 إلى الفاتحة: 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("من", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$surahName: ${ayah.numberInSurah}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("إلى", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$surahName: ${ayah.numberInSurah}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "المشاركة كـ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Options list (Matching Screenshot 1)
                ShareOptionRow(
                    label = "بطاقة مصورة (تصميم مصحفي بديع)",
                    icon = Icons.Outlined.Image,
                    isSelected = selectedShareMode == "image_card",
                    onClick = { selectedShareMode = "image_card" }
                )

                ShareOptionRow(
                    label = "نص الآية (نص مجرد)",
                    icon = Icons.Outlined.TextFields,
                    isSelected = selectedShareMode == "plain",
                    onClick = { selectedShareMode = "plain" }
                )

                ShareOptionRow(
                    label = "النص مع تشكيل (رسم عثماني كامل)",
                    icon = Icons.Outlined.FormatQuote,
                    isSelected = selectedShareMode == "tashkeel",
                    onClick = { selectedShareMode = "tashkeel" }
                )

                ShareOptionRow(
                    label = "النص مع التفسير (المختصر في التفسير)",
                    icon = Icons.Outlined.MenuBook,
                    isSelected = selectedShareMode == "with_tafsir",
                    onClick = { selectedShareMode = "with_tafsir" }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Green Share Button
                Button(
                    onClick = {
                        val shareContent = when (selectedShareMode) {
                            "image_card" -> {
                                """
                                ۞ سورة $surahName [الآية ${ayah.numberInSurah}] ۞
                                
                                ﷽
                                "${ayah.textArabic}"
                                
                                📖 المختصر في التفسير:
                                $tafsirText
                                
                                ───
                                📱 من تطبيق «رفيق المسلم PRO»
                                """.trimIndent()
                            }
                            "plain" -> {
                                val clean = ayah.textArabic.replace(Regex("[ًٌٍَُِّْـٓۛۜۚۘۗ]"), "")
                                "$clean ($surahName: ${ayah.numberInSurah})"
                            }
                            "tashkeel" -> {
                                "﴿ ${ayah.textArabic} ﴾ [سورة $surahName: ${ayah.numberInSurah}]"
                            }
                            "with_tafsir" -> {
                                """
                                ﴿ ${ayah.textArabic} ﴾ [سورة $surahName: ${ayah.numberInSurah}]
                                
                                التفسير:
                                $tafsirText
                                """.trimIndent()
                            }
                            else -> ayah.textArabic
                        }

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "آية من القرآن الكريم - سورة $surahName")
                            putExtra(Intent.EXTRA_TEXT, shareContent)
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة الآية الكريمة"))
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "مشاركة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareOptionRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = IslamicGreenPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Font & Display Settings Bottom Sheet
 * Matches Screenshot 3 (إمكانية تغيير نوع وحجم الخط)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranFontSettingsSheet(
    dataManager: DataManager,
    onDismiss: () -> Unit,
    onSettingsChanged: () -> Unit
) {
    var quranFontSize by remember { mutableFloatStateOf(dataManager.quranFontSize) }
    var tafsirFontSize by remember { mutableFloatStateOf(dataManager.tafsirFontSize) }
    var currentFontFamily by remember { mutableStateOf(dataManager.quranFontFamily) }
    var currentViewMode by remember { mutableStateOf(dataManager.quranViewMode) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "إعدادات القراءة والخطوط",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IslamicGreenPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Reading mode toggle
            Text(
                text = "طريقة عرض المصحف:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentViewMode == "mushaf_page",
                    onClick = {
                        currentViewMode = "mushaf_page"
                        dataManager.quranViewMode = "mushaf_page"
                        onSettingsChanged()
                    },
                    label = { Text("صفحات مصحف المدينة الورقي") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = currentViewMode == "flexible_list",
                    onClick = {
                        currentViewMode = "flexible_list"
                        dataManager.quranViewMode = "flexible_list"
                        onSettingsChanged()
                    },
                    label = { Text("وضع القراءة المرنة") },
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Quran Preview Text Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = QuranParchmentLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, QuranFrameBorderGold)
            ) {
                Text(
                    text = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ ۝٢",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = when (currentFontFamily) {
                            "amiri" -> AmiriFontFamily
                            "scheherazade_new" -> ScheherazadeFontFamily
                            else -> AmiriQuranFontFamily
                        },
                        fontSize = quranFontSize.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF261D0F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quran Font Size Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ع", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Slider(
                    value = quranFontSize,
                    onValueChange = {
                        quranFontSize = it
                        dataManager.quranFontSize = it
                        onSettingsChanged()
                    },
                    valueRange = 18f..38f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGreenPrimary,
                        activeTrackColor = IslamicGreenPrimary
                    )
                )
                Text("ع", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Font Type Selector Buttons (Matching Screenshot 3)
            Text(
                text = "نوع الخط القرآني:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        currentFontFamily = "amiri_quran"
                        dataManager.quranFontFamily = "amiri_quran"
                        onSettingsChanged()
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (currentFontFamily == "amiri_quran") ButtonDefaults.outlinedButtonColors(containerColor = IslamicGreenPrimary.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("خط المصحف", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        currentFontFamily = "amiri"
                        dataManager.quranFontFamily = "amiri"
                        onSettingsChanged()
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (currentFontFamily == "amiri") ButtonDefaults.outlinedButtonColors(containerColor = IslamicGreenPrimary.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("خط النسخ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        currentFontFamily = "scheherazade_new"
                        dataManager.quranFontFamily = "scheherazade_new"
                        onSettingsChanged()
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (currentFontFamily == "scheherazade_new") ButtonDefaults.outlinedButtonColors(containerColor = IslamicGreenPrimary.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("خط شهريزاد", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Tafsir Font Size Slider
            Text(
                text = "حجم خط التفسير (${tafsirFontSize.toInt()} sp):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ع", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = tafsirFontSize,
                    onValueChange = {
                        tafsirFontSize = it
                        dataManager.tafsirFontSize = it
                        onSettingsChanged()
                    },
                    valueRange = 12f..24f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGold,
                        activeTrackColor = IslamicGold
                    )
                )
                Text("ع", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

/**
 * Dialog for Adding Personal Reflection / Note (إضافة خاطرة / تدبر)
 */
@Composable
fun AddTadabburNoteDialog(
    ayah: Ayah,
    surahName: String,
    dataManager: DataManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("تدبر سورة $surahName آية ${ayah.numberInSurah}") }
    var reflectionText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "إضافة خاطرة وتدبر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreenPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "﴿ ${ayah.textArabic} ﴾",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = AmiriQuranFontFamily),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الخاطرة") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    label = { Text("اكتب تدبرك أو ما استفدته من الآية...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reflectionText.isNotBlank()) {
                                dataManager.saveTadabburNote(
                                    TadabburNote(
                                        id = UUID.randomUUID().toString(),
                                        title = title.ifBlank { "خاطرة قرآنية" },
                                        reflectionText = reflectionText.trim(),
                                        surahNumber = ayah.surahNumber,
                                        surahName = surahName,
                                        ayahNumber = ayah.numberInSurah
                                    )
                                )
                                Toast.makeText(context, "تم حفظ الخاطرة في دفتر التدبر بنجاح", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "يرجى كتابة نص الخاطرة أولاً", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                    ) {
                        Text("حفظ الخاطرة")
                    }
                }
            }
        }
    }
}

/**
 * Tafsir & Books Library Manager
 * Matches Screenshot 4 (إدارة التحميلات وإمكانية تنزيل أو حذف الكتب)
 */
@Composable
fun TafsirLibraryDialog(
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val booksList = remember {
        listOf(
            TafsirBookItem(
                id = "mukhtasar_ar",
                title = "المختصر في تفسير القرآن الكريم",
                language = "العربية",
                status = "مثبت أوفلاين (جاهز)",
                isDownloaded = true,
                size = "4.2 MB"
            ),
            TafsirBookItem(
                id = "muyassar_ar",
                title = "التفسير الميسر (مجمع الملك فهد)",
                language = "العربية",
                status = "مثبت أوفلاين (جاهز)",
                isDownloaded = true,
                size = "3.8 MB"
            ),
            TafsirBookItem(
                id = "mukhtasar_en",
                title = "The Abridged Explanation of the Quran",
                language = "English",
                status = "متاح للتنزيل",
                isDownloaded = false,
                size = "5.1 MB"
            ),
            TafsirBookItem(
                id = "mukhtasar_fr",
                title = "Traduction du résumé de l'exégèse du Noble Coran",
                language = "Français",
                status = "متاح للتنزيل",
                isDownloaded = false,
                size = "5.3 MB"
            ),
            TafsirBookItem(
                id = "mukhtasar_id",
                title = "Terjemahan Al-Mukhtashar fi Tafsir Al-Qur'an",
                language = "Bahasa Indonesia",
                status = "متاح للتنزيل",
                isDownloaded = false,
                size = "4.9 MB"
            )
        )
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) booksList else booksList.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.language.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قائمة كتب التفسير والترجمات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن كتاب التفسير...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { book ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = book.language,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IslamicGreenPrimary
                                        )
                                        Text(
                                            text = "• ${book.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (book.isDownloaded) IslamicGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = book.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (book.isDownloaded) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TafsirBookItem(
    val id: String,
    val title: String,
    val language: String,
    val status: String,
    val isDownloaded: Boolean,
    val size: String
)
