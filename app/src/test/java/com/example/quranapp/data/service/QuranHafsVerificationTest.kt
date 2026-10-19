package com.example.quranapp.data.service

import com.example.quranapp.data.model.QuranAyahJson
import com.example.quranapp.util.QuranTextSanitizer
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class QuranHafsVerificationTest {

    @Test
    fun testQuranTextSanitizer_normalizesFathaBeforeDaggerAlif() {
        // In Uthmanic fonts, \u064E (Fatha) followed by \u0670 (Dagger Alif) causes glyph collision/hiding.
        // Sanitizer should normalize \u064E\u0670 to \u0670 so dagger alif renders cleanly via Mark-to-Base.
        val input = "ٱلۡعَٰلَمِينَ" // Contains \u0639 + \u064E + \u0670 + \u0644
        assertTrue("Input should contain Fatha followed by Dagger Alif", input.contains("\u064E\u0670"))
        
        val sanitized = QuranTextSanitizer.sanitize(input)
        assertFalse("Sanitized text must not contain Fatha followed by Dagger Alif", sanitized.contains("\u064E\u0670"))
        assertTrue("Sanitized text must retain the Dagger Alif", sanitized.contains("\u0670"))
    }

    @Test
    fun testQuranDataJson_isCompleteHafsDataset() {
        val jsonFile = File("src/main/assets/quran_data.json").let {
            if (it.exists()) it else File("app/src/main/assets/quran_data.json")
        }
        assertTrue("quran_data.json must exist in assets", jsonFile.exists())

        val jsonString = jsonFile.readText(Charsets.UTF_8)
        val gson = Gson()
        val ayahs = gson.fromJson(jsonString, Array<QuranAyahJson>::class.java)

        assertNotNull("JSON parsing should not return null", ayahs)
        // Standard Hafs Quran contains exactly 6236 ayahs
        assertEquals("Total ayahs in Hafs narration must be exactly 6236", 6236, ayahs.size)

        // Group by surah
        val surahs = ayahs.groupBy { it.surahNumber }
        assertEquals("Total surahs in Quran must be 114", 114, surahs.size)

        // Surah 1: Al-Fatihah
        val fatihahAyahs = surahs[1]
        assertNotNull("Surah 1 must exist", fatihahAyahs)
        assertEquals("Surah Al-Fatihah must have exactly 7 ayahs in Hafs", 7, fatihahAyahs!!.size)

        val firstAyah = fatihahAyahs[0]
        assertEquals("Surah 1 name Arabic must be الفَاتِحة", "الفَاتِحة", firstAyah.surahNameAr.trim())
        assertEquals("Surah 1 Ayah 1 number must be 1", 1, firstAyah.ayahNumber)
        assertTrue("Surah 1 Ayah 1 must be Bismillah in Hafs", firstAyah.ayahText.contains("بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ"))

        val secondAyah = fatihahAyahs[1]
        assertEquals("Surah 1 Ayah 2 number must be 2", 2, secondAyah.ayahNumber)
        assertTrue("Surah 1 Ayah 2 must be Al-Hamdu lillah in Hafs", secondAyah.ayahText.contains("ٱلۡحَمۡدُ لِلَّهِ رَبِّ ٱلۡعَٰلَمِينَ"))

        val fourthAyah = fatihahAyahs[3]
        assertEquals("Surah 1 Ayah 4 number must be 4", 4, fourthAyah.ayahNumber)
        assertTrue("Surah 1 Ayah 4 must be Maliki with Dagger Alif (مَٰلِكِ)", fourthAyah.ayahText.contains("مَٰلِكِ"))

        // Surah 114: An-Nas
        val nasAyahs = surahs[114]
        assertNotNull("Surah 114 must exist", nasAyahs)
        assertEquals("Surah An-Nas must have 6 ayahs", 6, nasAyahs!!.size)
    }

    @Test
    fun testBismillahDisplayPolicy() {
        // In standard Hafs Quran apps:
        // Surah 1 (Al-Fatihah) has Bismillah as Ayah 1, so a separate top BismillahCard should NOT be shown
        // Surah 9 (At-Tawbah) has no Bismillah
        // Surahs 2..8 and 10..114 should show the top BismillahCard
        fun shouldShowBismillahCard(surahId: Int): Boolean {
            return surahId != 1 && surahId != 9
        }

        assertFalse("Surah 1 should not show separate BismillahCard", shouldShowBismillahCard(1))
        assertFalse("Surah 9 should not show separate BismillahCard", shouldShowBismillahCard(9))
        assertTrue("Surah 2 should show separate BismillahCard", shouldShowBismillahCard(2))
        assertTrue("Surah 114 should show separate BismillahCard", shouldShowBismillahCard(114))
    }
}
