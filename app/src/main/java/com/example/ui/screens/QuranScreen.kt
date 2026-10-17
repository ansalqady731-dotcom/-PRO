package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataManager
import com.example.data.model.Bookmark
import com.example.data.model.PageItem
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import com.example.data.quran.*
import com.example.ui.components.MuslimTopBar
import com.example.ui.components.TafsirLibraryDialog
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    dataManager: DataManager,
    onNavigateToReader: (Int, Int?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        "السور (١١٤)",
        "الصفحات (٦٠٤)",
        "الأجزاء (٣٠)",
        "مصحف التجويد",
        "العلامات والمفضلة",
        "التنزيل أوفلاين"
    )

    var searchQuery by remember { mutableStateOf("") }
    var favoriteSurahs by remember { mutableStateOf(dataManager.getFavoriteSurahs()) }
    var bookmarks by remember { mutableStateOf(dataManager.getBookmarks()) }

    // Ensure Quran Repository is initialized
    val allSurahs = remember { QuranRepository.getAllSurahs(context) }
    val allPages = remember { QuranRepository.getAllPages(context) }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "القرآن الكريم",
                subtitle = "١١٤ سورة • ٦٠٤ صفحات • مصحف التجويد الملون",
                onBackClick = onBackClick,
                actions = {
                    // Direct quick button to Offline Download Manager
                    IconButton(
                        onClick = { selectedTabIndex = 5 },
                        modifier = Modifier.testTag("quran_download_manager_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "شاشة التنزيل دون إنترنت",
                            tint = if (selectedTabIndex == 5) IslamicGoldBright else IslamicGold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Tab Row with 6 dedicated tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IslamicGreenPrimary,
                edgePadding = 12.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Search Bar (shown for Surahs and Pages)
            if (selectedTabIndex == 0 || selectedTabIndex == 1 || selectedTabIndex == 4) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (selectedTabIndex == 1) "ابحث عن رقم الصفحة (1 - 604)..."
                            else "ابحث عن سورة بالاسم أو الرقم..."
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGreenPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("quran_search_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // 1. All 114 Surahs
                    val filteredSurahs = remember(searchQuery, allSurahs) {
                        if (searchQuery.isBlank()) {
                            allSurahs
                        } else {
                            val q = searchQuery.trim()
                            allSurahs.filter {
                                it.nameArabic.contains(q, ignoreCase = true) ||
                                        it.nameEnglish.contains(q, ignoreCase = true) ||
                                        it.number.toString() == q
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSurahs, key = { it.number }) { surah ->
                            val isFav = favoriteSurahs.contains(surah.number)
                            SurahCard(
                                surah = surah,
                                isFavorite = isFav,
                                isAvailableOffline = true,
                                onFavoriteToggle = {
                                    dataManager.toggleFavoriteSurah(surah.number)
                                    favoriteSurahs = dataManager.getFavoriteSurahs()
                                },
                                onClick = { onNavigateToReader(surah.number, null) }
                            )
                        }
                    }
                }

                1 -> {
                    // 2. All 604 Pages
                    val filteredPages = remember(searchQuery, allPages) {
                        if (searchQuery.isBlank()) {
                            allPages
                        } else {
                            val q = searchQuery.trim()
                            allPages.filter {
                                it.pageNumber.toString().contains(q) ||
                                        it.surahName.contains(q) ||
                                        "صفحة ${it.pageNumber}".contains(q)
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPages, key = { it.pageNumber }) { page ->
                            PageCard(
                                page = page,
                                onClick = {
                                    val targetSurah = allSurahs.find { s -> s.nameArabic == page.surahName }?.number ?: 1
                                    onNavigateToReader(targetSurah, page.pageNumber)
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // 3. 30 Juz
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QuranCatalog.juzList, key = { it.number }) { juz ->
                            JuzCard(juz = juz, onClick = {
                                val targetSurah = allSurahs.firstOrNull { it.juzStart == juz.number }?.number ?: 1
                                onNavigateToReader(targetSurah, null)
                            })
                        }
                    }
                }

                3 -> {
                    // 4. مصحف التجويد وأحكامه
                    TajweedSectionView(
                        onOpenPdf = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TajweedEncyclopedia.SURAH_QURAN_PDF_URL))
                            context.startActivity(intent)
                        },
                        onOpenWeb = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TajweedEncyclopedia.SURAH_QURAN_PAGE_URL))
                            context.startActivity(intent)
                        }
                    )
                }

                4 -> {
                    // 5. Bookmarks & Favorites
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "السور المفضلة (${favoriteSurahs.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        val favList = allSurahs.filter { favoriteSurahs.contains(it.number) }
                        if (favList.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "لا توجد سور في المفضلة. انقر على رمز النجمة بجانب أي سورة لإضافتها هنا.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(14.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(favList, key = { "fav_${it.number}" }) { surah ->
                                SurahCard(
                                    surah = surah,
                                    isFavorite = true,
                                    isAvailableOffline = true,
                                    onFavoriteToggle = {
                                        dataManager.toggleFavoriteSurah(surah.number)
                                        favoriteSurahs = dataManager.getFavoriteSurahs()
                                    },
                                    onClick = { onNavigateToReader(surah.number, null) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "العلامات المرجعية المحفوظة (${bookmarks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (bookmarks.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "لا توجد علامات مرجعية محفوظة بعد. يمكنك حفظ أي آية أثناء القراءة بالضغط على زر العلامة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(14.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(bookmarks, key = { it.id }) { bm ->
                                BookmarkCard(
                                    bookmark = bm,
                                    onClick = { onNavigateToReader(bm.surahNumber, null) },
                                    onDelete = {
                                        dataManager.deleteBookmark(bm.id)
                                        bookmarks = dataManager.getBookmarks()
                                    }
                                )
                            }
                        }
                    }
                }

                5 -> {
                    // 6. شاشة تنزيل محتوى المصحف للاستخدام دون إنترنت (Offline Download Manager)
                    OfflineDownloadScreen(
                        dataManager = dataManager,
                        allSurahs = allSurahs
                    )
                }
            }
        }
    }
}

