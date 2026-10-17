package com.example.data.quran

import com.example.data.model.Ayah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object SurahQuranImporter {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchSurah(surahNumber: Int): Result<List<Ayah>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://surahquran.com/$surahNumber.html"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("فشل الاتصال بالموقع (رمز الاستجابة: ${response.code})"))
                }

                val html = response.body?.string() ?: return@withContext Result.failure(Exception("محتوى الصفحة فارغ"))
                
                // Parse the main verse container
                val verses = parseVersesFromHtml(html, surahNumber)
                if (verses.isEmpty()) {
                    return@withContext Result.failure(Exception("تعذر استخراج آيات السورة من صفحة الموقع"))
                }

                Result.success(verses)
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطأ في الاتصال بموقع سورة قرآن: ${e.localizedMessage}"))
        }
    }

    private fun parseVersesFromHtml(html: String, surahNumber: Int): List<Ayah> {
        val list = mutableListOf<Ayah>()

        // Find the div containing the verses text
        val containerPattern = Pattern.compile(
            "<div style=\"font-size:2em;[^\"]*\">(.*?)</div>",
            Pattern.DOTALL
        )
        val containerMatcher = containerPattern.matcher(html)
        if (!containerMatcher.find()) {
            return list
        }

        var content = containerMatcher.group(1) ?: return list

        // Remove header h2, title links, and footer paragraphs
        content = content.replace(Regex("<h2[^>]*>.*?</h2>", RegexOption.DOT_MATCHES_ALL), "")
        content = content.replace(Regex("<p[^>]*>.*?</p>", RegexOption.DOT_MATCHES_ALL), "")
        content = content.replace(Regex("<a[^>]*>.*?</a>", RegexOption.DOT_MATCHES_ALL), "")

        // Verses are separated by <label>(N)</label>
        val parts = content.split(Regex("<label>\\s*\\((\\d+)\\)\\s*</label>"))

        // Find all labels to match with parts
        val labelMatcher = Pattern.compile("<label>\\s*\\((\\d+)\\)\\s*</label>").matcher(content)
        val ayahNumbers = mutableListOf<Int>()
        while (labelMatcher.find()) {
            val numStr = labelMatcher.group(1)
            numStr?.toIntOrNull()?.let { ayahNumbers.add(it) }
        }

        val meta = QuranCatalog.allSurahsMetadata.find { it.number == surahNumber }
        val juz = meta?.juzStart ?: 1

        for (i in ayahNumbers.indices) {
            if (i < parts.size) {
                val rawText = parts[i]
                // Strip HTML tags like <br />, <b>, </b>
                val clean = rawText
                    .replace(Regex("<[^>]+>"), "")
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .trim()
                
                val singleSpaced = clean.replace(Regex("\\s+"), " ")

                if (singleSpaced.isNotBlank()) {
                    list.add(
                        Ayah(
                            numberInSurah = ayahNumbers[i],
                            textArabic = singleSpaced,
                            juz = juz
                        )
                    )
                }
            }
        }

        return list
    }
}
