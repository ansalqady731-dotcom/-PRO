package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adhkar.AdhkarCatalog
import com.example.data.local.DataManager
import com.example.data.model.AdhkarCategory
import com.example.data.model.DhikrItem
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import com.example.util.VibrationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    dataManager: DataManager,
    initialCategory: String? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val categories = remember { AdhkarCategory.values().toList() }
    
    var selectedCatId by remember {
        mutableStateOf(initialCategory ?: AdhkarCategory.MORNING.id)
    }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var favoriteIds by remember { mutableStateOf(dataManager.getFavoriteAdhkar()) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Recomposition trigger for counter updates
    var counterVersion by remember { mutableIntStateOf(0) }

    val displayedItems = remember(selectedCatId, showOnlyFavorites, searchQuery, favoriteIds, counterVersion) {
        val baseList = if (showOnlyFavorites) {
            AdhkarCatalog.allAdhkar.filter { favoriteIds.contains(it.id) }
        } else {
            AdhkarCatalog.getByCategory(selectedCatId)
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim()
            baseList.filter {
                it.text.contains(q, ignoreCase = true) ||
                        it.reward.contains(q, ignoreCase = true) ||
                        it.source.contains(q, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "الأذكار والأدعية",
                subtitle = "من صحيح السنة النبوية المطهرة",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("adhkar_reset_all_action")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تصفير عدادات اليوم", tint = IslamicGold)
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
            // Category Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = if (showOnlyFavorites) categories.size else categories.indexOfFirst { it.id == selectedCatId }.coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IslamicGreenPrimary,
                edgePadding = 16.dp
            ) {
                categories.forEach { cat ->
                    val isSelected = !showOnlyFavorites && selectedCatId == cat.id
                    Tab(
                        selected = isSelected,
                        onClick = {
                            showOnlyFavorites = false
                            selectedCatId = cat.id
                        },
                        text = {
                            Text(
                                text = cat.titleArabic,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
                // Favorites Tab
                Tab(
                    selected = showOnlyFavorites,
                    onClick = { showOnlyFavorites = true },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = IslamicGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "المفضلة",
                                fontWeight = if (showOnlyFavorites) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث في نصوص الأذكار أو الفضل...") },
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
                    .testTag("adhkar_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // List of Adhkar
            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlyFavorites) "لا توجد أذكار في المفضلة بعد" else "لا توجد نتائج مطابقة لبحثك",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedItems, key = { it.id }) { dhikr ->
                        val currentCount = dataManager.getAdhkarCount(dhikr.id)
                        val isFav = favoriteIds.contains(dhikr.id)

                        DhikrCard(
                            dhikr = dhikr,
                            currentCount = currentCount,
                            isFavorite = isFav,
                            onIncrement = {
                                if (currentCount < dhikr.count) {
                                    val next = currentCount + 1
                                    dataManager.setAdhkarCount(dhikr.id, next)
                                    counterVersion++
                                    if (dataManager.tasbeehVibration) {
                                        if (next == dhikr.count) {
                                            VibrationHelper.vibrateGoal(context)
                                        } else {
                                            VibrationHelper.vibrate(context, 30)
                                        }
                                    }
                                }
                            },
                            onComplete = {
                                dataManager.setAdhkarCount(dhikr.id, dhikr.count)
                                counterVersion++
                                if (dataManager.tasbeehVibration) {
                                    VibrationHelper.vibrateGoal(context)
                                }
                            },
                            onToggleFavorite = {
                                dataManager.toggleFavoriteAdhkar(dhikr.id)
                                favoriteIds = dataManager.getFavoriteAdhkar()
                            },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${dhikr.text}\n\n[المصدر: ${dhikr.source}]")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة الذكر المبارك")
                                context.startActivity(shareIntent)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تصفير العدادات") },
            text = { Text("هل ترغب في إعادة ضبط جميع عدادات الأذكار لتبدأ من جديد اليوم؟") },
            confirmButton = {
                Button(
                    onClick = {
                        dataManager.resetAllAdhkarCounts()
                        counterVersion++
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، تصفير")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun DhikrCard(
    dhikr: DhikrItem,
    currentCount: Int,
    isFavorite: Boolean,
    onIncrement: () -> Unit,
    onComplete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit
) {
    val isDone = currentCount >= dhikr.count

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${dhikr.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = if (isDone) androidx.compose.foundation.BorderStroke(1.dp, IslamicGreenPrimary.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top action buttons (Favorite & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDone) IslamicGreenPrimary.copy(alpha = 0.15f) else IslamicGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isDone) "تم الإكمال ✓" else "التكرار: ${dhikr.count} مرة",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) IslamicGreenPrimary else IslamicGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "مفضلة",
                            tint = if (isFavorite) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Dhikr Text
            Text(
                text = dhikr.text,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Reward / Virtue
            if (dhikr.reward.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "فضل الذكر: ${dhikr.reward}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Authentic Source
            if (dhikr.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "المصدر: ${dhikr.source}",
                    style = MaterialTheme.typography.labelSmall,
                    color = IslamicGoldBright,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Counter Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick complete button
                if (!isDone) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.testTag("dhikr_complete_btn_${dhikr.id}")
                    ) {
                        Text(
                            text = "إكمال مباشر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "تقبل الله طاعتك",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive Counter Button
                Button(
                    onClick = onIncrement,
                    enabled = !isDone,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) IslamicGreenPrimary.copy(alpha = 0.6f) else IslamicGreenPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("dhikr_counter_btn_${dhikr.id}")
                ) {
                    Text(
                        text = "$currentCount / ${dhikr.count}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
