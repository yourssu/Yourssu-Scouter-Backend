package com.yourssu.scouter.hrms.implement.support

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType

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
                    defaultValue
                }
            }
        }

        else -> defaultValue
    }
}

fun Cell?.isNullOrBlank(): Boolean {
    return this == null || this.cellType == CellType.BLANK || this.stringCellValue.isBlank()
}

