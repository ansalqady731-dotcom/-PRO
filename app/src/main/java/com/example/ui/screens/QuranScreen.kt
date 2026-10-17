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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataManager
import com.example.data.model.Bookmark
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import com.example.data.quran.JuzInfo
import com.example.data.quran.QuranCatalog
import com.example.data.quran.SurahQuranImporter
import com.example.data.quran.TajweedColorizer
import com.example.data.quran.TajweedEncyclopedia
import com.example.data.quran.TajweedLesson
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    dataManager: DataManager,
    onNavigateToReader: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("السور", "مصحف التجويد", "الأجزاء", "المفضلة", "العلامات")

    var searchQuery by remember { mutableStateOf("") }
    var favoriteSurahs by remember { mutableStateOf(dataManager.getFavoriteSurahs()) }
    var bookmarks by remember { mutableStateOf(dataManager.getBookmarks()) }
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "القرآن الكريم",
                subtitle = "١١٤ سورة • مصحف التجويد الملون",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.testTag("quran_import_json_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "استيراد من surahquran.com",
                            tint = IslamicGold
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
            // Tabs
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
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Search Bar (shown for Surahs, Tajweed, and Favorites)
            if (selectedTabIndex == 0 || selectedTabIndex == 3) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
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
                    // All 114 Surahs
                    val filteredSurahs = remember(searchQuery) {
                        if (searchQuery.isBlank()) {
                            QuranCatalog.allSurahsMetadata
                        } else {
                            val q = searchQuery.trim()
                            QuranCatalog.allSurahsMetadata.filter {
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
                            val isCached = dataManager.hasCachedSurah(surah.number) || QuranCatalog.preBundledVerses.containsKey(surah.number)

                            SurahCard(
                                surah = surah,
                                isFavorite = isFav,
                                isAvailableOffline = isCached,
                                onFavoriteToggle = {
                                    val nowFav = dataManager.toggleFavoriteSurah(surah.number)
                                    favoriteSurahs = dataManager.getFavoriteSurahs()
                                },
                                onClick = { onNavigateToReader(surah.number) }
                            )
                        }
                    }
                }
                1 -> {
                    // مصحف التجويد وأحكامه (Imported from surahquran.com)
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
                2 -> {
                    // 30 Juz
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QuranCatalog.juzList, key = { it.number }) { juz ->
                            JuzCard(juz = juz, onClick = {
                                val targetSurah = QuranCatalog.allSurahsMetadata.firstOrNull { it.juzStart == juz.number }?.number ?: 1
                                onNavigateToReader(targetSurah)
                            })
                        }
                    }
                }
                3 -> {
                    // Favorites
                    val favList = remember(favoriteSurahs, searchQuery) {
                        QuranCatalog.allSurahsMetadata.filter { favoriteSurahs.contains(it.number) }
                            .filter {
                                searchQuery.isBlank() || it.nameArabic.contains(searchQuery.trim())
                            }
                    }

                    if (favList.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.StarBorder,
                            message = "لا توجد سور في المفضلة حتى الآن\nاضغط على رمز النجمة بجانب أي سورة لإضافتها إلى المفضلة"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favList, key = { it.number }) { surah ->
                                val isCached = dataManager.hasCachedSurah(surah.number) || QuranCatalog.preBundledVerses.containsKey(surah.number)
                                SurahCard(
                                    surah = surah,
                                    isFavorite = true,
                                    isAvailableOffline = isCached,
                                    onFavoriteToggle = {
                                        dataManager.toggleFavoriteSurah(surah.number)
                                        favoriteSurahs = dataManager.getFavoriteSurahs()
                                    },
                                    onClick = { onNavigateToReader(surah.number) }
                                )
                            }
                        }
                    }
                }
                4 -> {
                    // Bookmarks
                    if (bookmarks.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.BookmarkBorder,
                            message = "لا توجد علامات مرجعية محفوظة\nأثناء قراءة السورة يمكنك حفظ موضعك بالضغط على علامة الحفظ"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(bookmarks, key = { it.id }) { bm ->
                                BookmarkCard(
                                    bookmark = bm,
                                    onClick = { onNavigateToReader(bm.surahNumber) },
                                    onDelete = {
                                        dataManager.deleteBookmark(bm.id)
                                        bookmarks = dataManager.getBookmarks()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        QuranImportDialog(
            dataManager = dataManager,
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
fun TajweedSectionView(
    onOpenPdf: () -> Unit,
    onOpenWeb: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card for Tajweed Quran
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مصحف التجويد الملون",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IslamicGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "دار المعرفة • معتمد",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "المصحف المجود الملون بالرسم العثماني برواية حفص عن عاصم بخط عثمان طه، الصادر بموافقة الأزهر الشريف والموثق في موقع سورة قرآن (surahquran.com). يشتمل على ثلاثة ألوان رئيسية تغطي 28 حكماً تجويدياً لتيسير تلاوة كتاب الله.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحميل المصحف PDF")
                        }

                        OutlinedButton(
                            onClick = onOpenWeb,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("موقع surahquran")
                        }
                    }
                }
            }
        }

        // Color Legend Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "دلالات ألوان مصحف التجويد:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TajweedColorizer.legendItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium,
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

        // Tajweed Encyclopedia Lessons
        item {
            Text(
                text = "دروس وأحكام التجويد (مستوردة من surahquran.com):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IslamicGreenPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = lesson.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = IslamicGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "تفاصيل الحكم"
                    )
                }
            }

            Text(
                text = lesson.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = lesson.detailedContent,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                    if (lesson.examples.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "أمثلة قرآنية: " + lesson.examples.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
            .testTag("surah_card_${surah.number}")
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Surah Number Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(IslamicGreenPrimary.copy(alpha = 0.12f))
                        .border(1.dp, IslamicGold.copy(alpha = 0.5f), CircleShape),
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
                            text = surah.nameArabic,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (surah.revelationType == RevelationType.MECCAN)
                                IslamicGold.copy(alpha = 0.15f)
                            else
                                IslamicGreenPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = surah.revelationType.labelArabic,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (surah.revelationType == RevelationType.MECCAN)
                                    IslamicGold
                                else
                                    IslamicGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isAvailableOffline) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = IslamicGreenPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "تجويد ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicGreenPrimary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${surah.nameEnglish} • ${surah.ayahsCount} آيات • الجزء ${surah.juzStart}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.testTag("surah_fav_button_${surah.number}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "تفضيل السورة",
                    tint = if (isFavorite) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(IslamicGold.copy(alpha = 0.15f)),
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
                        text = "الآية رقم ${bookmark.ayahNumber}",
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
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun QuranImportDialog(
    dataManager: DataManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isBatchImporting by remember { mutableStateOf(false) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    var currentProgressText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "استيراد المصحف من موقع سورة قرآن",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "الموقع المعتمد: surahquran.com\nيقوم هذا الزر باستيراد وحفظ السور الكريمة بنصها العثماني المعتمد مع كامل الضبط التجويدي للعمل دون اتصال بالإنترنت:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (isBatchImporting) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = IslamicGreenPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentProgressText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (importStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = importStatus ?: "",
                        color = IslamicGreenPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isBatchImporting = true
                    importStatus = null
                    coroutineScope.launch {
                        // Import prominent recited surahs from surahquran.com
                        val targetSurahs = listOf(1, 36, 55, 56, 67, 112, 113, 114)
                        var successCount = 0
                        for (sNum in targetSurahs) {
                            val sName = QuranCatalog.allSurahsMetadata.find { it.number == sNum }?.nameArabic ?: ""
                            currentProgressText = "جارٍ استيراد سورة $sName من surahquran.com..."
                            val res = SurahQuranImporter.fetchSurah(sNum)
                            if (res.isSuccess) {
                                res.getOrNull()?.let { ayahs ->
                                    dataManager.saveCachedSurahVerses(sNum, ayahs)
                                    successCount++
                                }
                            }
                        }
                        isBatchImporting = false
                        importStatus = "تم استيراد $successCount سور بنجاح وتخزينها محلياً في هاتفك!"
                    }
                },
                enabled = !isBatchImporting,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
            ) {
                Text("بدء استيراد السور الأساسية")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBatchImporting) {
                Text("إغلاق")
            }
        }
    )
}
