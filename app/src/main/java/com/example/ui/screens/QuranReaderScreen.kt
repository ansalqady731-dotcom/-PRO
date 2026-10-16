package com.example.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataManager
import com.example.data.model.Ayah
import com.example.data.model.Bookmark
import com.example.data.quran.QuranCatalog
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    dataManager: DataManager,
    onBackClick: () -> Unit
) {
    val surahMeta = remember(surahNumber) {
        QuranCatalog.allSurahsMetadata.find { it.number == surahNumber }
    }
    val surahName = surahMeta?.nameArabic ?: "سورة"
    val verses = remember(surahNumber) {
        QuranCatalog.preBundledVerses[surahNumber] ?: emptyList()
    }

    var fontSize by remember { mutableFloatStateOf(dataManager.quranFontSize) }
    var isDistractionFree by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showBookmarkAddedSnackbar by remember { mutableStateOf(false) }

    // Auto-save last read location on screen launch
    LaunchedEffect(surahNumber) {
        dataManager.lastReadSurah = surahNumber
        dataManager.lastReadSurahName = surahName
        if (verses.isNotEmpty()) {
            dataManager.lastReadAyah = verses.first().numberInSurah
        } else {
            dataManager.lastReadAyah = 1
        }
    }

    Scaffold(
        topBar = {
            if (!isDistractionFree) {
                MuslimTopBar(
                    title = "سورة $surahName",
                    subtitle = "${surahMeta?.revelationType?.labelArabic ?: ""} • ${surahMeta?.ayahsCount ?: 0} آيات • الجزء ${surahMeta?.juzStart ?: 1}",
                    onBackClick = onBackClick,
                    actions = {
                        IconButton(
                            onClick = { showFontDialog = true },
                            modifier = Modifier.testTag("reader_font_size_button")
                        ) {
                            Icon(Icons.Default.FormatSize, contentDescription = "تغيير حجم الخط", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(
                            onClick = { showJumpDialog = true },
                            modifier = Modifier.testTag("reader_jump_button")
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = "الانتقال إلى آية", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(
                            onClick = { isDistractionFree = true },
                            modifier = Modifier.testTag("reader_distraction_free_button")
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "قراءة دون مشتتات", tint = IslamicGreenPrimary)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (isDistractionFree) {
                FloatingActionButton(
                    onClick = { isDistractionFree = false },
                    containerColor = IslamicGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("exit_distraction_free_fab")
                ) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "إنهاء وضع ملء الشاشة")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isDistractionFree) PaddingValues(0.dp) else innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Surah Header Frame
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, IslamicGold.copy(alpha = 0.4f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "سُورَةُ $surahName",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${surahMeta?.revelationType?.labelArabic ?: ""} • ${surahMeta?.ayahsCount ?: 0} آيات • الجزء ${surahMeta?.juzStart ?: 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Bismillah (except for Surah At-Tawbah, surah #9)
                            if (surahNumber != 9) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = (fontSize * 0.95f).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IslamicGold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (verses.isNotEmpty()) {
                    items(verses, key = { it.numberInSurah }) { ayah ->
                        AyahCard(
                            ayah = ayah,
                            fontSize = fontSize,
                            onBookmark = {
                                dataManager.saveBookmark(
                                    Bookmark(
                                        id = UUID.randomUUID().toString(),
                                        surahNumber = surahNumber,
                                        surahName = surahName,
                                        ayahNumber = ayah.numberInSurah
                                    )
                                )
                                dataManager.lastReadSurah = surahNumber
                                dataManager.lastReadAyah = ayah.numberInSurah
                                dataManager.lastReadSurahName = surahName
                                showBookmarkAddedSnackbar = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    // Authentic message when a full Surah is not pre-packaged in the initial lightweight APK bundle
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = IslamicGold,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "فهرس السورة متاح ومعتمد",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "سورة $surahName (${surahMeta?.ayahsCount} آيات - ${surahMeta?.revelationType?.labelArabic}).\n\nحرصاً والتزاماً بقدسية كتاب الله ودقته وتجنباً لأي نصوص غير مدققة، يتضمن التطبيق السور الأساسية مسبقاً، ويتيح استيراد نص المصحف كاملاً بنقرة واحدة عبر ملف JSON المعتمد محلياً.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onBackClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("الرجوع لفهرس السور واستيراد البيانات")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Font size adjustment dialog
    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text("حجم خط القراءة") },
            text = {
                Column {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = fontSize.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = fontSize,
                        onValueChange = {
                            fontSize = it
                            dataManager.quranFontSize = it
                        },
                        valueRange = 18f..40f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = IslamicGreenPrimary,
                            activeTrackColor = IslamicGreenPrimary
                        )
                    )
                    Text(
                        text = "${fontSize.toInt()} نقطة",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontDialog = false }) {
                    Text("تم", color = IslamicGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Jump to Ayah dialog
    if (showJumpDialog) {
        var inputAyah by remember { mutableStateOf("") }
        val maxAyahs = surahMeta?.ayahsCount ?: 1
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("الانتقال إلى آية") },
            text = {
                Column {
                    Text("أدخل رقم الآية (1 إلى $maxAyahs):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputAyah,
                        onValueChange = { inputAyah = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputAyah.toIntOrNull()
                        if (num != null && num in 1..maxAyahs) {
                            dataManager.lastReadAyah = num
                            showJumpDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                ) {
                    Text("انتقال")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Bookmark added snackbar/toast
    if (showBookmarkAddedSnackbar) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showBookmarkAddedSnackbar = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IslamicGreenPrimary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.BookmarkAdded, contentDescription = null, tint = IslamicGoldBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تم حفظ العلامة المرجعية بنجاح",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    fontSize: Float,
    onBookmark: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Verse Text
            Text(
                text = "${ayah.textArabic}  ۝${convertArabicNumbers(ayah.numberInSurah)}",
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.8f).sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آية ${ayah.numberInSurah}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "حفظ علامة",
                        tint = IslamicGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun convertArabicNumbers(number: Int): String {
    val arabicDigits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = number.toString()
    val sb = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(arabicDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}
