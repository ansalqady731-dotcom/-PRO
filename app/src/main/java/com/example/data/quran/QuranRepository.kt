package com.example.data.quran

import android.content.Context
import com.example.data.model.Ayah
import com.example.data.model.PageItem
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import org.json.JSONArray
import org.json.JSONObject

object QuranRepository {

    private var surahsCache: List<Surah>? = null
    private var tafsirCache: Map<String, String>? = null
    private var pageItemsCache: List<PageItem>? = null

    @Synchronized
    fun initialize(context: Context) {
        if (surahsCache != null && tafsirCache != null) return

        try {
            // 1. Load Quran Uthmani Text from assets
            val quranJsonString = context.assets.open("quran/quran_uthmani.json").bufferedReader().use { it.readText() }
            val surahsArray = JSONArray(quranJsonString)

            val surahsList = mutableListOf<Surah>()
            val pageMap = mutableMapOf<Int, PageItem>()

            for (i in 0 until surahsArray.length()) {
                val sObj = surahsArray.getJSONObject(i)
                val sNum = sObj.getInt("num")
                val sName = sObj.getString("name")
                val sEnglish = sObj.getString("english")
                val sType = if (sObj.getString("type").equals("Meccan", ignoreCase = true))
                    RevelationType.MECCAN else RevelationType.MEDINAN
                val sCount = sObj.getInt("count")

                val ayahsArray = sObj.getJSONArray("ayahs")
                val ayahsList = mutableListOf<Ayah>()

                for (j in 0 until ayahsArray.length()) {
                    val aObj = ayahsArray.getJSONObject(j)
                    val aNum = aObj.getInt("num")
                    val aText = aObj.getString("text")
                    val aPage = aObj.getInt("page")
                    val aJuz = aObj.getInt("juz")
                    val aHizb = aObj.getInt("hizb")
                    val aSajda = aObj.optBoolean("sajda", false)

                    ayahsList.add(
                        Ayah(
                            numberInSurah = aNum,
                            textArabic = aText,
                            surahNumber = sNum,
                            juz = aJuz,
                            page = aPage,
                            hizbQuarter = aHizb,
                            sajda = aSajda
                        )
                    )

                    // Track first appearance of each page
                    if (!pageMap.containsKey(aPage)) {
                        pageMap[aPage] = PageItem(
                            pageNumber = aPage,
                            surahName = sName,
                            juzNumber = aJuz,
                            startAyahNumber = aNum
                        )
                    }
                }

                val juzStart = ayahsList.firstOrNull()?.juz ?: 1
                surahsList.add(
                    Surah(
                        number = sNum,
                        nameArabic = sName,
                        nameEnglish = sEnglish,
                        ayahsCount = sCount,
                        revelationType = sType,
                        juzStart = juzStart,
                        ayahs = ayahsList
                    )
                )
            }

            surahsCache = surahsList
            pageItemsCache = (1..604).map { p ->
                pageMap[p] ?: PageItem(p, "سورة", 1, 1)
            }

            // 2. Load Tafsir Al-Muyassar
            val tafsirJsonString = context.assets.open("quran/tafsir_muyassar.json").bufferedReader().use { it.readText() }
            val tObj = JSONObject(tafsirJsonString)
            val tMap = mutableMapOf<String, String>()
            val keys = tObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                tMap[k] = tObj.getString(k)
            }
            tafsirCache = tMap

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllSurahs(context: Context): List<Surah> {
        initialize(context)
        return surahsCache ?: QuranCatalog.allSurahsMetadata
    }

    fun getSurah(context: Context, surahNumber: Int): Surah? {
        initialize(context)
        return surahsCache?.find { it.number == surahNumber } ?: QuranCatalog.getSurah(surahNumber)
    }

    fun getAyahsByPage(context: Context, pageNumber: Int): List<Ayah> {
        initialize(context)
        val list = mutableListOf<Ayah>()
        surahsCache?.forEach { s ->
            s.ayahs.forEach { a ->
                if (a.page == pageNumber) list.add(a)
            }
        }
        return list
    }

    fun getAyahsByJuz(context: Context, juzNumber: Int): List<Ayah> {
        initialize(context)
        val list = mutableListOf<Ayah>()
        surahsCache?.forEach { s ->
            s.ayahs.forEach { a ->
                if (a.juz == juzNumber) list.add(a)
            }
        }
        return list
    }

    fun getAllPages(context: Context): List<PageItem> {
        initialize(context)
        return pageItemsCache ?: emptyList()
    }

    fun getTafsir(context: Context, surahNumber: Int, ayahNumber: Int): String {
        initialize(context)
        val key = "$surahNumber:$ayahNumber"
        return tafsirCache?.get(key) ?: "التفسير الميسر متاح أوفلاين في التطبيق."
    }

    fun searchVerses(context: Context, query: String): List<Ayah> {
        initialize(context)
        if (query.trim().length < 2) return emptyList()
        val q = query.trim()
        val results = mutableListOf<Ayah>()
        surahsCache?.forEach { s ->
            s.ayahs.forEach { a ->
                if (a.textArabic.contains(q, ignoreCase = true)) {
                    results.add(a)
                }
            }
        }
        return results
    }
}
