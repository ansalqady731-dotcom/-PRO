package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreenPrimary

data class NavigationTab(
    val screen: Screen,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)

@Composable
fun MuslimBottomNavigation(
    currentRoute: String,
    onTabSelected: (Screen) -> Unit
) {
    val tabs = listOf(
        NavigationTab(Screen.Home, "الرئيسية", Icons.Default.Home, "nav_home"),
        NavigationTab(Screen.Quran, "القرآن", Icons.Default.MenuBook, "nav_quran"),
        NavigationTab(Screen.Adhkar, "الأذكار", Icons.Default.WbSunny, "nav_adhkar"),
        NavigationTab(Screen.Tasbeeh, "المسبحة", Icons.Default.Fingerprint, "nav_tasbeeh"),
        NavigationTab(Screen.Wird, "وردي", Icons.Default.TaskAlt, "nav_wird")
    )

    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("muslim_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = IslamicGreenPrimary
    ) {
        tabs.forEach { tab ->
            val isSelected = currentRoute == tab.screen.route || (tab.screen == Screen.Quran && currentRoute.startsWith("quran_reader"))
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.screen) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = IslamicGreenPrimary,
                    indicatorColor = IslamicGreenPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}
