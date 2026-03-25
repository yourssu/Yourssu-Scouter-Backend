package com.yourssu.scouter.hrms.implement.support

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.abs
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Font

/**
 * 엑셀에 보이는 값에 가깝게 문자열로 읽는다.
 *
 * - **텍스트 셀**(STRING, 또는 수식 결과가 문자열): DataFormatter 결과만 쓰고, 엑셀 날짜 시리얼로 해석하지 않는다.
 *   (`'46047` 처럼 텍스트로 넣은 값은 그대로 `"46047"` 유지)
 * - **숫자 셀**(NUMERIC): [DateUtil.isCellDateFormatted]이면 **날짜 서식**으로 본다.
 *   DataFormatter가 여전히 `46047.0`처럼 숫자만 주면 [EXCEL_SERIAL_AS_DISPLAY_DATE]로 바꾼다.
 * - **날짜 서식이 아닌 숫자**인데 표시가 숫자만이고 유효 시리얼이면(일반 서식에 날짜가 박힌 경우) 날짜 문자열로 보정한다.
 *
 * [getStringSafe]는 NUMERIC을 toLong 문자열로만 바꿔 날짜 셀이 "46047"처럼 나오므로, 매핑 UI·raw 수집에는 이 함수를 쓴다.
 */
private val excelDisplayFormatter: ThreadLocal<DataFormatter> =
    ThreadLocal.withInitial { DataFormatter(Locale.getDefault()) }

/** 날짜 시리얼을 사람이 읽는 연.월.일로 통일 (시트에 날짜로 쓴 셀과 혼용 시 매핑 키 안정화). */
private val EXCEL_SERIAL_AS_DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.M.d")

/** 시리얼이 실제 날짜일 때만 재해석 (1.0 같은 플래그·5자리 학번 등과 구분). */
private const val EXCEL_DATE_SERIAL_MIN = 30000.0

private const val EXCEL_DATE_SERIAL_MAX = 100_000.0

/** DataFormatter 결과가 "숫자만(+소수)"인지 — 날짜 표기(2026.1.25)는 제외. */
private val FORMATTED_PLAIN_NUMBER: Regex = Regex("^[,\\d]+(\\.\\d+)?$")

fun Cell?.getFormattedStringSafe(defaultValue: String = ""): String {
    if (this == null) {
        return defaultValue
    }
    if (this.cellType == CellType.BLANK) {
        return defaultValue
    }
    val formatter = excelDisplayFormatter.get()
    return when (this.cellType) {
        CellType.STRING -> formatter.formatCellValue(this).trim()
        CellType.BOOLEAN -> formatter.formatCellValue(this).trim()
        CellType.NUMERIC -> formatNumericCellForDisplay(this, formatter)
        CellType.FORMULA -> when (this.cachedFormulaResultType) {
            CellType.STRING -> formatter.formatCellValue(this).trim()
            CellType.NUMERIC -> formatNumericCellForDisplay(this, formatter)
            CellType.BOOLEAN -> formatter.formatCellValue(this).trim()
            else -> formatter.formatCellValue(this).trim()
        }
        else -> formatter.formatCellValue(this).trim()
    }
}

/**
 * NUMERIC(또는 수식 결과 숫자) 셀만: 날짜 서식 vs 일반 숫자를 구분한다.
 */
private fun formatNumericCellForDisplay(cell: Cell, formatter: DataFormatter): String {
    val serial = cell.numericCellValue
    val formatted = formatter.formatCellValue(cell).trim()
    val plainNumericDisplay = formatted.replace(",", "").trim().matches(FORMATTED_PLAIN_NUMBER)

    if (DateUtil.isCellDateFormatted(cell) && DateUtil.isValidExcelDate(serial)) {
        if (plainNumericDisplay) {
            return formatExcelSerialAsDisplayDate(serial)
        }
        return formatted
    }

    if (shouldRewriteSerialAsDisplayDate(formatted, serial)) {
        return formatExcelSerialAsDisplayDate(serial)
    }
    return formatted
}

internal fun shouldRewriteSerialAsDisplayDate(formatted: String, serial: Double): Boolean {
    if (!DateUtil.isValidExcelDate(serial)) {
        return false
    }
    if (serial < EXCEL_DATE_SERIAL_MIN || serial > EXCEL_DATE_SERIAL_MAX) {
        return false
    }
    val norm = formatted.replace(",", "").trim()
    if (!norm.matches(FORMATTED_PLAIN_NUMBER)) {
        return false
    }
    val parsed = norm.toDoubleOrNull() ?: return false
    return abs(parsed - serial) < 1e-6
}

internal fun formatExcelSerialAsDisplayDate(serial: Double): String {
    val date = DateUtil.getLocalDateTime(serial).toLocalDate()
    return EXCEL_SERIAL_AS_DISPLAY_DATE.format(date)
}

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

        CellType.STRING -> parseLocalDateFromPlainText(this.stringCellValue) ?: defaultValue

        CellType.FORMULA -> when (this.cachedFormulaResultType) {
            CellType.NUMERIC -> {
                try {
                    this.localDateTimeCellValue.toLocalDate()
                } catch (e: Exception) {
                    defaultValue
                }
            }
            CellType.STRING -> parseLocalDateFromPlainText(this.stringCellValue) ?: defaultValue
            else -> defaultValue
        }

        else -> defaultValue
    }
}

/**
 * 생년월일·가입일·탈퇴일 등: [getLocalDateSafe](null) 후, 실패 시 [getFormattedStringSafe] 문자열을 날짜로 파싱.
 * 날짜 서식/일반 숫자 시리얼/텍스트 날짜가 섞인 인포시트 시트에 맞춘다.
 */
fun Cell?.getFlexibleLocalDateSafe(defaultValue: LocalDate? = null): LocalDate? {
    if (this == null) {
        return defaultValue
    }
    if (this.cellType == CellType.BLANK) {
        return defaultValue
    }
    getLocalDateSafe(null)?.let { return it }
    val text = getFormattedStringSafe("").trim()
    if (text.isBlank()) {
        return defaultValue
    }
    return parseLocalDateFromPlainText(text) ?: defaultValue
}

/** 엑셀 문자열/수식 결과 문자열을 생년월일·가입일 등에 쓰는 날짜로 파싱. */
private fun parseLocalDateFromPlainText(raw: String): LocalDate? {
    val text = raw.trim()
    if (text.isBlank()) {
        return null
    }
    parseFlexibleDate(text)?.let { return it }
    val formats = listOf(
        "yy.MM.dd.",
        "yy.MM.dd",
        "yyyy.MM.dd",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
    ).map { DateTimeFormatter.ofPattern(it) }
    return formats.firstNotNullOfOrNull { formatter ->
        try {
            LocalDate.parse(text, formatter)
        } catch (e: DateTimeParseException) {
            null
        }
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


