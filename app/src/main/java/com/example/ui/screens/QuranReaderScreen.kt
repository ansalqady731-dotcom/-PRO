package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DataManager
import com.example.data.model.Ayah
import com.example.data.model.Bookmark
import com.example.data.model.Reciter
import com.example.data.quran.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Professional Quran Reader Screen providing authentic Madina Mushaf pages layout,
 * color-coded Tajweed, interactive Ayah actions, Tafsir Al-Mukhtasar, sharing (text & image),
 * and font customization inspired by the Madina Mushaf design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    initialPageNumber: Int? = null,
    dataManager: DataManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (dataManager.themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    // Determine initial page number from surah or parameter
    val defaultPage = remember(surahNumber) {
        QuranRepository.getSurah(context, surahNumber)?.ayahs?.firstOrNull()?.page ?: 1
    }
    var currentPageNum by remember { mutableIntStateOf(initialPageNumber ?: defaultPage) }

    // Reading Settings & Modes
    var quranViewMode by remember { mutableStateOf(dataManager.quranViewMode) } // "mushaf_page" or "flexible_list"
    var quranFontSize by remember { mutableFloatStateOf(dataManager.quranFontSize) }
    var quranFontFamily by remember { mutableStateOf(dataManager.quranFontFamily) }
    var isTajweedMode by remember { mutableStateOf(dataManager.isTajweedModeEnabled) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var showFontSettingsSheet by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showTajweedLegend by remember { mutableStateOf(false) }
    var showReciterSheet by remember { mutableStateOf(false) }

    // Ayah Interaction States (Matches Screenshot 1 & 2)
    var selectedAyahForAction by remember { mutableStateOf<Ayah?>(null) }
    var selectedAyahForShare by remember { mutableStateOf<Ayah?>(null) }
    var selectedAyahForNote by remember { mutableStateOf<Ayah?>(null) }

    // Audio Playback states
    val isPlaying by QuranAudioPlayer.isPlaying.collectAsState()
    val playingSurah by QuranAudioPlayer.currentPlayingSurah.collectAsState()
    val playingAyah by QuranAudioPlayer.currentPlayingAyah.collectAsState()
    val selectedReciter by QuranAudioPlayer.selectedReciter.collectAsState()

    // Load authentic ayahs on the current page (from 1 to 604)
    val pageAyahs = remember(currentPageNum) {
        QuranRepository.getAyahsByPage(context, currentPageNum)
    }

    // Page metadata
    val primarySurahNumber = pageAyahs.firstOrNull()?.surahNumber ?: surahNumber
    val primarySurahMeta = remember(primarySurahNumber) {
        QuranCatalog.allSurahsMetadata.find { it.number == primarySurahNumber }
    }
    val surahName = primarySurahMeta?.nameArabic ?: "سورة"
    val juzNumber = pageAyahs.firstOrNull()?.juz ?: 1
    val hizbQuarter = pageAyahs.firstOrNull()?.hizbQuarter ?: 1

    // Group ayahs on this page by Surah
    val ayahsBySurah = remember(pageAyahs) {
        pageAyahs.groupBy { it.surahNumber }
    }

    // Auto-save last read location
    LaunchedEffect(currentPageNum, pageAyahs) {
        dataManager.lastReadPage = currentPageNum
        dataManager.lastReadSurah = primarySurahNumber
        dataManager.lastReadSurahName = surahName
        if (pageAyahs.isNotEmpty()) {
            dataManager.lastReadAyah = pageAyahs.first().numberInSurah
        }
    }

    // Font Family resolution
    val resolvedQuranFont = remember(quranFontFamily) {
        when (quranFontFamily) {
            "amiri" -> AmiriFontFamily
            "scheherazade_new" -> ScheherazadeFontFamily
            else -> AmiriQuranFontFamily
        }
    }

    // Gesture detection for swiping between pages
    var totalDragAmount by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                MuslimTopBar(
                    title = "صفحة ${toArabicNumerals(currentPageNum)} • سورة $surahName",
                    subtitle = "الجزء ${toArabicNumerals(juzNumber)} • الحزب ${(hizbQuarter - 1) / 4 + 1}",
                    onBackClick = onBackClick,
                    actions = {
                        // Toggle between Mushaf Page and Flexible Reading
                        IconButton(
                            onClick = {
                                val next = if (quranViewMode == "mushaf_page") "flexible_list" else "mushaf_page"
                                quranViewMode = next
                                dataManager.quranViewMode = next
                                val msg = if (next == "mushaf_page") "تم التحويل إلى صفحات مصحف المدينة" else "تم التحويل إلى وضع القراءة المرنة"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("toggle_quran_view_mode")
                        ) {
                            Icon(
                                imageVector = if (quranViewMode == "mushaf_page") Icons.Outlined.AutoStories else Icons.Outlined.ViewAgenda,
                                contentDescription = "تغيير طريقة العرض",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Toggle Tajweed Colors
                        IconButton(
                            onClick = {
                                val next = !isTajweedMode
                                isTajweedMode = next
                                dataManager.isTajweedModeEnabled = next
                            },
                            modifier = Modifier.testTag("toggle_tajweed_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "ألوان التجويد",
                                tint = if (isTajweedMode) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Font & Display Settings (Screenshot 3)
                        IconButton(
                            onClick = { showFontSettingsSheet = true },
                            modifier = Modifier.testTag("reader_font_settings_btn")
                        ) {
                            Icon(Icons.Default.FormatSize, contentDescription = "إعدادات الخط", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        // Jump to page / surah
                        IconButton(
                            onClick = { showJumpDialog = true },
                            modifier = Modifier.testTag("reader_jump_dialog_btn")
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "الانتقال السريع", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        // Fullscreen reading toggle
                        IconButton(
                            onClick = { isFullscreen = true },
                            modifier = Modifier.testTag("reader_fullscreen_btn")
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "ملء الشاشة", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                )
            }
        },
        bottomBar = {
            Column {
                // Persistent Audio Recitation Bar
                if (isPlaying || playingAyah != null) {
                    AudioRecitationBar(
                        isPlaying = isPlaying,
                        currentSurah = playingSurah,
                        currentAyah = playingAyah,
                        selectedReciter = selectedReciter,
                        onPlayPause = { QuranAudioPlayer.togglePlayPause() },
                        onStop = { QuranAudioPlayer.stop() },
                        onNext = { QuranAudioPlayer.playNextAyah() },
                        onPrevious = { QuranAudioPlayer.playPreviousAyah() },
                        onChangeReciter = { showReciterSheet = true }
                    )
                }

                // Page Navigation Bar (unless in fullscreen mode)
                if (!isFullscreen) {
                    PageNavigationBottomBar(
                        currentPage = currentPageNum,
                        totalPages = 604,
                        onPrevPage = {
                            if (currentPageNum > 1) currentPageNum--
                        },
                        onNextPage = {
                            if (currentPageNum < 604) currentPageNum++
                        },
                        onJumpClick = { showJumpDialog = true }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullscreen) PaddingValues(0.dp) else innerPadding)
                .pointerInput(currentPageNum) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            totalDragAmount += dragAmount
                        },
                        onDragEnd = {
                            // In RTL, swipe left goes to next page, swipe right goes to previous page
                            if (totalDragAmount > 80) {
                                if (currentPageNum > 1) currentPageNum--
                            } else if (totalDragAmount < -80) {
                                if (currentPageNum < 604) currentPageNum++
                            }
                            totalDragAmount = 0f
                        }
                    )
                }
        ) {
            if (quranViewMode == "mushaf_page") {
                // 1. Authentic Madina Mushaf Page Layout (Matching Screenshot 5 & 2)
                QuranPageFrame(
                    pageNumber = currentPageNum,
                    surahName = surahName,
                    juzNumber = juzNumber,
                    hizbQuarter = hizbQuarter,
                    isDarkTheme = isDarkTheme
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ayahsBySurah.forEach { (sNum, sAyahs) ->
                            val meta = QuranCatalog.allSurahsMetadata.find { it.number == sNum }
                            val sName = meta?.nameArabic ?: "سورة"

                            // If this page contains verse 1 of this surah, render the ornate Surah Header Banner
                            val startsOnThisPage = sAyahs.any { it.numberInSurah == 1 }
                            if (startsOnThisPage) {
                                item(key = "header_$sNum") {
                                    QuranSurahHeaderBanner(
                                        surahName = sName,
                                        ayahsCount = meta?.ayahsCount ?: sAyahs.size,
                                        revelationTypeLabel = meta?.revelationType?.labelArabic ?: "مكية",
                                        isDarkTheme = isDarkTheme
                                    )
                                    // Bismillah (Omitted for Surah At-Tawbah 9, and Al-Fatiha 1 already has it as Ayah 1)
                                    if (sNum != 9 && sNum != 1) {
                                        QuranBasmalah(isDarkTheme = isDarkTheme)
                                    }
                                }
                            }

                            // Render verses of this Surah on this page
                            item(key = "verses_$sNum") {
                                FlowingVersesBlock(
                                    ayahs = sAyahs,
                                    surahName = sName,
                                    fontFamily = resolvedQuranFont,
                                    fontSize = quranFontSize,
                                    isTajweed = isTajweedMode,
                                    isDarkTheme = isDarkTheme,
                                    activePlayingAyah = if (playingSurah == sNum) playingAyah else null,
                                    selectedAyah = selectedAyahForAction,
                                    onAyahClick = { ayah ->
                                        selectedAyahForAction = ayah
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // 2. Flexible Reading Mode (Card by Card with larger text & Tafsir toggles)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ayahsBySurah.forEach { (sNum, sAyahs) ->
                        val meta = QuranCatalog.allSurahsMetadata.find { it.number == sNum }
                        val sName = meta?.nameArabic ?: "سورة"

                        item(key = "flex_header_$sNum") {
                            QuranSurahHeaderBanner(
                                surahName = sName,
                                ayahsCount = meta?.ayahsCount ?: sAyahs.size,
                                revelationTypeLabel = meta?.revelationType?.labelArabic ?: "مكية",
                                isDarkTheme = isDarkTheme
                            )
                            if (sNum != 9 && sNum != 1 && sAyahs.any { it.numberInSurah == 1 }) {
                                QuranBasmalah(isDarkTheme = isDarkTheme)
                            }
                        }

                        items(sAyahs, key = { "${it.surahNumber}_${it.numberInSurah}" }) { ayah ->
                            FlexibleAyahCard(
                                ayah = ayah,
                                surahName = sName,
                                fontFamily = resolvedQuranFont,
                                fontSize = quranFontSize,
                                isTajweed = isTajweedMode,
                                isPlaying = (playingSurah == ayah.surahNumber && playingAyah == ayah.numberInSurah),
                                isFavorite = dataManager.isAyahFavorite(ayah.surahNumber, ayah.numberInSurah),
                                onAyahClick = { selectedAyahForAction = ayah },
                                onShareClick = { selectedAyahForShare = ayah },
                                onNoteClick = { selectedAyahForNote = ayah }
                            )
                        }
                    }
                }
            }

            // Exit Fullscreen Floating Button
            if (isFullscreen) {
                SmallFloatingActionButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = IslamicGreenPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "إنهاء ملء الشاشة")
                }
            }
        }
    }

    // --- DIALOGS AND BOTTOM SHEETS ---

    // 1. Ayah Action & Tafsir Bottom Sheet (Screenshot 2)
    selectedAyahForAction?.let { ayah ->
        val sMeta = QuranCatalog.allSurahsMetadata.find { it.number == ayah.surahNumber }
        AyahActionBottomSheet(
            ayah = ayah,
            surahName = sMeta?.nameArabic ?: surahName,
            dataManager = dataManager,
            onDismiss = { selectedAyahForAction = null },
            onOpenShare = { selectedAyahForShare = it },
            onOpenAddNote = { selectedAyahForNote = it }
        )
    }

    // 2. Ayah Share Dialog (Screenshot 1)
    selectedAyahForShare?.let { ayah ->
        val sMeta = QuranCatalog.allSurahsMetadata.find { it.number == ayah.surahNumber }
        AyahShareDialog(
            ayah = ayah,
            surahName = sMeta?.nameArabic ?: surahName,
            onDismiss = { selectedAyahForShare = null }
        )
    }

    // 3. Add Reflection / Tadabbur Note Dialog (Screenshot 2 - إضافة خاطرة)
    selectedAyahForNote?.let { ayah ->
        val sMeta = QuranCatalog.allSurahsMetadata.find { it.number == ayah.surahNumber }
        AddTadabburNoteDialog(
            ayah = ayah,
            surahName = sMeta?.nameArabic ?: surahName,
            dataManager = dataManager,
            onDismiss = { selectedAyahForNote = null }
        )
    }

    // 4. Font & Reading Settings Sheet (Screenshot 3)
    if (showFontSettingsSheet) {
        QuranFontSettingsSheet(
            dataManager = dataManager,
            onDismiss = { showFontSettingsSheet = false },
            onSettingsChanged = {
                quranFontSize = dataManager.quranFontSize
                quranFontFamily = dataManager.quranFontFamily
                quranViewMode = dataManager.quranViewMode
            }
        )
    }

    // 5. Jump to Page or Surah Dialog
    if (showJumpDialog) {
        JumpToPageDialog(
            currentPage = currentPageNum,
            onPageSelected = { page ->
                currentPageNum = page.coerceIn(1, 604)
                showJumpDialog = false
            },
            onSurahSelected = { surah ->
                val targetPage = surah.ayahs.firstOrNull()?.page ?: 1
                currentPageNum = targetPage
                showJumpDialog = false
            },
            allSurahs = QuranCatalog.allSurahsMetadata,
            onDismiss = { showJumpDialog = false }
        )
    }

    // 6. Reciter Selection Sheet
    if (showReciterSheet) {
        ReciterSelectionBottomSheet(
            selectedReciter = selectedReciter,
            onReciterSelected = {
                QuranAudioPlayer.setReciter(it)
                showReciterSheet = false
            },
            onDismiss = { showReciterSheet = false }
        )
    }
}

/**
 * Renders the verses of a page as a unified, flowing Quranic block,
 * where each verse is selectable and ends with the traditional Quranic roundel.
 */
@Composable
fun FlowingVersesBlock(
    ayahs: List<Ayah>,
    surahName: String,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    fontSize: Float,
    isTajweed: Boolean,
    isDarkTheme: Boolean,
    activePlayingAyah: Int?,
    selectedAyah: Ayah?,
    onAyahClick: (Ayah) -> Unit
) {
    val defaultTextColor = if (isDarkTheme) Color(0xFFEFE8DA) else Color(0xFF221A0F)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ayahs.forEach { ayah ->
            val isSelected = selectedAyah?.numberInSurah == ayah.numberInSurah && selectedAyah.surahNumber == ayah.surahNumber
            val isAudioPlaying = activePlayingAyah == ayah.numberInSurah

            val bgColor = when {
                isSelected -> QuranAyahHighlightGold
                isAudioPlaying -> IslamicGreenPrimary.copy(alpha = 0.18f)
                else -> Color.Transparent
            }

            val annotatedAyah = remember(ayah.textArabic, isTajweed, defaultTextColor) {
                if (isTajweed) {
                    TajweedColorizer.colorize(ayah.textArabic, defaultTextColor)
                } else {
                    buildAnnotatedString {
                        append(ayah.textArabic)
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onAyahClick(ayah) },
                color = bgColor,
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(annotatedAyah)
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    color = QuranFrameBorderGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (fontSize * 0.85).sp
                                )
                            ) {
                                append("۝${toArabicNumerals(ayah.numberInSurah)}")
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = fontFamily,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.8).sp,
                            textAlign = TextAlign.Justify
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Card for Flexible Reading Mode
 */
@Composable
fun FlexibleAyahCard(
    ayah: Ayah,
    surahName: String,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    fontSize: Float,
    isTajweed: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onAyahClick: () -> Unit,
    onShareClick: () -> Unit,
    onNoteClick: () -> Unit
) {
    val context = LocalContext.current
    val tafsirText = remember(ayah) {
        QuranRepository.getTafsir(context, ayah.surahNumber, ayah.numberInSurah)
    }
    var showTafsirInline by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAyahClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) IslamicGreenLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlaying) IslamicGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Ayah number & Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = IslamicGreenPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "الآية ${toArabicNumerals(ayah.numberInSurah)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Share, contentDescription = "مشاركة", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onNoteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.EditNote, contentDescription = "خاطرة", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { showTafsirInline = !showTafsirInline },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showTafsirInline) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "التفسير",
                            tint = if (showTafsirInline) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ayah Text
            val textAnnotated = if (isTajweed) {
                TajweedColorizer.colorize(ayah.textArabic, MaterialTheme.colorScheme.onSurface)
            } else {
                buildAnnotatedString { append(ayah.textArabic) }
            }

            Text(
                text = textAnnotated,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = fontFamily,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.7).sp,
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Inline Tafsir expansion
            AnimatedVisibility(visible = showTafsirInline) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "المختصر في التفسير:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tafsirText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Bottom Navigation Bar for paging through the 604 pages of the Quran
 */
@Composable
fun PageNavigationBottomBar(
    currentPage: Int,
    totalPages: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onJumpClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // In RTL, "السابق" decreases page, "التالي" increases page
            OutlinedButton(
                onClick = onPrevPage,
                enabled = currentPage > 1,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("السابقة")
            }

            // Clickable Center Page Indicator
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onJumpClick() },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "صفحة ${toArabicNumerals(currentPage)} من ${toArabicNumerals(totalPages)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            OutlinedButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("التالية")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * Persistent Audio Recitation Bar for listening to Ayahs
 */
@Composable
fun AudioRecitationBar(
    isPlaying: Boolean,
    currentSurah: Int?,
    currentAyah: Int?,
    selectedReciter: Reciter,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onChangeReciter: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = IslamicGreenPrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onChangeReciter() }
            ) {
                val surahMeta = remember(currentSurah) {
                    QuranCatalog.allSurahsMetadata.find { it.number == currentSurah }
                }
                Text(
                    text = "سورة ${surahMeta?.nameArabic ?: ""} - الآية ${toArabicNumerals(currentAyah ?: 1)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "القارئ: ${selectedReciter.nameArabic} (انقر للتغيير)",
                    style = MaterialTheme.typography.labelSmall,
                    color = IslamicGoldBright
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "السابق", tint = Color.White)
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(44.dp)
                        .background(IslamicGold, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل/إيقاف",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "التالي", tint = Color.White)
                }

                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Close, contentDescription = "إيقاف", tint = Color.White)
                }
            }
        }
    }
}

/**
 * Quick Jump to Page or Surah Dialog
 */
@Composable
fun JumpToPageDialog(
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onSurahSelected: (com.example.data.model.Surah) -> Unit,
    allSurahs: List<com.example.data.model.Surah>,
    onDismiss: () -> Unit
) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: رقم الصفحة, 1: السورة

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
                    text = "الانتقال السريع في المصحف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreenPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("رقم الصفحة (1-604)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("السور (114)") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = pageInput,
                        onValueChange = { pageInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("أدخل رقم الصفحة (1 - 604)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val p = pageInput.toIntOrNull() ?: 1
                                onPageSelected(p)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                        ) {
                            Text("انتقال")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        items(allSurahs, key = { it.number }) { surah ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSurahSelected(surah) }
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${surah.number}. ${surah.nameArabic}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "صفحة ${surah.ayahs.firstOrNull()?.page ?: 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * Bottom Sheet for Selecting Audio Reciter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterSelectionBottomSheet(
    selectedReciter: Reciter,
    onReciterSelected: (Reciter) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "اختيار القارئ للتلاوة المرتلة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IslamicGreenPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(QuranAudioPlayer.reciters, key = { it.id }) { reciter ->
                    val isCurrent = reciter.id == selectedReciter.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReciterSelected(reciter) }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrent) IslamicGreenPrimary.copy(alpha = 0.12f) else Color.Transparent,
                        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, IslamicGreenPrimary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = reciter.nameArabic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "تلاوة مرتلة برواية حفص عن عاصم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = IslamicGreenPrimary)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