@Composable
fun PageCard(
    page: PageItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(IslamicGreenPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${page.pageNumber}",
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreenPrimary,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = page.surahName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "الجزء ${page.juzNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun OfflineDownloadScreen(
    dataManager: DataManager,
    allSurahs: List<Surah>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isBatchImporting by remember { mutableStateOf(false) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    var currentProgressText by remember { mutableStateOf("") }
    var importProgress by remember { mutableFloatStateOf(0f) }
    var showTafsirLibraryDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overview Status Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = IslamicGreenPrimary,
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = IslamicGoldBright,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "المصحف كامل ومتاح أوفلاين 100%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "جميع السور الـ ١١٤ والآيات الـ ٦٢٣٦ بالرسم العثماني المعتمد، والتفسير الميسر كاملاً، ومعاجم معاني الكلمات وأحكام التجويد مدمجة ومحفوظة بالكامل على هاتفك دون حاجة لأي اتصال بالإنترنت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Package 1: Quran Uthmani
        item {
            ContentPackageCard(
                title = "النص القرآني بالرسم العثماني المعتمد",
                subtitle = "١١٤ سورة • ٦٠٤ صفحات • ٦٢٣٦ آية مع الضبط التام",
                sizeText = "1.7 MB",
                isInstalled = true,
                badgeText = "مثبت أوفلاين"
            )
        }

        // Package 2: Tafsir Muyassar
        item {
            ContentPackageCard(
                title = "التفسير الميسر والمختصر",
                subtitle = "تفسير موثوق لجميع آيات القرآن الكريم الـ ٦٢٣٦",
                sizeText = "2.5 MB",
                isInstalled = true,
                badgeText = "مثبت أوفلاين"
            )
        }

        // Package: Tafsir Library & Translations (Matching Screenshot 4)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTafsirLibraryDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, IslamicGreenPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(IslamicGreenPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LibraryBooks,
                            contentDescription = null,
                            tint = IslamicGreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مكتبة كتب التفسير والترجمات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "المختصر في التفسير، الميسر، والترجمات العالمية (انقر للإدارة)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = IslamicGreenPrimary
                    )
                }
            }
        }

        // Package 3: Words & Tajweed
        item {
            ContentPackageCard(
                title = "معاني الكلمات الغريبة وموسوعة التجويد",
                subtitle = "قاموس المفردات القرآنية وألوان أحكام التجويد المعتمدة",
                sizeText = "0.5 MB",
                isInstalled = true,
                badgeText = "مثبت أوفلاين"
            )
        }

        // Package 4: Sync & Import with surahquran.com
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, IslamicGold.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "استيراد وتحديث السور من موقع سورة قرآن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "المصدر المعتمد: surahquran.com\nيتيح لك هذا الزر استيراد نص السور ومطابقتها مباشرة من الموقع الرسمي وتخزينها في الذاكرة المحلية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isBatchImporting) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { importProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = IslamicGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentProgressText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenPrimary
                        )
                    }

                    if (importStatus != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = importStatus ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isBatchImporting = true
                            importStatus = null
                            coroutineScope.launch {
                                val targetSurahs = listOf(1, 36, 55, 56, 67, 112, 113, 114)
                                var count = 0
                                for ((idx, sNum) in targetSurahs.withIndex()) {
                                    val sName = allSurahs.find { it.number == sNum }?.nameArabic ?: ""
                                    currentProgressText = "جارٍ استيراد سورة $sName من surahquran.com..."
                                    importProgress = (idx + 1).toFloat() / targetSurahs.size
                                    val res = SurahQuranImporter.fetchSurah(sNum)
                                    if (res.isSuccess) {
                                        res.getOrNull()?.let { ayahs ->
                                            dataManager.saveCachedSurahVerses(sNum, ayahs)
                                            count++
                                        }
                                    }
                                }
                                isBatchImporting = false
                                importStatus = "تم استيراد $count سور بنجاح وتحديثها في الذاكرة المحلية أوفلاين!"
                                Toast.makeText(context, "اكتمل الاستيراد والتحديث بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isBatchImporting,
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بدء استيراد وتحديث السور من surahquran.com")
                    }
                }
            }
        }

        // Audio Recitations Offline info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = IslamicGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "التلاوات الصوتية لكبار القراء",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "التطبيق يدعم الاستماع الفوري لـ ٤ قراء معتمدين (الشيخ مشاري العفاسي، الشيخ خليل الحصري، الشيخ عبد الباسط عبد الصمد، الشيخ سعد الغامدي) مع ميزة التخزين التلقائي المؤقت للآيات المستمع إليها لتعمل دون إنترنت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showTafsirLibraryDialog) {
        TafsirLibraryDialog(onDismiss = { showTafsirLibraryDialog = false })
    }
}

