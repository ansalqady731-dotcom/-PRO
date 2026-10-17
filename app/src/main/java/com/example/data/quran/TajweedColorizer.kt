package com.example.data.quran

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

data class TajweedLegendItem(
    val color: Color,
    val title: String,
    val description: String
)

object TajweedColorizer {

    // Colors aligned with the official Dar Al-Maarifah / Azhar Tajweed Quran featured on surahquran.com
    val ColorMaddLong = Color(0xFFC62828)      // Red: المد اللازم (6 حركات)
    val ColorMaddMedium = Color(0xFFE65100)    // Orange/Coral: المد الواجب والمتصل والجائز (4-5 حركات)
    val ColorGhunnah = Color(0xFF2E7D32)       // Emerald Green: الغنة، الإخفاء، والإدغام بغنة
    val ColorQalqalah = Color(0xFF0288D1)      // Cyan/Sky Blue: حروف القلقلة (ق ط ب ج د) الساكنة
    val ColorTafkheem = Color(0xFF1565C0)      // Deep Blue: التفخيم ولفظ الجلالة
    val ColorSilent = Color(0xFF8E8E93)        // Gray: الحروف التي لا تُلفظ (همزة الوصل واللام الشمسية)

    val legendItems = listOf(
        TajweedLegendItem(
            color = ColorMaddLong,
            title = "أحمر داكن (المد اللازم)",
            description = "يمد 6 حركات وجوباً عند السكون الأصلي أو الشدة"
        ),
        TajweedLegendItem(
            color = ColorMaddMedium,
            title = "برتقالي / قرمزي (المدود)",
            description = "المد الواجب المتصل والمنفصل والعارض للسكون (4 إلى 5 حركات)"
        ),
        TajweedLegendItem(
            color = ColorGhunnah,
            title = "أخضر (الغنّة والإخفاء)",
            description = "أحكام الغنة، الإخفاء الحقيقي، الإدغام بغنة، والنون والميم المشددتان"
        ),
        TajweedLegendItem(
            color = ColorQalqalah,
            title = "أزرق سماوي (القلقلة)",
            description = "اضطراب المخرج عند النطق بحروف (ق، ط، ب، ج، د) عند سكونها"
        ),
        TajweedLegendItem(
            color = ColorTafkheem,
            title = "أزرق داكن (التفخيم)",
            description = "حروف الاستعلاء (خص ضغط قظ) والراء ولام لفظ الجلالة المفخمة"
        ),
        TajweedLegendItem(
            color = ColorSilent,
            title = "رمادي (لا يُلفظ)",
            description = "الحروف التي تكتب ولا تنطق كهمزة الوصل واللام الشمسية وما أدغم كاملاً"
        )
    )

    private val qalqalahLetters = setOf('ق', 'ط', 'ب', 'ج', 'د')
    private val maddCharacters = setOf('ٓ', 'آ')

    fun colorize(text: String, defaultColor: Color): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            var i = 0
            while (i < text.length) {
                val ch = text[i]

                // 1. Check Hamzat Al-Wasl (ٱ)
                if (ch == 'ٱ') {
                    addStyle(
                        SpanStyle(color = ColorSilent, fontWeight = FontWeight.Normal),
                        i,
                        i + 1
                    )
                }

                // 2. Check Madd signs (ـٓ or letters with madd mark)
                else if (maddCharacters.contains(ch)) {
                    val start = (i - 1).coerceAtLeast(0)
                    addStyle(
                        SpanStyle(color = ColorMaddLong, fontWeight = FontWeight.Bold),
                        start,
                        i + 1
                    )
                }

                // 3. Check Shaddah on Noon / Meem (الغنة المشددة)
                else if ((ch == 'ن' || ch == 'م') && i + 1 < text.length && text[i + 1] == 'ّ') {
                    addStyle(
                        SpanStyle(color = ColorGhunnah, fontWeight = FontWeight.Bold),
                        i,
                        i + 2
                    )
                    i++ // Skip shaddah
                }

                // 4. Check Qalqalah letters with sukun (قْ، طْ، بْ، جْ، دْ)
                else if (qalqalahLetters.contains(ch)) {
                    val hasSukun = i + 1 < text.length && (text[i + 1] == 'ْ' || text[i + 1] == 'ۡ')
                    if (hasSukun) {
                        addStyle(
                            SpanStyle(color = ColorQalqalah, fontWeight = FontWeight.Bold),
                            i,
                            i + 2
                        )
                        i++
                    }
                }

                // 5. Highlight لفظ الجلالة (اللَّه / لِلَّه)
                else if (ch == 'ل' && text.substring(i).startsWith("لَّهِ") || text.substring(i).startsWith("للَّهِ")) {
                    val end = (i + 5).coerceAtMost(text.length)
                    addStyle(
                        SpanStyle(color = ColorTafkheem, fontWeight = FontWeight.Bold),
                        i,
                        end
                    )
                }

                // 6. Check Tanween followed by Ghunnah / Ikhfaa
                else if (ch == 'ً' || ch == 'ٍ' || ch == 'ٌ') {
                    addStyle(
                        SpanStyle(color = ColorGhunnah, fontWeight = FontWeight.SemiBold),
                        i,
                        i + 1
                    )
                }

                i++
            }
        }
    }
}
