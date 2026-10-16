package com.example.data.model

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val ayahsCount: Int,
    val revelationType: RevelationType,
    val juzStart: Int,
    val ayahs: List<Ayah> = emptyList()
)

enum class RevelationType(val labelArabic: String) {
    MECCAN("مكية"),
    MEDINAN("مدنية")
}

data class Ayah(
    val numberInSurah: Int,
    val textArabic: String,
    val juz: Int = 1
)

data class Bookmark(
    val id: String,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class DhikrItem(
    val id: String,
    val categoryId: String,
    val text: String,
    val count: Int,
    val currentCount: Int = 0,
    val reward: String = "",
    val source: String = "",
    val isFavorite: Boolean = false
)

enum class AdhkarCategory(val id: String, val titleArabic: String, val iconName: String) {
    MORNING("morning", "أذكار الصباح", "wb_sunny"),
    EVENING("evening", "أذكار المساء", "nights_stay"),
    AFTER_PRAYER("after_prayer", "بعد الصلاة", "mosque"),
    SLEEP("sleep", "أذكار النوم", "bedtime"),
    WAKING("waking", "أذكار الاستيقاظ", "alarm"),
    GENERAL("general", "أذكار وأدعية مختارة", "auto_awesome")
}

data class TasbeehDhikr(
    val id: String,
    val text: String,
    val defaultTarget: Int
)

data class DailyWirdItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val category: String = "general",
    val target: Int = 1,
    val current: Int = 0,
    val isCompleted: Boolean = false
)

data class TadabburNote(
    val id: String,
    val title: String,
    val reflectionText: String,
    val surahNumber: Int? = null,
    val surahName: String = "",
    val ayahNumber: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalendarDayNote(
    val dateIso: String,
    val hijriString: String,
    val note: String
)
