package com.example.util

import java.util.Calendar
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameArabic: String,
    val dayNameArabic: String,
    val milestone: String? = null
)

object HijriCalendarHelper {

    val hijriMonthsArabic = listOf(
        "المحرّم", "صَفَر", "ربيع الأوّل", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجّة"
    )

    val weekDaysArabic = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    private val milestones: Map<Pair<Int, Int>, String> = mapOf(
        Pair(1, 1) to "رأس السنة الهجرية الجديدة",
        Pair(10, 1) to "يوم عاشوراء",
        Pair(12, 3) to "المولد النبوي الشريف",
        Pair(27, 7) to "ذكرى الإسراء والمعراج",
        Pair(15, 8) to "ليلة النصف من شعبان",
        Pair(1, 9) to "غرة شهر رمضان المبارك",
        Pair(17, 9) to "ذكرى غزوة بدر الكبرى",
        Pair(27, 9) to "ليلة 27 رمضان (ليلة القدر المرجوة)",
        Pair(1, 10) to "عيد الفطر المبارك",
        Pair(8, 12) to "يوم التروية",
        Pair(9, 12) to "يوم عرفة المبارك",
        Pair(10, 12) to "عيد الأضحى المبارك",
        Pair(11, 12) to "أول أيام التشريق",
        Pair(12, 12) to "ثاني أيام التشريق",
        Pair(13, 12) to "ثالث أيام التشريق"
    )

    fun getTodayHijri(manualOffsetDays: Int = 0): HijriDate {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, manualOffsetDays)
        return convertGregorianToHijri(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun convertGregorianToHijri(year: Int, month: Int, day: Int): HijriDate {
        val jd = gregorianToJulianDay(year, month, day)
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        val lPrime = l - 10631 * n + 354
        val j = (floor((10985 - lPrime) / 5316.0) * floor((50 * lPrime + 2459) / 17719.0) +
                floor(lPrime / 5670.0) * floor((43 * lPrime + 15238) / 15242.0)).toInt()
        val lDoublePrime = lPrime - (floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) +
                floor(j / 16.0) * floor((15242 * j) / 43.0)).toInt() + 29
        val hMonth = floor((24 * lDoublePrime) / 709.0).toInt()
        val hDay = lDoublePrime - floor((709 * hMonth) / 24.0).toInt()
        val hYear = 30 * n + j - 30

        val clampedMonth = hMonth.coerceIn(1, 12)
        val monthName = hijriMonthsArabic.getOrElse(clampedMonth - 1) { "شهر" }

        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day)
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        val dayName = weekDaysArabic.getOrElse(dayOfWeekIndex) { "اليوم" }

        val milestone = milestones[Pair(hDay, clampedMonth)]

        return HijriDate(
            day = hDay,
            month = clampedMonth,
            year = hYear,
            monthNameArabic = monthName,
            dayNameArabic = dayName,
            milestone = milestone
        )
    }

    private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Int {
        var y = year
        var m = month
        if (m < 3) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0).toInt()
        val b = 2 - a + floor(a / 4.0).toInt()
        return floor(365.25 * (y + 4716)).toInt() +
                floor(30.6001 * (m + 1)).toInt() + day + b - 1524
    }

    fun getTodayGregorianArabicFormatted(): String {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val months = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        val mName = months.getOrElse(month - 1) { "$month" }
        return "$day $mName $year م"
    }
}
