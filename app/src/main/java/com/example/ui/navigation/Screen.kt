package com.example.ui.navigation

sealed class Screen(val route: String, val titleArabic: String) {
    object Home : Screen("home", "الرئيسية")
    object Quran : Screen("quran", "القرآن الكريم")
    object Adhkar : Screen("adhkar", "الأذكار")
    object Tasbeeh : Screen("tasbeeh", "المسبحة")
    object Wird : Screen("wird", "وردي اليومي")
    object Calendar : Screen("calendar", "التقويم الهجري")
    object Tadabbur : Screen("tadabbur", "دفتر التدبر")
    object Search : Screen("search", "البحث الشامل")
    object Settings : Screen("settings", "الإعدادات")
    data class QuranReader(val surahNumber: Int, val initialPageNumber: Int? = null) : Screen("quran_reader/$surahNumber", "قراءة المصحف")
}
