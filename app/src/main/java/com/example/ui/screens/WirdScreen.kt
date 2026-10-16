package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.DailyWirdItem
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirdScreen(
    dataManager: DataManager,
    onBackClick: () -> Unit
) {
    var wirdList by remember { mutableStateOf(dataManager.getDailyWirdItems()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<DailyWirdItem?>(null) }

    val completedCount = wirdList.count { it.isCompleted }
    val totalCount = wirdList.size
    val progressPercent = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()) else 0f

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "وردي اليومي",
                subtitle = "متابعة العبادات والأهداف اليومية",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IslamicGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_wird_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة هدف")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats & Progress Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (completedCount == totalCount && totalCount > 0)
                                "ما شاء الله، أتممت وردك اليومي!"
                            else
                                "إنجاز ورد اليوم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (completedCount == totalCount && totalCount > 0) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedCount من أصل $totalCount عبادات مكتملة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = IslamicGreenPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Percentage Badge
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(IslamicGreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            fontWeight = FontWeight.ExtraBold,
                            color = IslamicGreenPrimary,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            // List of Wird Items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wirdList, key = { it.id }) { item ->
                    WirdItemCard(
                        item = item,
                        onToggle = {
                            val updated = wirdList.map {
                                if (it.id == item.id) it.copy(isCompleted = !it.isCompleted) else it
                            }
                            wirdList = updated
                            dataManager.saveDailyWirdItems(updated)
                        },
                        onEdit = { itemToEdit = item },
                        onDelete = {
                            val updated = wirdList.filterNot { it.id == item.id }
                            wirdList = updated
                            dataManager.saveDailyWirdItems(updated)
                        }
                    )
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddDialog) {
        WirdGoalDialog(
            initialItem = null,
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                val updated = wirdList.toMutableList()
                updated.add(newItem)
                wirdList = updated
                dataManager.saveDailyWirdItems(updated)
                showAddDialog = false
            }
        )
    }

    // Edit Goal Dialog
    if (itemToEdit != null) {
        WirdGoalDialog(
            initialItem = itemToEdit,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                val updated = wirdList.map { if (it.id == updatedItem.id) updatedItem else it }
                wirdList = updated
                dataManager.saveDailyWirdItems(updated)
                itemToEdit = null
            }
        )
    }
}

@Composable
fun WirdItemCard(
    item: DailyWirdItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wird_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = IslamicGreenPrimary
                    ),
                    modifier = Modifier.testTag("wird_checkbox_${item.id}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Bold,
                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WirdGoalDialog(
    initialItem: DailyWirdItem?,
    onDismiss: () -> Unit,
    onSave: (DailyWirdItem) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var subtitle by remember { mutableStateOf(initialItem?.subtitle ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "إضافة هدف عبادي جديد" else "تعديل الهدف") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الهدف (مثال: صلاة الضحى)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("ملاحظة أو تفاصيل إضافية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val item = initialItem?.copy(
                            title = title.trim(),
                            subtitle = subtitle.trim()
                        ) ?: DailyWirdItem(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            subtitle = subtitle.trim(),
                            category = "custom",
                            isCompleted = false
                        )
                        onSave(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
