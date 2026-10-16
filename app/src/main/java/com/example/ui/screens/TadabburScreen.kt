package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataManager
import com.example.data.model.TadabburNote
import com.example.data.quran.QuranCatalog
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TadabburScreen(
    dataManager: DataManager,
    onBackClick: () -> Unit
) {
    var notes by remember { mutableStateOf(dataManager.getTadabburNotes()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<TadabburNote?>(null) }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) {
            notes
        } else {
            val q = searchQuery.trim()
            notes.filter {
                it.title.contains(q, ignoreCase = true) ||
                        it.reflectionText.contains(q, ignoreCase = true) ||
                        it.surahName.contains(q, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "دفتر التدبر",
                subtitle = "خواطر وتأملات قرآنية شخصية",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IslamicGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_tadabbur_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "تدبر جديد")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Respectful Disclaimer Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = IslamicGold.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "تنبيه",
                        tint = IslamicGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تنبيه: هذه الملاحظات هي خواطر شخصية للاستذكار والتدبر الذاتي، وليست تفسيراً معتمداً لكتاب الله. يُرجع في تفسير الآيات إلى مصادر التفسير المعتمدة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث في ملاحظات التدبر...") },
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
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("tadabbur_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Notes List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (notes.isEmpty())
                            "لا توجد ملاحظات تدبر محفوظة بعد\nاضغط على زر (+) لتدوين أول خاطرة قرآنية"
                        else
                            "لا توجد نتائج مطابقة لبحثك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        TadabburNoteCard(
                            note = note,
                            onEdit = { noteToEdit = note },
                            onDelete = {
                                dataManager.deleteTadabburNote(note.id)
                                notes = dataManager.getTadabburNotes()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TadabburNoteDialog(
            initialNote = null,
            onDismiss = { showAddDialog = false },
            onSave = { newNote ->
                dataManager.saveTadabburNote(newNote)
                notes = dataManager.getTadabburNotes()
                showAddDialog = false
            }
        )
    }

    if (noteToEdit != null) {
        TadabburNoteDialog(
            initialNote = noteToEdit,
            onDismiss = { noteToEdit = null },
            onSave = { updatedNote ->
                dataManager.saveTadabburNote(updatedNote)
                notes = dataManager.getTadabburNotes()
                noteToEdit = null
            }
        )
    }
}

@Composable
fun TadabburNoteCard(
    note: TadabburNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatted = remember(note.timestamp) {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        sdf.format(Date(note.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tadabbur_card_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Linked Surah / Ayah Badge
            if (note.surahNumber != null || note.surahName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = IslamicGreenPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "مرتبطة بسورة ${note.surahName}${if (note.ayahNumber != null) " - آية ${note.ayahNumber}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicGreenPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.reflectionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TadabburNoteDialog(
    initialNote: TadabburNote?,
    onDismiss: () -> Unit,
    onSave: (TadabburNote) -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var reflectionText by remember { mutableStateOf(initialNote?.reflectionText ?: "") }
    var surahNumberText by remember { mutableStateOf(initialNote?.surahNumber?.toString() ?: "") }
    var ayahNumberText by remember { mutableStateOf(initialNote?.ayahNumber?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialNote == null) "تدوين خاطرة تدبر جديدة" else "تعديل الملاحظة") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الخاطرة (مثال: أثر الاستغفار)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = surahNumberText,
                        onValueChange = { surahNumberText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("رقم السورة (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ayahNumberText,
                        onValueChange = { ayahNumberText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("رقم الآية (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    label = { Text("نص التدبر والتأمل الشخصي...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && reflectionText.isNotBlank()) {
                        val sNum = surahNumberText.toIntOrNull()
                        val sName = if (sNum != null) {
                            QuranCatalog.allSurahsMetadata.find { it.number == sNum }?.nameArabic ?: ""
                        } else ""
                        val aNum = ayahNumberText.toIntOrNull()

                        val note = initialNote?.copy(
                            title = title.trim(),
                            reflectionText = reflectionText.trim(),
                            surahNumber = sNum,
                            surahName = sName,
                            ayahNumber = aNum
                        ) ?: TadabburNote(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            reflectionText = reflectionText.trim(),
                            surahNumber = sNum,
                            surahName = sName,
                            ayahNumber = aNum
                        )
                        onSave(note)
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
