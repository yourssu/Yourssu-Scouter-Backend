package com.yourssu.scouter.ats.business.support.utils

/**
 * 출생연도/나이 응답을 "XX년생" 형태로 통일.
 * 예: "00년생", "02", "2003년생", "2000" -> "00년생", "02년생", "03년생", "00년생"
 * 전각 숫자(０１２３)도 반각으로 변환 후 파싱.
 */
object AgeNormalizer {

    private val digitsRegex = Regex("\\d+")

    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val normalized = raw.trim().replaceFullWidthDigits()
        val digits = digitsRegex.find(normalized)?.value ?: return ""
        val twoDigits = when {
            digits.length >= 4 -> digits.takeLast(2)
            digits.length == 2 -> digits
            digits.length == 1 -> "0$digits"
            else -> digits.takeLast(2)
        }
        return "${twoDigits}년생"
    }

    private fun String.replaceFullWidthDigits(): String =
        map { c ->
            if (c in '\uFF10'..'\uFF19') (c.code - 0xFF10 + 0x30).toChar() else c
        }.joinToString("")
}
