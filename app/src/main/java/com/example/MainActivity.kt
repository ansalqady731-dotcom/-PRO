package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import com.example.data.local.DataManager
import com.example.ui.components.MuslimBottomNavigation
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MuslimProTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dataManager = DataManager(applicationContext)

        // Initialize local notification channel
        NotificationHelper.createNotificationChannel(applicationContext)

        setContent {
            MuslimCompanionApp(dataManager = dataManager)
        }
    }
}

@Composable
fun MuslimCompanionApp(dataManager: DataManager) {
    val context = LocalContext.current

    // Request notification permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Theme state
    var themeMode by remember { mutableStateOf(dataManager.themeMode) }
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    // Navigation state stack
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedReaderSurahNumber by remember { mutableIntStateOf(dataManager.lastReadSurah) }
    var selectedReaderPageNumber by remember { mutableStateOf<Int?>(dataManager.lastReadPage) }
    var initialAdhkarCategory by remember { mutableStateOf<String?>(null) }

    // Enforce 100% Arabic RTL (Right-to-Left) layout direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MuslimProTheme(darkTheme = isDarkTheme) {
            // Handle system back button for sub-screens
            if (currentScreen != Screen.Home) {
                BackHandler {
                    currentScreen = when (currentScreen) {
                        is Screen.QuranReader -> Screen.Quran
                        else -> Screen.Home
                    }
                }
            }

            val showBottomNav = currentScreen !is Screen.QuranReader

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (showBottomNav) {
                        MuslimBottomNavigation(
                            currentRoute = currentScreen.route,
                            onTabSelected = { screen ->
                                currentScreen = screen
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (showBottomNav) innerPadding else androidx.compose.foundation.layout.PaddingValues())
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                dataManager = dataManager,
                                onNavigateToQuran = { currentScreen = Screen.Quran },
                                onNavigateToQuranReader = { surahNum, pageNum ->
                                    selectedReaderSurahNumber = surahNum
                                    selectedReaderPageNumber = pageNum
                                    currentScreen = Screen.QuranReader(surahNum, pageNum)
                                },
                                onNavigateToAdhkar = { catId ->
                                    initialAdhkarCategory = catId
                                    currentScreen = Screen.Adhkar
                                },
                                onNavigateToTasbeeh = { currentScreen = Screen.Tasbeeh },
                                onNavigateToWird = { currentScreen = Screen.Wird },
                                onNavigateToCalendar = { currentScreen = Screen.Calendar },
                                onNavigateToTadabbur = { currentScreen = Screen.Tadabbur },
                                onNavigateToSearch = { currentScreen = Screen.Search },
                                onNavigateToSettings = { currentScreen = Screen.Settings }
                            )
                        }
                        is Screen.Quran -> {
                            QuranScreen(
                                dataManager = dataManager,
                                onNavigateToReader = { surahNum, pageNum ->
                                    selectedReaderSurahNumber = surahNum
                                    selectedReaderPageNumber = pageNum
                                    currentScreen = Screen.QuranReader(surahNum, pageNum)
                                },
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.QuranReader -> {
                            QuranReaderScreen(
                                surahNumber = screen.surahNumber,
                                initialPageNumber = screen.initialPageNumber,
                                dataManager = dataManager,
                                onBackClick = { currentScreen = Screen.Quran }
                            )
                        }
                        is Screen.Adhkar -> {
                            AdhkarScreen(
                                dataManager = dataManager,
                                initialCategory = initialAdhkarCategory,
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.Tasbeeh -> {
                            TasbeehScreen(
                                dataManager = dataManager,
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.Wird -> {
                            WirdScreen(
                                dataManager = dataManager,
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.Calendar -> {
                            CalendarScreen(
                                dataManager = dataManager,
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.Tadabbur -> {
                            TadabburScreen(
                                dataManager = dataManager,
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.Search -> {
                            SearchScreen(
                                dataManager = dataManager,
                                onNavigateToSurahReader = { surahNum ->
                                    selectedReaderSurahNumber = surahNum
                                    currentScreen = Screen.QuranReader(surahNum)
                                },
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.Settings -> {
                            SettingsScreen(
                                dataManager = dataManager,
                                onBackClick = { currentScreen = Screen.Home },
                                onThemeChanged = { newTheme ->
                                    themeMode = newTheme
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
