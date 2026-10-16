package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataManager
import com.example.ui.components.MuslimTopBar
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.IslamicGreenPrimary
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dataManager: DataManager,
    onBackClick: () -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current

    var currentTheme by remember { mutableStateOf(dataManager.themeMode) }
    var fontSize by remember { mutableFloatStateOf(dataManager.quranFontSize) }
    var hijriOffset by remember { mutableIntStateOf(dataManager.hijriOffset) }
    var tasbeehVibration by remember { mutableStateOf(dataManager.tasbeehVibration) }

    var isMorningEnabled by remember { mutableStateOf(dataManager.isMorningReminderEnabled) }
    var morningTime by remember { mutableStateOf(dataManager.morningReminderTime) }
    var isEveningEnabled by remember { mutableStateOf(dataManager.isEveningReminderEnabled) }
    var eveningTime by remember { mutableStateOf(dataManager.eveningReminderTime) }

    var hasNotificationPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            MuslimTopBar(
                title = "الإعدادات والنسخ الاحتياطي",
                subtitle = "الخصوصية والتحكم الكامل",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Theme Card ---
            SettingsSectionCard(title = "المظهر والشاشة", icon = Icons.Default.Palette) {
                Text(text = "نمط العرض:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        Triple("system", "تلقائي", Icons.Default.BrightnessAuto),
                        Triple("light", "نهاري", Icons.Default.LightMode),
                        Triple("dark", "ليلي", Icons.Default.DarkMode)
                    )
                    themes.forEach { (key, label, icon) ->
                        ElevatedFilterChip(
                            selected = currentTheme == key,
                            onClick = {
                                currentTheme = key
                                dataManager.themeMode = key
                                onThemeChanged(key)
                            },
                            label = { Text(label) },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                selectedContainerColor = IslamicGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Font size slider
                Text(
                    text = "حجم خط القراءة القرآني: ${fontSize.toInt()} نقطة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = fontSize,
                    onValueChange = {
                        fontSize = it
                        dataManager.quranFontSize = it
                    },
                    valueRange = 18f..40f,
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGreenPrimary,
                        activeTrackColor = IslamicGreenPrimary
                    )
                )
            }

            // --- Notifications & Reminders ---
            SettingsSectionCard(title = "التذكيرات والتنبيهات", icon = Icons.Default.Notifications) {
                // Permission Status Indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasNotificationPermission) IslamicGreenPrimary.copy(alpha = 0.12f) else IslamicGold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasNotificationPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasNotificationPermission) IslamicGreenPrimary else IslamicGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasNotificationPermission) "صلاحية الإشعارات مفعلة ومؤكدة" else "يرجى منح صلاحية الإشعارات للتطبيق",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Morning Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "تذكير أذكار الصباح", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "الوقت: $morningTime صباحاً", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isMorningEnabled,
                        onCheckedChange = {
                            isMorningEnabled = it
                            dataManager.isMorningReminderEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IslamicGreenPrimary)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Evening Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "تذكير أذكار المساء", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "الوقت: $eveningTime مساءً", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isEveningEnabled,
                        onCheckedChange = {
                            isEveningEnabled = it
                            dataManager.isEveningReminderEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IslamicGreenPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Test Notification Button
                OutlinedButton(
                    onClick = {
                        val sent = NotificationHelper.sendTestNotification(
                            context,
                            "رفيق المسلم PRO",
                            "حان موعد أذكار الصباح: ألا بذكر الله تطمئن القلوب"
                        )
                        if (sent) {
                            Toast.makeText(context, "تم إرسال إشعار تجريبي بنجاح", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "تعذر إرسال الإشعار، يرجى فحص صلاحيات النظام", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_notification_btn")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اختبار إرسال إشعار تجريبي الآن")
                }
            }

            // --- Tasbeeh Preferences ---
            SettingsSectionCard(title = "المسبحة والاهتزاز", icon = Icons.Default.Fingerprint) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "الاهتزاز عند الضغط", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "يعطي نبضة لمسية خفيفة لكل تسبيحة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = tasbeehVibration,
                        onCheckedChange = {
                            tasbeehVibration = it
                            dataManager.tasbeehVibration = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IslamicGreenPrimary)
                    )
                }
            }

            // --- Backup & Restore ---
            SettingsSectionCard(title = "النسخ الاحتياطي واستعادة البيانات", icon = Icons.Default.Save) {
                Text(
                    text = "جميع بياناتك محفوظة محلياً على جهازك فقط حفاظاً على خصوصيتك الكاملة. يمكنك تصدير نسخة احتياطية أو استعادتها بأمان:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            exportedJsonText = dataManager.exportBackupJson()
                            showExportDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_backup_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير النسخة")
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_backup_btn")
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استيراد نسخة")
                    }
                }
            }

            // --- Clear Data Danger Zone ---
            SettingsSectionCard(title = "إعادة ضبط البيانات", icon = Icons.Default.DeleteForever) {
                Text(
                    text = "حذف جميع الملاحظات والعلامات المرجعية وسجل التسبيح والعودة للحالة الأولية:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showClearDataDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_all_data_btn")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف جميع بيانات المستخدم بعد التأكيد")
                }
            }

            // App Identity & Offline info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "رفيق المسلم PRO • الإصدار 1.0",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGold
                )
                Text(
                    text = "تطبيق إسلامي كامل 100% يعمل دون اتصال بالإنترنت",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("النسخة الاحتياطية جاهزة") },
            text = {
                Column {
                    Text("تم إنشاء النسخة الاحتياطية بنجاح بصيغة JSON. يمكنك نسخها أو مشاركتها لحفظها في مكان آمن:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("MuslimProBackup", exportedJsonText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ محتوى النسخة إلى الحافظة", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                ) {
                    Text("نسخ للحافظة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        var importInput by remember { mutableStateOf("") }
        var resultMessage by remember { mutableStateOf<String?>(null) }
        var isSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("استيراد نسخة احتياطية") },
            text = {
                Column {
                    Text("الصق نص النسخة الاحتياطية بصيغة JSON وسيتم التحقق من صحتها قبل الاستيراد:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInput,
                        onValueChange = { importInput = it },
                        placeholder = { Text("الصق بيانات JSON هنا...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                    if (resultMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resultMessage ?: "",
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
                        val res = dataManager.restoreBackupJson(importInput)
                        if (res.isSuccess) {
                            resultMessage = res.getOrNull()
                            isSuccess = true
                            Toast.makeText(context, "تمت الاستعادة بنجاح", Toast.LENGTH_SHORT).show()
                        } else {
                            resultMessage = "فشل الاستيراد: ${res.exceptionOrNull()?.message}"
                            isSuccess = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                ) {
                    Text("تحقق واستعادة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("تأكيد حذف جميع البيانات") },
            text = { Text("هل أنت متأكد تماماً من حذف كافة العلامات المرجعية وخواطر التدبر وعدادات الأذكار والمسبحة؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        dataManager.clearAllUserData()
                        showClearDataDialog = false
                        Toast.makeText(context, "تم حذف جميع البيانات بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، احذف نهائياً")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = IslamicGreenPrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
