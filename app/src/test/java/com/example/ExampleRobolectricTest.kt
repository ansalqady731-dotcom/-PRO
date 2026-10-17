package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("رفيق المسلم PRO", appName)
  }

  @Test
  fun `verify quran repository integrity 114 surahs and 604 pages`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val surahs = com.example.data.quran.QuranRepository.getAllSurahs(context)
    assertEquals(114, surahs.size)

    val totalAyahs = surahs.sumOf { it.ayahs.size }
    assertEquals(6236, totalAyahs)

    val page1Ayahs = com.example.data.quran.QuranRepository.getAyahsByPage(context, 1)
    org.junit.Assert.assertTrue("Page 1 should have ayahs", page1Ayahs.isNotEmpty())
    assertEquals(1, page1Ayahs.first().surahNumber)

    val page604Ayahs = com.example.data.quran.QuranRepository.getAyahsByPage(context, 604)
    org.junit.Assert.assertTrue("Page 604 should have ayahs", page604Ayahs.isNotEmpty())

    val tafsir = com.example.data.quran.QuranRepository.getTafsir(context, 1, 1)
    org.junit.Assert.assertNotNull("Tafsir for 1:1 should exist", tafsir)
    org.junit.Assert.assertTrue("Tafsir text should not be empty", tafsir.isNotBlank())
  }
}
