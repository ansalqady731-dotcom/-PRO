package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * An authentic Madina Mushaf Page frame replicating the traditional printed Quran page,
 * including ornate golden borders, corner arabesques, top header (Surah & Juz)
 * and bottom footer (Hizb & Page number).
 */
@Composable
fun QuranPageFrame(
    pageNumber: Int,
    surahName: String,
    juzNumber: Int,
    hizbQuarter: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val parchmentBg = if (isDarkTheme) QuranParchmentDark else QuranParchmentLight
    val outerGold = QuranFrameBorderGold
    val innerGold = QuranFrameInnerGold
    val textColor = if (isDarkTheme) Color(0xFFE2D9C8) else Color(0xFF3C311E)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(parchmentBg)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Outer decorative double frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.5.dp, outerGold, RoundedCornerShape(4.dp))
                .padding(3.dp)
                .border(1.dp, innerGold.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Draw decorative Islamic corner arabesques
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerSize = 16.dp.toPx()
                val strokeW = 1.5f

                // Top-Left corner motif
                drawPath(
                    path = Path().apply {
                        moveTo(0f, cornerSize)
                        lineTo(cornerSize, 0f)
                        moveTo(0f, cornerSize * 0.5f)
                        lineTo(cornerSize * 0.5f, 0f)
                    },
                    color = outerGold,
                    style = Stroke(width = strokeW)
                )

                // Top-Right corner motif
                drawPath(
                    path = Path().apply {
                        moveTo(size.width - cornerSize, 0f)
                        lineTo(size.width, cornerSize)
                        moveTo(size.width - cornerSize * 0.5f, 0f)
                        lineTo(size.width, cornerSize * 0.5f)
                    },
                    color = outerGold,
                    style = Stroke(width = strokeW)
                )

                // Bottom-Left corner motif
                drawPath(
                    path = Path().apply {
                        moveTo(0f, size.height - cornerSize)
                        lineTo(cornerSize, size.height)
                        moveTo(0f, size.height - cornerSize * 0.5f)
                        lineTo(cornerSize * 0.5f, size.height)
                    },
                    color = outerGold,
                    style = Stroke(width = strokeW)
                )

                // Bottom-Right corner motif
                drawPath(
                    path = Path().apply {
                        moveTo(size.width - cornerSize, size.height)
                        lineTo(size.width, size.height - cornerSize)
                        moveTo(size.width - cornerSize * 0.5f, size.height)
                        lineTo(size.width, size.height - cornerSize * 0.5f)
                    },
                    color = outerGold,
                    style = Stroke(width = strokeW)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Row (سورة كذا - الجزء كذا)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الجزء ${toArabicNumerals(juzNumber)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )

                    // Center decorative mini divider
                    Text(
                        text = "۞",
                        color = outerGold,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "سورة $surahName",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                }

                // Thin divider line under header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(innerGold.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Page Content (Surah banner + Verses)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    content = content
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Thin divider line above footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(innerGold.copy(alpha = 0.5f))
                )

                // Bottom Footer Row (رقم الصفحة والحزب)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hizbNumber = (hizbQuarter - 1) / 4 + 1
                    val quarterText = when (hizbQuarter % 4) {
                        1 -> "الحزب $hizbNumber"
                        2 -> "ربع الحزب $hizbNumber"
                        3 -> "نصف الحزب $hizbNumber"
                        0 -> "ثلاثة أرباع الحزب $hizbNumber"
                        else -> "الحزب $hizbNumber"
                    }

                    Text(
                        text = quarterText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.85f)
                        )
                    )

                    // Centered Page number in Islamic ornamental frame
                    Box(
                        modifier = Modifier
                            .border(1.dp, outerGold, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = toArabicNumerals(pageNumber),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        )
                    }

                    Text(
                        text = "صفحة $pageNumber",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.85f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Converts English digits to traditional Arabic-Indic numerals (١ ٢ ٣)
 */
fun toArabicNumerals(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = number.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            builder.append(arabicDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

/**
 * Traditional Ornate Surah Header Banner (إطار رأس السورة الزخرفي)
 * As seen in Madina Mushaf (Screenshot 2 and Screenshot 5)
 */
@Composable
fun QuranSurahHeaderBanner(
    surahName: String,
    ayahsCount: Int,
    revelationTypeLabel: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val goldBorder = QuranSurahHeaderBorder
    val bgGold = if (isDarkTheme) Color(0xFF2C261B) else QuranSurahHeaderBg
    val textColor = if (isDarkTheme) Color(0xFFF1E7D5) else Color(0xFF2A1F0D)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(bgGold, RoundedCornerShape(8.dp))
            .border(1.5.dp, goldBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "۞ $revelationTypeLabel ۞",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            )

            Text(
                text = "سُورَةُ $surahName",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = AmiriQuranFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            )

            Text(
                text = "۞ ${toArabicNumerals(ayahsCount)} آيَاتُهَا ۞",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            )
        }
    }
}

/**
 * Centered Bismillah with traditional Quran calligraphy typography
 */
@Composable
fun QuranBasmalah(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkTheme) Color(0xFFF5EEDB) else Color(0xFF241C10)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = AmiriQuranFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = textColor
            )
        )
    }
}
