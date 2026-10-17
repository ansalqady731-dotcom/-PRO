package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
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
import com.example.data.model.Reciter
import com.example.data.model.WordMeaning
import com.example.data.quran.*
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
    val listState = rememberLazyListState()

    var currentSurahNum by remember { mutableIntStateOf(surahNumber) }

    val surahMeta = remember(currentSurahNum) {
        QuranCatalog.allSurahsMetadata.find { it.number == currentSurahNum }
    }
    val surahName = surahMeta?.nameArabic ?: "سورة"

    // Load authentic verses directly from local QuranRepository (all 114 Surahs offline)
    var verses by remember(currentSurahNum) {
        mutableStateOf(
            QuranRepository.getSurah(context, currentSurahNum)?.ayahs
                ?: dataManager.getCachedSurahVerses(currentSurahNum)
                ?: QuranCatalog.preBundledVerses[currentSurahNum]
                ?: emptyList()
        )
    }

    var isTajweedMode by remember { mutableStateOf(dataManager.isTajweedModeEnabled) }
    var showTajweedLegend by remember { mutableStateOf(false) }

    var fontSize by remember { mutableFloatStateOf(dataManager.quranFontSize) }
    var isDistractionFree by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showReciterSheet by remember { mutableStateOf(false) }

    // Selected Ayah for Tafsir & Word Meanings Modal Sheet
    var selectedAyahForDetails by remember { mutableStateOf<Ayah?>(null) }
    var showBookmarkAddedSnackbar by remember { mutableStateOf(false) }

    // Audio Player states
    val isPlaying by QuranAudioPlayer.isPlaying.collectAsState()
    val playingSurah by QuranAudioPlayer.currentPlayingSurah.collectAsState()
    val playingAyah by QuranAudioPlayer.currentPlayingAyah.collectAsState()
    val selectedReciter by QuranAudioPlayer.selectedReciter.collectAsState()

    // Auto-scroll to currently playing ayah
    LaunchedEffect(playingAyah) {
        if (playingSurah == currentSurahNum && playingAyah != null && verses.isNotEmpty()) {
            val idx = verses.indexOfFirst { it.numberInSurah == playingAyah }
            if (idx >= 0) {
                // +1 for header item
                listState.animateScrollToItem((idx + 1).coerceAtMost(verses.size))
            }
        }
    }

    // Auto-save last read location on screen launch
    LaunchedEffect(currentSurahNum, verses) {
        dataManager.lastReadSurah = currentSurahNum
        dataManager.lastReadSurahName = surahName
        if (verses.isNotEmpty()) {
            dataManager.lastReadAyah = verses.first().numberInSurah
            dataManager.lastReadPage = verses.first().page
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
        bottomBar = {
            if (!isDistractionFree) {
                QuranAudioPlayerBar(
                    isPlaying = isPlaying && playingSurah == currentSurahNum,
                    playingAyah = if (playingSurah == currentSurahNum) playingAyah else null,
                    totalAyahs = surahMeta?.ayahsCount ?: 1,
                    selectedReciter = selectedReciter,
                    onTogglePlay = {
                        if (playingSurah == currentSurahNum && isPlaying) {
                            QuranAudioPlayer.togglePlayPause()
                        } else {
                            val startAyah = playingAyah ?: 1
                            QuranAudioPlayer.playAyah(currentSurahNum, startAyah, surahMeta?.ayahsCount ?: 1)
                        }
                    },
                    onNextAyah = { QuranAudioPlayer.playNextAyah() },
                    onPrevAyah = { QuranAudioPlayer.playPreviousAyah() },
                    onChangeReciter = { showReciterSheet = true }
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
                                    text = "دليل الألوان التجويدية المعتمد (مصحف دار المعرفة / الأزهر):",
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
                state = listState,
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
                            if (currentSurahNum != 9) {
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

                // Render Verses
                if (verses.isNotEmpty()) {
                    items(verses, key = { it.numberInSurah }) { ayah ->
                        val isCurrentPlaying = (playingSurah == currentSurahNum && playingAyah == ayah.numberInSurah)
                        AyahCard(
                            ayah = ayah,
                            fontSize = fontSize,
                            isTajweed = isTajweedMode,
                            isPlaying = isCurrentPlaying,
                            onClick = { selectedAyahForDetails = ayah },
                            onPlayAudio = {
                                QuranAudioPlayer.playAyah(
                                    currentSurahNum,
                                    ayah.numberInSurah,
                                    surahMeta?.ayahsCount ?: 1
                                )
                            },
                            onBookmark = {
                                dataManager.saveBookmark(
                                    Bookmark(
                                        id = UUID.randomUUID().toString(),
                                        surahNumber = currentSurahNum,
                                        surahName = surahName,
                                        ayahNumber = ayah.numberInSurah,
                                        page = ayah.page
                                    )
                                )
                                dataManager.lastReadSurah = currentSurahNum
                                dataManager.lastReadAyah = ayah.numberInSurah
                                dataManager.lastReadPage = ayah.page
                                dataManager.lastReadSurahName = surahName
                                showBookmarkAddedSnackbar = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Surah Navigation Footer (Next / Prev Surah)
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentSurahNum > 1) {
                                Button(
                                    onClick = {
                                        currentSurahNum -= 1
                                        verses = QuranRepository.getSurah(context, currentSurahNum)?.ayahs ?: emptyList()
                                        coroutineScope.launch { listState.scrollToItem(0) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("السورة السابقة", color = MaterialTheme.colorScheme.onSurface)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            if (currentSurahNum < 114) {
                                Button(
                                    onClick = {
                                        currentSurahNum += 1
                                        verses = QuranRepository.getSurah(context, currentSurahNum)?.ayahs ?: emptyList()
                                        coroutineScope.launch { listState.scrollToItem(0) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("السورة التالية", color = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = IslamicGreenPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("جارٍ تحميل آيات سورة $surahName بالرسم العثماني...")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Details Bottom Sheet for Ayah (Tafsir + Word Meanings + Recite + Copy + Bookmark)
    if (selectedAyahForDetails != null) {
        val ayah = selectedAyahForDetails!!
        val tafsirText = remember(ayah.numberInSurah, currentSurahNum) {
            QuranRepository.getTafsir(context, currentSurahNum, ayah.numberInSurah)
        }
        val wordMeanings = remember(ayah.textArabic) {
            QuranWordMeanings.extractWordMeaningsForAyah(ayah.textArabic)
        }

        ModalBottomSheet(
            onDismissRequest = { selectedAyahForDetails = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxHeight(0.85f)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سورة $surahName • آية ${ayah.numberInSurah} (صفحة ${ayah.page})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary
                    )
                    IconButton(onClick = { selectedAyahForDetails = null }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ayah Uthmani calligraphy
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val normalColor = MaterialTheme.colorScheme.onSurface
                            val styledText = if (isTajweedMode) {
                                TajweedColorizer.colorize(ayah.textArabic, normalColor)
                            } else {
                                androidx.compose.ui.text.AnnotatedString(ayah.textArabic)
                            }

                            Text(
                                text = styledText,
                                fontSize = (fontSize * 0.9f).sp,
                                lineHeight = (fontSize * 1.6f).sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Action buttons (Audio Recite, Copy, Bookmark)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    QuranAudioPlayer.playAyah(
                                        currentSurahNum,
                                        ayah.numberInSurah,
                                        surahMeta?.ayahsCount ?: 1
                                    )
                                    selectedAyahForDetails = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تلاوة الآية")
                            }

                            OutlinedButton(
                                onClick = {
                                    val clip = ClipData.newPlainText(
                                        "آية قرآنية",
                                        "«${ayah.textArabic}» [سورة $surahName: ${ayah.numberInSurah}]\n\nالتفسير الميسر:\n$tafsirText"
                                    )
                                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ الآية وتفسيرها", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نسخ الآية")
                            }
                        }
                    }

                    // Tafsir Al-Muyassar Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = IslamicGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "التفسير الميسر (معتمد أوفلاين):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = tafsirText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Word Meanings Section
                    if (wordMeanings.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Translate,
                                            contentDescription = null,
                                            tint = IslamicGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "معاني الكلمات الغريبة في الآية:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    wordMeanings.forEach { wm ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "«${wm.word}»:",
                                                fontWeight = FontWeight.Bold,
                                                color = IslamicGreenPrimary,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = wm.meaning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
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
    }

    // Reciters Selector Bottom Sheet
    if (showReciterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReciterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختيار القارئ للتلاوة المباركة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreenPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
                QuranAudioPlayer.reciters.forEach { reciter ->
                    val isSel = (reciter.id == selectedReciter.id)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                QuranAudioPlayer.setReciter(reciter)
                                showReciterSheet = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) IslamicGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, IslamicGreenPrimary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = if (isSel) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = reciter.nameArabic,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSel) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "محدد", tint = IslamicGreenPrimary)
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
                            val idx = verses.indexOfFirst { it.numberInSurah == num }
                            if (idx >= 0) {
                                coroutineScope.launch { listState.animateScrollToItem(idx + 1) }
                            }
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
                .padding(bottom = 80.dp),
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
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit,
    onBookmark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isPlaying) 2.dp else 0.dp,
                color = if (isPlaying) IslamicGold else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) IslamicGreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 3.dp else 1.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "آية ${ayah.numberInSurah} • ص ${ayah.page} • ج ${ayah.juz}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• انقر للتفسير والمعاني",
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicGold,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayCircleOutline,
                            contentDescription = "استماع للآية",
                            tint = if (isPlaying) IslamicGold else IslamicGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

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
}

@Composable
fun QuranAudioPlayerBar(
    isPlaying: Boolean,
    playingAyah: Int?,
    totalAyahs: Int,
    selectedReciter: Reciter,
    onTogglePlay: () -> Unit,
    onNextAyah: () -> Unit,
    onPrevAyah: () -> Unit,
    onChangeReciter: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Reciter Info & Switch button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onChangeReciter() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = selectedReciter.nameArabic,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "تغيير القارئ",
                        tint = IslamicGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = if (playingAyah != null) "تلاوة الآية: $playingAyah من $totalAyahs" else "اضغط للبدء في الاستماع",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPlaying) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onPrevAyah) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "الآية السابقة", tint = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IslamicGreenPrimary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(onClick = onNextAyah) {
                    Icon(Icons.Default.SkipNext, contentDescription = "الآية التالية", tint = MaterialTheme.colorScheme.onSurface)
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
