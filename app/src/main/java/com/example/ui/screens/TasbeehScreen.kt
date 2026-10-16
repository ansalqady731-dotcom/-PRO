package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.util.VibrationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehScreen(
    dataManager: DataManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val presetDhikrs = listOf(
        "سُبْحَانَ اللَّهِ",
        "الْحَمْدُ لِلَّهِ",
        "لَا إِلَهَ إِلَّا اللَّهُ",
        "اللَّهُ أَكْبَرُ",
        "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
        "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
        "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ"
    )

    var count by remember { mutableIntStateOf(dataManager.tasbeehCount) }
    var selectedDhikr by remember { mutableStateOf(dataManager.tasbeehDhikr) }
    var target by remember { mutableIntStateOf(dataManager.tasbeehTarget) }
    var vibrationEnabled by remember { mutableStateOf(dataManager.tasbeehVibration) }
    var lifetimeCount by remember { mutableLongStateOf(dataManager.tasbeehLifetime) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var showCustomDhikrDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    var isPressed by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "button_scale"
    )

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "المسبحة الإلكترونية",
                subtitle = "الإجمالي: $lifetimeCount تسبيحة",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("tasbeeh_history_action")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "سجل التسبيح", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("tasbeeh_reset_action")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تصفير العداد", tint = MaterialTheme.colorScheme.error)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Dhikr selector chips
            Text(
                text = "اختر الذكر:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetDhikrs) { dhikr ->
                    FilterChip(
                        selected = selectedDhikr == dhikr,
                        onClick = {
                            selectedDhikr = dhikr
                            dataManager.tasbeehDhikr = dhikr
                        },
                        label = { Text(dhikr, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    AssistChip(
                        onClick = { showCustomDhikrDialog = true },
                        label = { Text("+ ذكر مخصص") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الهدف:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))

                listOf(33, 100, 0).forEach { t ->
                    val label = when (t) {
                        0 -> "مفتوح"
                        else -> "$t"
                    }
                    ElevatedFilterChip(
                        selected = target == t,
                        onClick = {
                            target = t
                            dataManager.tasbeehTarget = t
                        },
                        label = { Text(label, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = IslamicGold,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                ElevatedFilterChip(
                    selected = target !in listOf(33, 100, 0),
                    onClick = { showCustomTargetDialog = true },
                    label = { Text(if (target !in listOf(33, 100, 0)) "$target" else "مخصص") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Visual Dial and Tap Button
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(scaleAnim)
                    .clip(CircleShape)
                    .background(IslamicGreenPrimary)
                    .border(
                        width = 4.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(IslamicGoldBright, IslamicGold, IslamicGoldBright)
                        ),
                        shape = CircleShape
                    )
                    .clickable {
                        val next = count + 1
                        count = next
                        dataManager.tasbeehCount = next

                        val nextLifetime = lifetimeCount + 1
                        lifetimeCount = nextLifetime
                        dataManager.tasbeehLifetime = nextLifetime

                        if (vibrationEnabled) {
                            if (target > 0 && next % target == 0) {
                                VibrationHelper.vibrateGoal(context)
                            } else {
                                VibrationHelper.vibrate(context, 35)
                            }
                        }
                    }
                    .testTag("tasbeeh_main_tap_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = selectedDhikr,
                        color = IslamicGoldBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$count",
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    if (target > 0) {
                        Text(
                            text = "من $target",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = (count % target).toFloat() / target.toFloat()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .width(120.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IslamicGoldBright,
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                    } else {
                        Text(
                            text = "تسبيح مستمر",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "اضغط للتسبيح",
                        color = IslamicGoldBright.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Settings & Toggles Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "اهتزاز اللمس",
                            tint = IslamicGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "الاهتزاز اللمسي",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            dataManager.tasbeehVibration = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IslamicGreenPrimary
                        )
                    )
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تصفير العداد") },
            text = { Text("هل أنت متأكد من تصفير العداد الحالي؟ سيتم حفظ التسبيحات في السجل.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (count > 0) {
                            dataManager.addTasbeehHistory("$selectedDhikr: $count مرة")
                        }
                        count = 0
                        dataManager.tasbeehCount = 0
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

    // Custom Target Dialog
    if (showCustomTargetDialog) {
        var inputVal by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = { Text("تحديد هدف مخصص") },
            text = {
                Column {
                    Text("أدخل عدد التسبيحات المستهدف:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { inputVal = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputVal.toIntOrNull()
                        if (num != null && num > 0) {
                            target = num
                            dataManager.tasbeehTarget = num
                            showCustomTargetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Custom Dhikr Dialog
    if (showCustomDhikrDialog) {
        var customText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomDhikrDialog = false },
            title = { Text("إضافة ذكر مخصص") },
            text = {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = { Text("اكتب صيغة الذكر...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customText.isNotBlank()) {
                            selectedDhikr = customText.trim()
                            dataManager.tasbeehDhikr = customText.trim()
                            showCustomDhikrDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                ) {
                    Text("استخدام")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDhikrDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // History Dialog
    if (showHistoryDialog) {
        val history = remember { dataManager.getTasbeehHistory() }
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("سجل التسبيح المحفوظ") },
            text = {
                if (history.isEmpty()) {
                    Text("لا توجد جلسات تسبيح سابقة محفوظة بعد.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        history.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
