package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Bookmark
import com.example.data.model.CalendarDayNote
import com.example.data.model.DailyWirdItem
import com.example.data.model.TadabburNote
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class DataManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("muslim_companion_pro_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "app_theme" // "system", "light", "dark"
        private const val KEY_FONT_SIZE = "quran_font_size"
        private const val KEY_HIJRI_OFFSET = "hijri_offset"

        private const val KEY_TASBEEH_COUNT = "tasbeeh_current_count"
        private const val KEY_TASBEEH_TARGET = "tasbeeh_target"
        private const val KEY_TASBEEH_DHIKR = "tasbeeh_dhikr"
        private const val KEY_TASBEEH_LIFETIME = "tasbeeh_lifetime"
        private const val KEY_TASBEEH_VIBRATION = "tasbeeh_vibration"
        private const val KEY_TASBEEH_HISTORY = "tasbeeh_history"

        private const val KEY_LAST_READ_SURAH = "last_read_surah"
        private const val KEY_LAST_READ_AYAH = "last_read_ayah"
        private const val KEY_LAST_READ_SURAH_NAME = "last_read_surah_name"

        private const val KEY_BOOKMARKS = "quran_bookmarks"
        private const val KEY_SURAH_FAVORITES = "quran_surah_favorites"
        private const val KEY_ADHKAR_FAVORITES = "adhkar_favorites"
        private const val KEY_ADHKAR_COUNTS = "adhkar_counts_today"

        private const val KEY_DAILY_WIRD = "daily_wird_items"
        private const val KEY_TADABBUR_NOTES = "tadabbur_notes"
        private const val KEY_CALENDAR_NOTES = "calendar_notes"

        private const val KEY_MORNING_REMINDER_ENABLED = "morning_reminder_enabled"
        private const val KEY_MORNING_REMINDER_TIME = "morning_reminder_time" // "06:00"
        private const val KEY_EVENING_REMINDER_ENABLED = "evening_reminder_enabled"
        private const val KEY_EVENING_REMINDER_TIME = "evening_reminder_time" // "17:30"
        
        private const val KEY_EXTERNAL_QURAN_DATA = "external_quran_imported_data"
    }

    // --- App Settings ---
    var themeMode: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var quranFontSize: Float
        get() = prefs.getFloat(KEY_FONT_SIZE, 24f)
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE, value).apply()

    var hijriOffset: Int
        get() = prefs.getInt(KEY_HIJRI_OFFSET, 0)
        set(value) = prefs.edit().putInt(KEY_HIJRI_OFFSET, value).apply()

    // --- Tasbeeh ---
    var tasbeehCount: Int
        get() = prefs.getInt(KEY_TASBEEH_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_TASBEEH_COUNT, value).apply()

    var tasbeehTarget: Int
        get() = prefs.getInt(KEY_TASBEEH_TARGET, 33)
        set(value) = prefs.edit().putInt(KEY_TASBEEH_TARGET, value).apply()

    var tasbeehDhikr: String
        get() = prefs.getString(KEY_TASBEEH_DHIKR, "سُبْحَانَ اللَّهِ") ?: "سُبْحَانَ اللَّهِ"
        set(value) = prefs.edit().putString(KEY_TASBEEH_DHIKR, value).apply()

    var tasbeehLifetime: Long
        get() = prefs.getLong(KEY_TASBEEH_LIFETIME, 0L)
        set(value) = prefs.edit().putLong(KEY_TASBEEH_LIFETIME, value).apply()

    var tasbeehVibration: Boolean
        get() = prefs.getBoolean(KEY_TASBEEH_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_TASBEEH_VIBRATION, value).apply()

    fun getTasbeehHistory(): List<String> {
        val raw = prefs.getString(KEY_TASBEEH_HISTORY, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addTasbeehHistory(entry: String) {
        val list = getTasbeehHistory().toMutableList()
        list.add(0, entry)
        if (list.size > 50) list.removeAt(list.lastIndex)
        val arr = JSONArray(list)
        prefs.edit().putString(KEY_TASBEEH_HISTORY, arr.toString()).apply()
    }

    fun clearTasbeehHistory() {
        prefs.edit().remove(KEY_TASBEEH_HISTORY).apply()
    }

    // --- Quran Last Read ---
    var lastReadSurah: Int
        get() = prefs.getInt(KEY_LAST_READ_SURAH, 1)
        set(value) = prefs.edit().putInt(KEY_LAST_READ_SURAH, value).apply()

    var lastReadAyah: Int
        get() = prefs.getInt(KEY_LAST_READ_AYAH, 1)
        set(value) = prefs.edit().putInt(KEY_LAST_READ_AYAH, value).apply()

    var lastReadSurahName: String
        get() = prefs.getString(KEY_LAST_READ_SURAH_NAME, "الفاتحة") ?: "الفاتحة"
        set(value) = prefs.edit().putString(KEY_LAST_READ_SURAH_NAME, value).apply()

    // --- Quran Bookmarks ---
    fun getBookmarks(): List<Bookmark> {
        val raw = prefs.getString(KEY_BOOKMARKS, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<Bookmark>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Bookmark(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        surahNumber = obj.getInt("surahNumber"),
                        surahName = obj.optString("surahName", ""),
                        ayahNumber = obj.getInt("ayahNumber"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        note = obj.optString("note", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveBookmark(bookmark: Bookmark) {
        val current = getBookmarks().filterNot { it.surahNumber == bookmark.surahNumber && it.ayahNumber == bookmark.ayahNumber }.toMutableList()
        current.add(0, bookmark)
        val arr = JSONArray()
        current.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("surahNumber", it.surahNumber)
            obj.put("surahName", it.surahName)
            obj.put("ayahNumber", it.ayahNumber)
            obj.put("timestamp", it.timestamp)
            obj.put("note", it.note)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_BOOKMARKS, arr.toString()).apply()
    }

    fun deleteBookmark(id: String) {
        val current = getBookmarks().filterNot { it.id == id }
        val arr = JSONArray()
        current.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("surahNumber", it.surahNumber)
            obj.put("surahName", it.surahName)
            obj.put("ayahNumber", it.ayahNumber)
            obj.put("timestamp", it.timestamp)
            obj.put("note", it.note)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_BOOKMARKS, arr.toString()).apply()
    }

    // --- Favorites ---
    fun getFavoriteSurahs(): Set<Int> {
        val raw = prefs.getStringSet(KEY_SURAH_FAVORITES, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleFavoriteSurah(surahNumber: Int): Boolean {
        val current = getFavoriteSurahs().toMutableSet()
        val isFav = if (current.contains(surahNumber)) {
            current.remove(surahNumber)
            false
        } else {
            current.add(surahNumber)
            true
        }
        prefs.edit().putStringSet(KEY_SURAH_FAVORITES, current.map { it.toString() }.toSet()).apply()
        return isFav
    }

    // --- Adhkar Favorites & Counters ---
    fun getFavoriteAdhkar(): Set<String> {
        return prefs.getStringSet(KEY_ADHKAR_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavoriteAdhkar(dhikrId: String): Boolean {
        val current = getFavoriteAdhkar().toMutableSet()
        val isFav = if (current.contains(dhikrId)) {
            current.remove(dhikrId)
            false
        } else {
            current.add(dhikrId)
            true
        }
        prefs.edit().putStringSet(KEY_ADHKAR_FAVORITES, current).apply()
        return isFav
    }

    fun getAdhkarCount(dhikrId: String): Int {
        val raw = prefs.getString(KEY_ADHKAR_COUNTS, "{}") ?: "{}"
        return try {
            val obj = JSONObject(raw)
            obj.optInt(dhikrId, 0)
        } catch (_: Exception) {
            0
        }
    }

    fun setAdhkarCount(dhikrId: String, count: Int) {
        val raw = prefs.getString(KEY_ADHKAR_COUNTS, "{}") ?: "{}"
        val obj = try { JSONObject(raw) } catch (_: Exception) { JSONObject() }
        obj.put(dhikrId, count)
        prefs.edit().putString(KEY_ADHKAR_COUNTS, obj.toString()).apply()
    }

    fun resetAllAdhkarCounts() {
        prefs.edit().remove(KEY_ADHKAR_COUNTS).apply()
    }

    // --- Daily Wird (وردي اليومي) ---
    fun getDailyWirdItems(): List<DailyWirdItem> {
        val raw = prefs.getString(KEY_DAILY_WIRD, null)
        if (raw == null) {
            // Initial default authentic spiritual goals
            return listOf(
                DailyWirdItem("w_1", "قراءة ورد القرآن اليومي", "حزب أو جزء بتدبر", "quran", 1, 0, false),
                DailyWirdItem("w_2", "أذكار الصباح", "التحصين وبداية اليوم بذكر الله", "adhkar", 1, 0, false),
                DailyWirdItem("w_3", "أذكار المساء", "الاستعاذة والشكر", "adhkar", 1, 0, false),
                DailyWirdItem("w_4", "الاستغفار والتسبيح 100 مرة", "سيد الاستغفار وسبحان الله وبحمده", "tasbeeh", 100, 0, false),
                DailyWirdItem("w_5", "صلاة الوتر وقراءة سورة الملك", "سنة مؤكدة قبل النوم", "prayer", 1, 0, false)
            )
        }
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<DailyWirdItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    DailyWirdItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        subtitle = obj.optString("subtitle", ""),
                        category = obj.optString("category", "general"),
                        target = obj.optInt("target", 1),
                        current = obj.optInt("current", 0),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveDailyWirdItems(items: List<DailyWirdItem>) {
        val arr = JSONArray()
        items.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("subtitle", it.subtitle)
            obj.put("category", it.category)
            obj.put("target", it.target)
            obj.put("current", it.current)
            obj.put("isCompleted", it.isCompleted)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_DAILY_WIRD, arr.toString()).apply()
    }

    // --- Tadabbur Notebook (دفتر التدبر) ---
    fun getTadabburNotes(): List<TadabburNote> {
        val raw = prefs.getString(KEY_TADABBUR_NOTES, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<TadabburNote>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TadabburNote(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        reflectionText = obj.getString("reflectionText"),
                        surahNumber = if (obj.has("surahNumber") && !obj.isNull("surahNumber")) obj.getInt("surahNumber") else null,
                        surahName = obj.optString("surahName", ""),
                        ayahNumber = if (obj.has("ayahNumber") && !obj.isNull("ayahNumber")) obj.getInt("ayahNumber") else null,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTadabburNote(note: TadabburNote) {
        val current = getTadabburNotes().filterNot { it.id == note.id }.toMutableList()
        current.add(0, note)
        persistTadabburNotes(current)
    }

    fun deleteTadabburNote(id: String) {
        val current = getTadabburNotes().filterNot { it.id == id }
        persistTadabburNotes(current)
    }

    private fun persistTadabburNotes(notes: List<TadabburNote>) {
        val arr = JSONArray()
        notes.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("reflectionText", it.reflectionText)
            it.surahNumber?.let { num -> obj.put("surahNumber", num) }
            obj.put("surahName", it.surahName)
            it.ayahNumber?.let { ayah -> obj.put("ayahNumber", ayah) }
            obj.put("timestamp", it.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_TADABBUR_NOTES, arr.toString()).apply()
    }

    // --- Calendar Notes ---
    fun getCalendarNotes(): Map<String, String> {
        val raw = prefs.getString(KEY_CALENDAR_NOTES, "{}") ?: "{}"
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getString(k)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun setCalendarNote(dateKey: String, note: String) {
        val map = getCalendarNotes().toMutableMap()
        if (note.trim().isEmpty()) {
            map.remove(dateKey)
        } else {
            map[dateKey] = note.trim()
        }
        val obj = JSONObject(map)
        prefs.edit().putString(KEY_CALENDAR_NOTES, obj.toString()).apply()
    }

    // --- Reminders Settings ---
    var isMorningReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_MORNING_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_MORNING_REMINDER_ENABLED, value).apply()

    var morningReminderTime: String
        get() = prefs.getString(KEY_MORNING_REMINDER_TIME, "06:00") ?: "06:00"
        set(value) = prefs.edit().putString(KEY_MORNING_REMINDER_TIME, value).apply()

    var isEveningReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_EVENING_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_EVENING_REMINDER_ENABLED, value).apply()

    var eveningReminderTime: String
        get() = prefs.getString(KEY_EVENING_REMINDER_TIME, "17:30") ?: "17:30"
        set(value) = prefs.edit().putString(KEY_EVENING_REMINDER_TIME, value).apply()

    // --- External Quran Data Importer ---
    fun getImportedQuranRaw(): String? = prefs.getString(KEY_EXTERNAL_QURAN_DATA, null)

    fun saveImportedQuranRaw(rawJson: String) {
        prefs.edit().putString(KEY_EXTERNAL_QURAN_DATA, rawJson).apply()
    }

    fun clearImportedQuranRaw() {
        prefs.edit().remove(KEY_EXTERNAL_QURAN_DATA).apply()
    }

    // --- Backup & Restore & Clear All ---
    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", "1.0.0")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "رفيق المسلم PRO")

        // Preferences
        val prefObj = JSONObject()
        prefObj.put("themeMode", themeMode)
        prefObj.put("quranFontSize", quranFontSize.toDouble())
        prefObj.put("hijriOffset", hijriOffset)
        prefObj.put("tasbeehLifetime", tasbeehLifetime)
        prefObj.put("tasbeehVibration", tasbeehVibration)
        root.put("settings", prefObj)

        // Bookmarks
        val bookmarksArr = JSONArray()
        getBookmarks().forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("surahNumber", b.surahNumber)
            bObj.put("surahName", b.surahName)
            bObj.put("ayahNumber", b.ayahNumber)
            bObj.put("timestamp", b.timestamp)
            bObj.put("note", b.note)
            bookmarksArr.put(bObj)
        }
        root.put("bookmarks", bookmarksArr)

        // Tadabbur Notes
        val tadabburArr = JSONArray()
        getTadabburNotes().forEach { n ->
            val nObj = JSONObject()
            nObj.put("id", n.id)
            nObj.put("title", n.title)
            nObj.put("reflectionText", n.reflectionText)
            n.surahNumber?.let { nObj.put("surahNumber", it) }
            nObj.put("surahName", n.surahName)
            n.ayahNumber?.let { nObj.put("ayahNumber", it) }
            nObj.put("timestamp", n.timestamp)
            tadabburArr.put(nObj)
        }
        root.put("tadabburNotes", tadabburArr)

        // Daily Wird
        val wirdArr = JSONArray()
        getDailyWirdItems().forEach { w ->
            val wObj = JSONObject()
            wObj.put("id", w.id)
            wObj.put("title", w.title)
            wObj.put("subtitle", w.subtitle)
            wObj.put("category", w.category)
            wObj.put("target", w.target)
            wObj.put("current", w.current)
            wObj.put("isCompleted", w.isCompleted)
            wirdArr.put(wObj)
        }
        root.put("dailyWird", wirdArr)

        // Calendar Notes
        root.put("calendarNotes", JSONObject(getCalendarNotes()))

        return root.toString(2)
    }

    fun restoreBackupJson(jsonString: String): Result<String> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("bookmarks") && !root.has("tadabburNotes") && !root.has("settings")) {
                return Result.failure(IllegalArgumentException("تنسيق ملف النسخة الاحتياطية غير صالح أو تالف"))
            }

            // Restore settings
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                if (s.has("themeMode")) themeMode = s.getString("themeMode")
                if (s.has("quranFontSize")) quranFontSize = s.getDouble("quranFontSize").toFloat()
                if (s.has("hijriOffset")) hijriOffset = s.getInt("hijriOffset")
                if (s.has("tasbeehLifetime")) tasbeehLifetime = s.getLong("tasbeehLifetime")
                if (s.has("tasbeehVibration")) tasbeehVibration = s.getBoolean("tasbeehVibration")
            }

            // Restore Bookmarks
            if (root.has("bookmarks")) {
                val bArr = root.getJSONArray("bookmarks")
                val bList = mutableListOf<Bookmark>()
                for (i in 0 until bArr.length()) {
                    val obj = bArr.getJSONObject(i)
                    bList.add(
                        Bookmark(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            surahNumber = obj.getInt("surahNumber"),
                            surahName = obj.optString("surahName", ""),
                            ayahNumber = obj.getInt("ayahNumber"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            note = obj.optString("note", "")
                        )
                    )
                }
                bList.forEach { saveBookmark(it) }
            }

            // Restore Tadabbur Notes
            if (root.has("tadabburNotes")) {
                val nArr = root.getJSONArray("tadabburNotes")
                val nList = mutableListOf<TadabburNote>()
                for (i in 0 until nArr.length()) {
                    val obj = nArr.getJSONObject(i)
                    nList.add(
                        TadabburNote(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.getString("title"),
                            reflectionText = obj.getString("reflectionText"),
                            surahNumber = if (obj.has("surahNumber") && !obj.isNull("surahNumber")) obj.getInt("surahNumber") else null,
                            surahName = obj.optString("surahName", ""),
                            ayahNumber = if (obj.has("ayahNumber") && !obj.isNull("ayahNumber")) obj.getInt("ayahNumber") else null,
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                persistTadabburNotes(nList)
            }

            // Restore Daily Wird
            if (root.has("dailyWird")) {
                val wArr = root.getJSONArray("dailyWird")
                val wList = mutableListOf<DailyWirdItem>()
                for (i in 0 until wArr.length()) {
                    val obj = wArr.getJSONObject(i)
                    wList.add(
                        DailyWirdItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.getString("title"),
                            subtitle = obj.optString("subtitle", ""),
                            category = obj.optString("category", "general"),
                            target = obj.optInt("target", 1),
                            current = obj.optInt("current", 0),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
                saveDailyWirdItems(wList)
            }

            // Restore Calendar Notes
            if (root.has("calendarNotes")) {
                val cObj = root.getJSONObject("calendarNotes")
                val keys = cObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    setCalendarNote(k, cObj.getString(k))
                }
            }

            Result.success("تم استرجاع النسخة الاحتياطية بنجاح تام")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearAllUserData() {
        prefs.edit().clear().apply()
    }
}
