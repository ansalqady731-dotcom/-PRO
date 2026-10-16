package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adhkar.AdhkarCatalog
import com.example.data.local.DataManager
import com.example.data.quran.QuranCatalog
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreenPrimary

sealed class SearchResultItem {
    data class SurahResult(val number: Int, val nameArabic: String, val ayahsCount: Int, val revelation: String) : SearchResultItem()
    data class AyahResult(val surahNumber: Int, val surahName: String, val ayahNumber: Int, val text: String) : SearchResultItem()
    data class DhikrResult(val id: String, val text: String, val category: String, val source: String) : SearchResultItem()
    data class NoteResult(val id: String, val title: String, val text: String, val surahName: String) : SearchResultItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    dataManager: DataManager,
    onNavigateToSurahReader: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filterIndex by remember { mutableIntStateOf(0) }
    val filterLabels = listOf("الكل", "القرآن", "الأذكار", "التدبر")

    val results = remember(query, filterIndex) {
        if (query.trim().length < 2) {
            emptyList()
        } else {
            val q = query.trim()
            val list = mutableListOf<SearchResultItem>()

            // Search Quran Surahs
            if (filterIndex == 0 || filterIndex == 1) {
                QuranCatalog.allSurahsMetadata.forEach { s ->
                    if (s.nameArabic.contains(q, ignoreCase = true) ||
                        s.nameEnglish.contains(q, ignoreCase = true) ||
                        s.number.toString() == q
                    ) {
                        list.add(SearchResultItem.SurahResult(s.number, s.nameArabic, s.ayahsCount, s.revelationType.labelArabic))
                    }
                }

                // Search Pre-bundled Verses Text
                QuranCatalog.preBundledVerses.forEach { (surahNum, ayahs) ->
                    val sMeta = QuranCatalog.allSurahsMetadata.find { it.number == surahNum }
                    val sName = sMeta?.nameArabic ?: ""
                    ayahs.forEach { a ->
                        if (a.textArabic.contains(q, ignoreCase = true)) {
                            list.add(SearchResultItem.AyahResult(surahNum, sName, a.numberInSurah, a.textArabic))
                        }
                    }
                }
            }

            // Search Adhkar
            if (filterIndex == 0 || filterIndex == 2) {
                AdhkarCatalog.allAdhkar.forEach { d ->
                    if (d.text.contains(q, ignoreCase = true) ||
                        d.reward.contains(q, ignoreCase = true) ||
                        d.source.contains(q, ignoreCase = true)
                    ) {
                        list.add(SearchResultItem.DhikrResult(d.id, d.text, d.categoryId, d.source))
                    }
                }
            }

            // Search Tadabbur Notes
            if (filterIndex == 0 || filterIndex == 3) {
                dataManager.getTadabburNotes().forEach { n ->
                    if (n.title.contains(q, ignoreCase = true) ||
                        n.reflectionText.contains(q, ignoreCase = true) ||
                        n.surahName.contains(q, ignoreCase = true)
                    ) {
                        list.add(SearchResultItem.NoteResult(n.id, n.title, n.reflectionText, n.surahName))
                    }
                }
            }

            list
        }
    }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "البحث الشامل",
                subtitle = "بحث فوري دون اتصال بالإنترنت",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("اكتب كلمة للبحث (سورة، آية، ذكر، ملاحظة)...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGreenPrimary)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("comprehensive_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterLabels.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = filterIndex == idx,
                        onClick = { filterIndex = idx },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results List
            if (query.trim().length < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "أدخل حرفين على الأقل لبدء البحث الشامل",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لم يتم العثور على نتائج مطابقة لـ \"$query\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { res ->
                        when (res) {
                            is SearchResultItem.SurahResult -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToSurahReader(res.number) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = IslamicGreenPrimary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "سورة ${res.nameArabic}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "رقم السورة ${res.number} • ${res.ayahsCount} آيات • ${res.revelation}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            is SearchResultItem.AyahResult -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToSurahReader(res.surahNumber) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "آية من سورة ${res.surahName} (الآية ${res.ayahNumber})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IslamicGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = res.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                            is SearchResultItem.DhikrResult -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "ذكر نبوي مبارك",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IslamicGreenPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = res.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (res.source.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = res.source,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            is SearchResultItem.NoteResult -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "خاطرة تدبر: ${res.title}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = res.text,
                                            style = MaterialTheme.typography.bodyMedium,
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
}