@Composable
fun ContentPackageCard(
    title: String,
    subtitle: String,
    sizeText: String,
    isInstalled: Boolean,
    badgeText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IslamicGreenPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = IslamicGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الحجم: $sizeText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = IslamicGreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SurahCard(
    surah: Surah,
    isFavorite: Boolean,
    isAvailableOffline: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("surah_card_${surah.number}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Surah Number in Islamic Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(IslamicGreenPrimary.copy(alpha = 0.1f))
                        .border(1.dp, IslamicGold.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}",
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "سورة ${surah.nameArabic}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (surah.revelationType == RevelationType.MECCAN)
                                IslamicGold.copy(alpha = 0.15f)
                            else
                                IslamicGreenPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = surah.revelationType.labelArabic,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (surah.revelationType == RevelationType.MECCAN) IslamicGold else IslamicGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${surah.nameEnglish} • ${surah.ayahsCount} آيات • الجزء ${surah.juzStart}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.testTag("fav_button_${surah.number}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "قراءة السورة",
                    tint = IslamicGreenPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun JuzCard(
    juz: JuzInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(IslamicGold.copy(alpha = 0.15f))
                        .border(1.dp, IslamicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${juz.number}",
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = juz.nameArabic,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = juz.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "فتح الجزء",
                tint = IslamicGreenPrimary
            )
        }
    }
}

@Composable
fun BookmarkCard(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "علامة مرجعية",
                    tint = IslamicGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "سورة ${bookmark.surahName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الآية رقم ${bookmark.ayahNumber} • صفحة ${bookmark.page}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGreenPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    if (bookmark.note.isNotBlank()) {
                        Text(
                            text = bookmark.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف العلامة",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TajweedSectionView(
    onOpenPdf: () -> Unit,
    onOpenWeb: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGreenPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "مصحف التجويد الملون مع التفسير",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "المصحف المعتمد وفق معايير دار المعرفة والأزهر الشريف، كما هو متاح على موقع surahquran.com. تميزت صفحاته بتلوين دقيق لأحكام التجويد لتيسير التلاوة الصحيحة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Color Key Legend
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مفتاح ألوان مصحف التجويد المعتمد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    TajweedColorizer.legendItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // External Resource Links
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "روابط المصحف من موقع سورة قرآن (surahquran.com):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فتح مصحف التجويد PDF الأصلي كاملاً")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onOpenWeb,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("زيارة صفحة مصحف التجويد على surahquran.com")
                    }
                }
            }
        }

        // Educational Tajweed Lessons
        item {
            Text(
                text = "دروس وأحكام التجويد النظرية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IslamicGreenPrimary
            )
        }

        items(TajweedEncyclopedia.lessons, key = { it.id }) { lesson ->
            TajweedLessonCard(lesson = lesson)
        }
    }
}

@Composable
fun TajweedLessonCard(lesson: TajweedLesson) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreenPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = IslamicGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = lesson.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = "تفاصيل الحكم:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.detailedContent,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "أمثلة من القرآن الكريم:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    lesson.examples.forEach { ex ->
                        Text(
                            text = "• $ex",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
