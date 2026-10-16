package com.example.ui.screens

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
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    dataManager: DataManager,
    onNavigateToReader: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("السور", "الأجزاء", "المفضلة", "العلامات")

    var searchQuery by remember { mutableStateOf("") }
    var favoriteSurahs by remember { mutableStateOf(dataManager.getFavoriteSurahs()) }
    var bookmarks by remember { mutableStateOf(dataManager.getBookmarks()) }
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "القرآن الكريم",
                subtitle = "١١٤ سورة - ٣٠ جزءاً",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.testTag("quran_import_json_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "استيراد بيانات القرآن",
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
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IslamicGreenPrimary
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

            // Search Bar (shown for Surahs and Favorites)
            if (selectedTabIndex == 0 || selectedTabIndex == 2) {
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
                            SurahCard(
                                surah = surah,
                                isFavorite = isFav,
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
                    // 30 Juz
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QuranCatalog.juzList, key = { it.number }) { juz ->
                            JuzCard(juz = juz, onClick = {
                                // Jump to starting surah of this juz
                                val targetSurah = QuranCatalog.allSurahsMetadata.firstOrNull { it.juzStart == juz.number }?.number ?: 1
                                onNavigateToReader(targetSurah)
                            })
                        }
                    }
                }
                2 -> {
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
                                SurahCard(
                                    surah = surah,
                                    isFavorite = true,
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
                3 -> {
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
fun SurahCard(
    surah: Surah,
    isFavorite: Boolean,
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
    var rawInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "استيراد بيانات المصحف الشريف (JSON)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "حرصاً على ضبط القرآن الكريم، يمكنك هنا استيراد ملف JSON معتمد (مثل مصحف مجمع الملك فهد أو تنزيل) بالهيكل القياسي.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    placeholder = { Text("الصق بيانات JSON المعتمدة هنا...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusMessage ?: "",
                        color = if (isSuccess) IslamicGreenPrimary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawInput.isBlank()) {
                        statusMessage = "يرجى لصق بيانات JSON صحيحة أولاً"
                        isSuccess = false
                        return@Button
                    }
                    try {
                        val parsed = JSONObject(rawInput)
                        // Verify minimal structural integrity
                        if (!parsed.has("surahs") && !parsed.has("quran")) {
                            statusMessage = "بنية JSON غير متوافقة: يجب أن يحتوي الملف على مصفوفة surahs أو quran"
                            isSuccess = false
                            return@Button
                        }
                        dataManager.saveImportedQuranRaw(rawInput)
                        statusMessage = "تم التحقق من بنية البيانات وحفظها محلياً بنجاح!"
                        isSuccess = true
                    } catch (e: Exception) {
                        statusMessage = "خطأ في قراءة ملف JSON: ${e.message}"
                        isSuccess = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
            ) {
                Text("تحقق واستيراد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}
