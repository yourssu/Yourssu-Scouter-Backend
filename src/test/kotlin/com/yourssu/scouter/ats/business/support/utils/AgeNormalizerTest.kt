package com.yourssu.scouter.ats.business.support.utils

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AgeNormalizerTest {

    @Test
    @DisplayName("00년생 -> 00년생")
    fun alreadyTwoDigitsWithSuffix() {
        assertEquals("00년생", AgeNormalizer.normalize("00년생"))
    }

    @Test
    @DisplayName("02 -> 02년생")
    fun twoDigitsOnly() {
        assertEquals("02년생", AgeNormalizer.normalize("02"))
    }

    @Test
    @DisplayName("2003년생 -> 03년생")
    fun fourDigitsWithSuffix() {
        assertEquals("03년생", AgeNormalizer.normalize("2003년생"))
    }

    @Test
    @DisplayName("2000 -> 00년생")
    fun fourDigitsOnly() {
        assertEquals("00년생", AgeNormalizer.normalize("2000"))
    }

    @Test
    @DisplayName("3 -> 03년생")
    fun singleDigitPadded() {
        assertEquals("03년생", AgeNormalizer.normalize("3"))
    }

    @Test
    @DisplayName("null/blank -> 빈 문자열")
    fun nullOrBlank() {
        assertEquals("", AgeNormalizer.normalize(null))
        assertEquals("", AgeNormalizer.normalize(""))
        assertEquals("", AgeNormalizer.normalize("   "))
    }

    @Test
    @DisplayName("숫자 없음 -> 빈 문자열")
    fun noDigits() {
        assertEquals("", AgeNormalizer.normalize("년생"))
        assertEquals("", AgeNormalizer.normalize("abc"))
    }

    @Test
    @DisplayName("전각 숫자 ０３ -> 03년생")
    fun fullWidthDigits() {
        assertEquals("03년생", AgeNormalizer.normalize("０３"))
        assertEquals("03년생", AgeNormalizer.normalize("２００３년생"))
    }
}
