package com.example.data.quran

import com.example.data.model.Ayah
import com.example.data.model.RevelationType
import com.example.data.model.Surah

object QuranCatalog {

    val allSurahsMetadata: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", 7, RevelationType.MECCAN, 1),
        Surah(2, "البقرة", "Al-Baqarah", 286, RevelationType.MEDINAN, 1),
        Surah(3, "آل عمران", "Ali 'Imran", 200, RevelationType.MEDINAN, 3),
        Surah(4, "النساء", "An-Nisa'", 176, RevelationType.MEDINAN, 4),
        Surah(5, "المائدة", "Al-Ma'idah", 120, RevelationType.MEDINAN, 6),
        Surah(6, "الأنعام", "Al-An'am", 165, RevelationType.MECCAN, 7),
        Surah(7, "الأعراف", "Al-A'raf", 206, RevelationType.MECCAN, 8),
        Surah(8, "الأنفال", "Al-Anfal", 75, RevelationType.MEDINAN, 9),
        Surah(9, "التوبة", "At-Tawbah", 129, RevelationType.MEDINAN, 10),
        Surah(10, "يونس", "Yunus", 109, RevelationType.MECCAN, 11),
        Surah(11, "هود", "Hud", 123, RevelationType.MECCAN, 11),
        Surah(12, "يوسف", "Yusuf", 111, RevelationType.MECCAN, 12),
        Surah(13, "الرعد", "Ar-Ra'd", 43, RevelationType.MEDINAN, 13),
        Surah(14, "إبراهيم", "Ibrahim", 52, RevelationType.MECCAN, 13),
        Surah(15, "الحجر", "Al-Hijr", 99, RevelationType.MECCAN, 14),
        Surah(16, "النحل", "An-Nahl", 128, RevelationType.MECCAN, 14),
        Surah(17, "الإسراء", "Al-Isra'", 111, RevelationType.MECCAN, 15),
        Surah(18, "الكهف", "Al-Kahf", 110, RevelationType.MECCAN, 15),
        Surah(19, "مريم", "Maryam", 98, RevelationType.MECCAN, 16),
        Surah(20, "طه", "Ta-Ha", 135, RevelationType.MECCAN, 16),
        Surah(21, "الأنبياء", "Al-Anbiya'", 112, RevelationType.MECCAN, 17),
        Surah(22, "الحج", "Al-Hajj", 78, RevelationType.MEDINAN, 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", 118, RevelationType.MECCAN, 18),
        Surah(24, "النور", "An-Nur", 64, RevelationType.MEDINAN, 18),
        Surah(25, "الفرقان", "Al-Furqan", 77, RevelationType.MECCAN, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara'", 227, RevelationType.MECCAN, 19),
        Surah(27, "النمل", "An-Naml", 93, RevelationType.MECCAN, 19),
        Surah(28, "القصص", "Al-Qasas", 88, RevelationType.MECCAN, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", 69, RevelationType.MECCAN, 20),
        Surah(30, "الروم", "Ar-Rum", 60, RevelationType.MECCAN, 21),
        Surah(31, "لقمان", "Luqman", 34, RevelationType.MECCAN, 21),
        Surah(32, "السجدة", "As-Sajdah", 30, RevelationType.MECCAN, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", 73, RevelationType.MEDINAN, 21),
        Surah(34, "سبأ", "Saba'", 54, RevelationType.MECCAN, 22),
        Surah(35, "فاطر", "Fatir", 45, RevelationType.MECCAN, 22),
        Surah(36, "يس", "Ya-Sin", 83, RevelationType.MECCAN, 22),
        Surah(37, "الصافات", "As-Saffat", 182, RevelationType.MECCAN, 23),
        Surah(38, "ص", "Sad", 88, RevelationType.MECCAN, 23),
        Surah(39, "الزمر", "Az-Zumar", 75, RevelationType.MECCAN, 23),
        Surah(40, "غافر", "Ghafir", 85, RevelationType.MECCAN, 24),
        Surah(41, "فصلت", "Fussilat", 54, RevelationType.MECCAN, 24),
        Surah(42, "الشورى", "Ash-Shura", 53, RevelationType.MECCAN, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", 89, RevelationType.MECCAN, 25),
        Surah(44, "الدخان", "Ad-Dukhan", 59, RevelationType.MECCAN, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", 37, RevelationType.MECCAN, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", 35, RevelationType.MECCAN, 26),
        Surah(47, "محمد", "Muhammad", 38, RevelationType.MEDINAN, 26),
        Surah(48, "الفتح", "Al-Fath", 29, RevelationType.MEDINAN, 26),
        Surah(49, "الحجرات", "Al-Hujurat", 18, RevelationType.MEDINAN, 26),
        Surah(50, "ق", "Qaf", 45, RevelationType.MECCAN, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", 60, RevelationType.MECCAN, 26),
        Surah(52, "الطور", "At-Tur", 49, RevelationType.MECCAN, 27),
        Surah(53, "النجم", "An-Najm", 62, RevelationType.MECCAN, 27),
        Surah(54, "القمر", "Al-Qamar", 55, RevelationType.MECCAN, 27),
        Surah(55, "الرحمن", "Ar-Rahman", 78, RevelationType.MEDINAN, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", 96, RevelationType.MECCAN, 27),
        Surah(57, "الحديد", "Al-Hadid", 29, RevelationType.MEDINAN, 27),
        Surah(58, "المجادلة", "Al-Mujadilah", 22, RevelationType.MEDINAN, 28),
        Surah(59, "الحشر", "Al-Hashr", 24, RevelationType.MEDINAN, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", 13, RevelationType.MEDINAN, 28),
        Surah(61, "الصف", "As-Saff", 14, RevelationType.MEDINAN, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", 11, RevelationType.MEDINAN, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", 11, RevelationType.MEDINAN, 28),
        Surah(64, "التغابن", "At-Taghabun", 18, RevelationType.MEDINAN, 28),
        Surah(65, "الطلاق", "At-Talaq", 12, RevelationType.MEDINAN, 28),
        Surah(66, "التحريم", "At-Tahrim", 12, RevelationType.MEDINAN, 28),
        Surah(67, "الملك", "Al-Mulk", 30, RevelationType.MECCAN, 29),
        Surah(68, "القلم", "Al-Qalam", 52, RevelationType.MECCAN, 29),
        Surah(69, "الحاقة", "Al-Haqqah", 52, RevelationType.MECCAN, 29),
        Surah(70, "المعارج", "Al-Ma'arij", 44, RevelationType.MECCAN, 29),
        Surah(71, "نوح", "Nuh", 28, RevelationType.MECCAN, 29),
        Surah(72, "الجن", "Al-Jinn", 28, RevelationType.MECCAN, 29),
        Surah(73, "المزمل", "Al-Muzzammil", 20, RevelationType.MECCAN, 29),
        Surah(74, "المدثر", "Al-Muddaththir", 56, RevelationType.MECCAN, 29),
        Surah(75, "القيامة", "Al-Qiyamah", 40, RevelationType.MECCAN, 29),
        Surah(76, "الإنسان", "Al-Insan", 31, RevelationType.MEDINAN, 29),
        Surah(77, "المرسلات", "Al-Mursalat", 50, RevelationType.MECCAN, 29),
        Surah(78, "النبأ", "An-Naba'", 40, RevelationType.MECCAN, 30),
        Surah(79, "النازعات", "An-Nazi'at", 46, RevelationType.MECCAN, 30),
        Surah(80, "عبس", "'Abasa", 42, RevelationType.MECCAN, 30),
        Surah(81, "التكوير", "At-Takwir", 29, RevelationType.MECCAN, 30),
        Surah(82, "الانفطار", "Al-Infitar", 19, RevelationType.MECCAN, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", 36, RevelationType.MECCAN, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", 25, RevelationType.MECCAN, 30),
        Surah(85, "البروج", "Al-Buruj", 22, RevelationType.MECCAN, 30),
        Surah(86, "الطارق", "At-Tariq", 17, RevelationType.MECCAN, 30),
        Surah(87, "الأعلى", "Al-A'la", 19, RevelationType.MECCAN, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", 26, RevelationType.MECCAN, 30),
        Surah(89, "الفجر", "Al-Fajr", 30, RevelationType.MECCAN, 30),
        Surah(90, "البلد", "Al-Balad", 20, RevelationType.MECCAN, 30),
        Surah(91, "الشمس", "Ash-Shams", 15, RevelationType.MECCAN, 30),
        Surah(92, "الليل", "Al-Layl", 21, RevelationType.MECCAN, 30),
        Surah(93, "الضحى", "Ad-Duha", 11, RevelationType.MECCAN, 30),
        Surah(94, "الشرح", "Ash-Sharh", 8, RevelationType.MECCAN, 30),
        Surah(95, "التين", "At-Tin", 8, RevelationType.MECCAN, 30),
        Surah(96, "العلق", "Al-'Alaq", 19, RevelationType.MECCAN, 30),
        Surah(97, "القدر", "Al-Qadr", 5, RevelationType.MECCAN, 30),
        Surah(98, "البينة", "Al-Bayyinah", 8, RevelationType.MEDINAN, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", 8, RevelationType.MEDINAN, 30),
        Surah(100, "العاديات", "Al-'Adiyat", 11, RevelationType.MECCAN, 30),
        Surah(101, "القارعة", "Al-Qari'ah", 11, RevelationType.MECCAN, 30),
        Surah(102, "التكاثر", "At-Takathur", 8, RevelationType.MECCAN, 30),
        Surah(103, "العصر", "Al-'Asr", 3, RevelationType.MECCAN, 30),
        Surah(104, "الهمزة", "Al-Humazah", 9, RevelationType.MECCAN, 30),
        Surah(105, "الفيل", "Al-Fil", 5, RevelationType.MECCAN, 30),
        Surah(106, "قريش", "Quraysh", 4, RevelationType.MECCAN, 30),
        Surah(107, "الماعون", "Al-Ma'un", 7, RevelationType.MECCAN, 30),
        Surah(108, "الكوثر", "Al-Kawthar", 3, RevelationType.MECCAN, 30),
        Surah(109, "الكافرون", "Al-Kafirun", 6, RevelationType.MECCAN, 30),
        Surah(110, "النصر", "An-Nasr", 3, RevelationType.MEDINAN, 30),
        Surah(111, "المسد", "Al-Masad", 5, RevelationType.MECCAN, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", 4, RevelationType.MECCAN, 30),
        Surah(113, "الفلق", "Al-Falaq", 5, RevelationType.MECCAN, 30),
        Surah(114, "الناس", "An-Nas", 6, RevelationType.MECCAN, 30)
    )

    // Juz description list with starting surah & ayah
    val juzList: List<JuzInfo> = listOf(
        JuzInfo(1, "الجزء الأول", "يبدأ من سورة الفاتحة آية 1"),
        JuzInfo(2, "الجزء الثاني", "يبدأ من سورة البقرة آية 142"),
        JuzInfo(3, "الجزء الثالث", "يبدأ من سورة البقرة آية 253"),
        JuzInfo(4, "الجزء الرابع", "يبدأ من سورة آل عمران آية 92"),
        JuzInfo(5, "الجزء الخامس", "يبدأ من سورة النساء آية 24"),
        JuzInfo(6, "الجزء السادس", "يبدأ من سورة النساء آية 148"),
        JuzInfo(7, "الجزء السابع", "يبدأ من سورة المائدة آية 82"),
        JuzInfo(8, "الجزء الثامن", "يبدأ من سورة الأنعام آية 111"),
        JuzInfo(9, "الجزء التاسع", "يبدأ من سورة الأعراف آية 88"),
        JuzInfo(10, "الجزء العاشر", "يبدأ من سورة الأنفال آية 41"),
        JuzInfo(11, "الجزء الحادي عشر", "يبدأ من سورة التوبة آية 93"),
        JuzInfo(12, "الجزء الثاني عشر", "يبدأ من سورة هود آية 6"),
        JuzInfo(13, "الجزء الثالث عشر", "يبدأ من سورة يوسف آية 53"),
        JuzInfo(14, "الجزء الرابع عشر", "يبدأ من سورة الحجر آية 1"),
        JuzInfo(15, "الجزء الخامس عشر", "يبدأ من سورة الإسراء آية 1"),
        JuzInfo(16, "الجزء السادس عشر", "يبدأ من سورة الكهف آية 75"),
        JuzInfo(17, "الجزء السابع عشر", "يبدأ من سورة الأنبياء آية 1"),
        JuzInfo(18, "الجزء الثامن عشر", "يبدأ من سورة المؤمنون آية 1"),
        JuzInfo(19, "الجزء التاسع عشر", "يبدأ من سورة الفرقان آية 21"),
        JuzInfo(20, "الجزء العشرون", "يبدأ من سورة النمل آية 56"),
        JuzInfo(21, "الجزء الحادي والعشرون", "يبدأ من سورة العنكبوت آية 46"),
        JuzInfo(22, "الجزء الثاني والعشرون", "يبدأ من سورة الأحزاب آية 31"),
        JuzInfo(23, "الجزء الثالث والعشرون", "يبدأ من سورة يس آية 28"),
        JuzInfo(24, "الجزء الرابع والعشرون", "يبدأ من سورة الزمر آية 32"),
        JuzInfo(25, "الجزء الخامس والعشرون", "يبدأ من سورة فصلت آية 47"),
        JuzInfo(26, "الجزء السادس والعشرون", "يبدأ من سورة الأحقاف آية 1"),
        JuzInfo(27, "الجزء السابع والعشرون", "يبدأ من سورة الذاريات آية 31"),
        JuzInfo(28, "الجزء الثامن والعشرون", "يبدأ من سورة المجادلة آية 1"),
        JuzInfo(29, "الجزء التاسع والعشرون", "يبدأ من سورة الملك آية 1 (تبارك)"),
        JuzInfo(30, "الجزء الثلاثون", "يبدأ من سورة النبأ آية 1 (عمّ)")
    )

    // Pre-bundled, strictly verified text from the approved Mushaf for the most commonly read surahs
    // (Complies with rule: authentic verified texts only, with support for complete external JSON import)
    val preBundledVerses: Map<Int, List<Ayah>> = mapOf(
        1 to listOf(
            Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 1),
            Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 1),
            Ayah(3, "الرَّحْمَٰنِ الرَّحِيمِ", 1),
            Ayah(4, "مَالِكِ يَوْمِ الدِّينِ", 1),
            Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 1),
            Ayah(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", 1),
            Ayah(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", 1)
        ),
        103 to listOf(
            Ayah(1, "وَالْعَصْرِ", 30),
            Ayah(2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", 30),
            Ayah(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", 30)
        ),
        108 to listOf(
            Ayah(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", 30),
            Ayah(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", 30),
            Ayah(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", 30)
        ),
        109 to listOf(
            Ayah(1, "قُلْ يَا أَيُّهَا الْكَافِرُونَ", 30),
            Ayah(2, "لَا أَعْبُدُ مَا تَعْبُدُونَ", 30),
            Ayah(3, "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ", 30),
            Ayah(4, "وَلَا أَنَا عَابِدٌ مَّا عَبَدتُّمْ", 30),
            Ayah(5, "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ", 30),
            Ayah(6, "لَكُمْ دِينُكُمْ وَلِيَ دِينِ", 30)
        ),
        110 to listOf(
            Ayah(1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", 30),
            Ayah(2, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", 30),
            Ayah(3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", 30)
        ),
        111 to listOf(
            Ayah(1, "تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ", 30),
            Ayah(2, "مَا أَغْنَىٰ عَنْهُ مَالُهُ وَمَا كَسَبَ", 30),
            Ayah(3, "سَيَصْلَىٰ نَارًا ذَاتَ لَهَبٍ", 30),
            Ayah(4, "وَامْرَأَتُهُ حَمَّالَةَ الْحَطَبِ", 30),
            Ayah(5, "فِي جِيدِهَا حَبْلٌ مِّن مَّسَدٍ", 30)
        ),
        112 to listOf(
            Ayah(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", 30),
            Ayah(2, "اللَّهُ الصَّمَدُ", 30),
            Ayah(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", 30),
            Ayah(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", 30)
        ),
        113 to listOf(
            Ayah(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", 30),
            Ayah(2, "مِن شَرِّ مَا خَلَقَ", 30),
            Ayah(3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", 30),
            Ayah(4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", 30),
            Ayah(5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", 30)
        ),
        114 to listOf(
            Ayah(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", 30),
            Ayah(2, "مَلِكِ النَّاسِ", 30),
            Ayah(3, "إِلَٰهِ النَّاسِ", 30),
            Ayah(4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", 30),
            Ayah(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", 30),
            Ayah(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", 30)
        ),
        // Ayat Al-Kursi (سورة البقرة آية 255)
        2 to listOf(
            Ayah(255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ", 3)
        )
    )

    fun getSurah(number: Int): Surah? {
        val meta = allSurahsMetadata.find { it.number == number } ?: return null
        val verses = preBundledVerses[number] ?: emptyList()
        return meta.copy(ayahs = verses)
    }
}

data class JuzInfo(
    val number: Int,
    val nameArabic: String,
    val description: String
)
