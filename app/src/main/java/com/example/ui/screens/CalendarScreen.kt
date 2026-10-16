package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import com.example.util.HijriCalendarHelper
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    dataManager: DataManager,
    onBackClick: () -> Unit
) {
    var manualOffset by remember { mutableIntStateOf(dataManager.hijriOffset) }
    var calendarMonthOffset by remember { mutableIntStateOf(0) } // 0 = current month

    val baseCal = remember(calendarMonthOffset) {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, calendarMonthOffset)
        c
    }

    val currentYear = baseCal.get(Calendar.YEAR)
    val currentMonth = baseCal.get(Calendar.MONTH) + 1 // 1-based
    val daysInMonth = baseCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Calculate starting day of week for the 1st day of month
    val firstDayCal = remember(currentYear, currentMonth) {
        val c = Calendar.getInstance()
        c.set(currentYear, currentMonth - 1, 1)
        c.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    }

    var selectedDay by remember {
        val now = Calendar.getInstance()
        mutableIntStateOf(if (calendarMonthOffset == 0) now.get(Calendar.DAY_OF_MONTH) else 1)
    }

    var calendarNotes by remember { mutableStateOf(dataManager.getCalendarNotes()) }
    var showEditNoteDialog by remember { mutableStateOf(false) }

    val selectedDateKey = "$currentYear-$currentMonth-$selectedDay"
    val selectedDayHijri = remember(currentYear, currentMonth, selectedDay, manualOffset) {
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth - 1, selectedDay)
        cal.add(Calendar.DAY_OF_YEAR, manualOffset)
        HijriCalendarHelper.convertGregorianToHijri(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "التقويم الهجري",
                subtitle = "المطابقة مع التقويم الميلادي والمناسبات",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Month Switcher Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { calendarMonthOffset-- },
                        modifier = Modifier.testTag("cal_prev_month_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الشهر السابق")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${selectedDayHijri.monthNameArabic} ${selectedDayHijri.year} هـ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenPrimary
                        )
                        Text(
                            text = "${getMonthNameGregorian(currentMonth)} $currentYear م",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { calendarMonthOffset++ },
                        modifier = Modifier.testTag("cal_next_month_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الشهر التالي")
                    }
                }
            }

            // Days of Week Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HijriCalendarHelper.weekDaysArabic.forEach { dayName ->
                    Text(
                        text = dayName.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(42.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid
            val totalCells = firstDayCal + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - firstDayCal + 1

                            if (dayNum in 1..daysInMonth) {
                                val dateKey = "$currentYear-$currentMonth-$dayNum"
                                val hasNote = calendarNotes.containsKey(dateKey)
                                val isSelected = selectedDay == dayNum

                                // Day Hijri Calc
                                val dayHijri = remember(currentYear, currentMonth, dayNum, manualOffset) {
                                    val cal = Calendar.getInstance()
                                    cal.set(currentYear, currentMonth - 1, dayNum)
                                    cal.add(Calendar.DAY_OF_YEAR, manualOffset)
                                    HijriCalendarHelper.convertGregorianToHijri(
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH) + 1,
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedDay = dayNum },
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isSelected -> IslamicGreenPrimary
                                        dayHijri.milestone != null -> IslamicGold.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${dayHijri.day}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Note Indicator Dot
                                        if (hasNote) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) IslamicGoldBright else IslamicGold)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(42.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Day Details & Notes Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedDayHijri.dayNameArabic}، ${selectedDayHijri.day} ${selectedDayHijri.monthNameArabic} ${selectedDayHijri.year} هـ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary
                            )
                            Text(
                                text = "الموافق: $selectedDay ${getMonthNameGregorian(currentMonth)} $currentYear م",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showEditNoteDialog = true },
                            modifier = Modifier.testTag("cal_add_note_btn")
                        ) {
                            Icon(
                                imageVector = if (calendarNotes.containsKey(selectedDateKey)) Icons.Default.EditNote else Icons.Default.AddComment,
                                contentDescription = "ملاحظة اليوم",
                                tint = IslamicGold
                            )
                        }
                    }

                    if (selectedDayHijri.milestone != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IslamicGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "مناسبة: ${selectedDayHijri.milestone}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // User note content
                    val currentNote = calendarNotes[selectedDateKey]
                    if (!currentNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ملاحظتك: $currentNote",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Manual Moon-Sighting Offset Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ضبط تقويم الهلال يدوياً:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قد تختلف الحسابات الفلكية بمقدار يوم أو يومين عن الرؤية الشرعية في بلدك. يمكنك تعديل الإزاحة:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(-2, -1, 0, 1, 2).forEach { offsetVal ->
                            val label = when {
                                offsetVal > 0 -> "+$offsetVal"
                                else -> "$offsetVal"
                            }
                            FilterChip(
                                selected = manualOffset == offsetVal,
                                onClick = {
                                    manualOffset = offsetVal
                                    dataManager.hijriOffset = offsetVal
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IslamicGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditNoteDialog) {
        var noteInput by remember { mutableStateOf(calendarNotes[selectedDateKey] ?: "") }
        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = { Text("ملاحظة لتاريخ $selectedDay/${currentMonth}") },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("اكتب ملاحظتك الشخصية لهذا اليوم...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dataManager.setCalendarNote(selectedDateKey, noteInput)
                        calendarNotes = dataManager.getCalendarNotes()
                        showEditNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNoteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

private fun getMonthNameGregorian(month: Int): String {
    val months = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )
    return months.getOrElse(month - 1) { "$month" }
}
