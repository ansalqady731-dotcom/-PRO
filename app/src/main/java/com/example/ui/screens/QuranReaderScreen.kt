package com.example.ui.screens

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
import com.example.data.local.DataManager
import com.example.data.model.Ayah
import com.example.data.model.Bookmark
import com.example.data.quran.QuranCatalog
import com.example.data.quran.SurahQuranImporter
import com.example.data.quran.TajweedColorizer
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    dataManager: DataManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val surahMeta = remember(surahNumber) {
        QuranCatalog.allSurahsMetadata.find { it.number == surahNumber }
    }
    val surahName = surahMeta?.nameArabic ?: "سورة"

    // Load verses from cache or pre-bundled
    var verses by remember(surahNumber) {
        mutableStateOf(
            dataManager.getCachedSurahVerses(surahNumber)
                ?: QuranCatalog.preBundledVerses[surahNumber]
                ?: emptyList()
        )
    }

    var isTajweedMode by remember { mutableStateOf(dataManager.isTajweedModeEnabled) }
    var showTajweedLegend by remember { mutableStateOf(false) }
    var isFetchingFromWeb by remember { mutableStateOf(false) }
    var fetchErrorMessage by remember { mutableStateOf<String?>(null) }

    var fontSize by remember { mutableFloatStateOf(dataManager.quranFontSize) }
    var isDistractionFree by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showBookmarkAddedSnackbar by remember { mutableStateOf(false) }

    // Auto-save last read location on screen launch
    LaunchedEffect(surahNumber, verses) {
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
                    subtitle = "${surahMeta?.revelationType?.labelArabic ?: ""} • ${surahMeta?.ayahsCount ?: 0} آيات • مصحف التجويد",
                    onBackClick = onBackClick,
                    actions = {
                        IconButton(
                            onClick = {
                                val next = !isTajweedMode
                                isTajweedMode = next
                                dataManager.isTajweedModeEnabled = next
                            },
                            modifier = Modifier.testTag("toggle_tajweed_mode_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "تلوين التجويد",
                                tint = if (isTajweedMode) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
            // Tajweed Mode Banner & Legend Toggle
            if (!isDistractionFree) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isTajweedMode) IslamicGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (isTajweedMode) "مصحف التجويد الملون: مُفعل" else "رسم المصحف العادي",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTajweedMode) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            TextButton(
                                onClick = { showTajweedLegend = !showTajweedLegend },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (showTajweedLegend) "إخفاء مفتاح الألوان" else "مفتاح ألوان التجويد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicGold
                                )
                            }
                        }

                        // Collapsible Tajweed Color Legend
                        AnimatedVisibility(visible = showTajweedLegend) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    text = "دليل الألوان التجويدية المعتمد (surahquran.com / دار المعرفة):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TajweedColorizer.legendItems.forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(item.color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${item.title}: ${item.description}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Surah Header Frame
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
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
                            isTajweed = isTajweedMode,
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
                    // Fetch from surahquran.com card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = IslamicGold,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "استيراد سورة $surahName من موقع سورة قرآن",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "يمكنك الآن استيراد نص السورة بالرسم العثماني المعتمد مباشرة من موقع surahquran.com بنقرة واحدة وحفظها لتعمل دون إنترنت.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (fetchErrorMessage != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = fetchErrorMessage ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        isFetchingFromWeb = true
                                        fetchErrorMessage = null
                                        coroutineScope.launch {
                                            val result = SurahQuranImporter.fetchSurah(surahNumber)
                                            isFetchingFromWeb = false
                                            if (result.isSuccess) {
                                                val fetched = result.getOrNull() ?: emptyList()
                                                verses = fetched
                                                dataManager.saveCachedSurahVerses(surahNumber, fetched)
                                                Toast.makeText(context, "تم استيراد سورة $surahName بنجاح وحفظها أوفلاين", Toast.LENGTH_LONG).show()
                                            } else {
                                                fetchErrorMessage = result.exceptionOrNull()?.message ?: "تعذر استيراد السورة"
                                            }
                                        }
                                    },
                                    enabled = !isFetchingFromWeb,
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("import_surah_from_web_btn")
                                ) {
                                    if (isFetchingFromWeb) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جارٍ الاستيراد من surahquran.com...")
                                    } else {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("استيراد وتحميل السورة الآن")
                                    }
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

    // Bookmark added snackbar
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
    isTajweed: Boolean,
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
            val normalTextColor = MaterialTheme.colorScheme.onSurface
            val verseSuffix = "  ۝${convertArabicNumbers(ayah.numberInSurah)}"

            if (isTajweed) {
                val tajweedAnnotated = remember(ayah.textArabic, normalTextColor) {
                    TajweedColorizer.colorize(ayah.textArabic + verseSuffix, normalTextColor)
                }
                Text(
                    text = tajweedAnnotated,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.85f).sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "${ayah.textArabic}$verseSuffix",
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.85f).sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Right,
                    color = normalTextColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
