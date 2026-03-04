package com.yourssu.scouter.hrms.implement.support

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Font

fun Cell?.getStringSafe(defaultValue: String = ""): String {
    return when (this?.cellType) {
        CellType.STRING -> this.stringCellValue.trim()
        CellType.NUMERIC -> this.numericCellValue.toLong().toString()
        CellType.BOOLEAN -> this.booleanCellValue.toString()
        CellType.FORMULA -> when (this.cachedFormulaResultType) {
            CellType.STRING -> this.stringCellValue.trim()
            CellType.NUMERIC -> this.numericCellValue.toLong().toString()
            else -> defaultValue
        }
        else -> defaultValue
    }
}

fun Cell?.getLocalDateSafe(defaultValue: LocalDate? = null): LocalDate? {
    if (this == null) {
        return defaultValue
    }

    return when (this.cellType) {
        CellType.NUMERIC -> {
            try {
                this.localDateTimeCellValue.toLocalDate()
            } catch (e: Exception) {
                defaultValue
            }
        }

        CellType.STRING -> {
            val text = this.stringCellValue.trim()
            if (text.isBlank()) {
                return defaultValue
            }

            // 1차: 유연한 yyyy/yy.MM.dd, yyyy/yy.MM, yy.MM.**, 공백 포함 포맷 처리
            parseFlexibleDate(text) ?: run {
                // 2차: 기존 포맷들 그대로 시도 (역호환)
                val formats = listOf(
                    "yy.MM.dd.",
                    "yy.MM.dd",
                    "yyyy.MM.dd",
                    "yyyy-MM-dd",
                    "yyyy/MM/dd"
                ).map { DateTimeFormatter.ofPattern(it) }

                formats.firstNotNullOfOrNull { formatter ->
                    try {
                        LocalDate.parse(text, formatter)
                    } catch (e: DateTimeParseException) {
                        null
                    }
                } ?: defaultValue
            }
        }

        else -> defaultValue
    }
}

fun Cell?.isNullOrBlank(): Boolean {
    return this == null || this.cellType == CellType.BLANK || this.stringCellValue.isBlank()
}

fun Cell?.isStrikethrough(): Boolean {
    if (this == null) {
        return false
    }

    return runCatching {
        val style = this.cellStyle ?: return false
        val workbook = this.sheet?.workbook ?: return false
        val font: Font = workbook.getFontAt(style.fontIndexAsInt)
        font.strikeout
    }.getOrDefault(false)
}

/**
 * 다양한 날짜 문자열을 최대한 LocalDate로 변환한다.
 *
 * 지원 요구사항:
 * - 2003.7.20 / 2002. 7. 20
 * - 01.01.30 / 02.12.07 (yy.MM.dd -> 20yy-MM-dd 가정)
 * - 23.03.** / 23.09  (일자가 없거나 **이면 fallback 사용)
 */
private fun parseFlexibleDate(raw: String): LocalDate? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null

    // 공백 제거 후 마침표 기준 분리
    val cleaned = trimmed.replace(" ", "")
    val parts = cleaned.split('.').filter { it.isNotBlank() }
    if (parts.isEmpty()) return null

    // 연.월만 있는 경우나 day가 비어 있거나 **인 경우는 fallback을 쓰기 위해 여기서 null 반환
    if (parts.size < 3) return null

    val (yearPart, monthPart, dayPart) = Triple(parts[0], parts[1], parts[2])
    if (dayPart.isBlank() || dayPart == "**") return null

    val year = when (yearPart.length) {
        4 -> yearPart.toIntOrNull()
        1, 2 -> {
            // 00~99 -> 2000~2099 로 가정
            val yy = yearPart.toIntOrNull() ?: return null
            2000 + yy
        }
        else -> null
    } ?: return null

    val month = monthPart.toIntOrNull() ?: return null
    val day = dayPart.toIntOrNull() ?: return null

    return try {
        LocalDate.of(year, month, day)
    } catch (e: Exception) {
        null
    }
}


