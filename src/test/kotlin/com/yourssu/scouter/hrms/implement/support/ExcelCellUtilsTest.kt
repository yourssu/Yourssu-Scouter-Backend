package com.yourssu.scouter.hrms.implement.support

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("ExcelCellUtils")
class ExcelCellUtilsTest {

    private val workbook = XSSFWorkbook()

    @AfterEach
    fun tearDown() {
        workbook.close()
    }

    @Nested
    @DisplayName("getStringSafe")
    inner class GetStringSafe {

        @Test
        fun `null 셀은 default 반환`() {
            assertThat(null.getStringSafe()).isEqualTo("")
            assertThat(null.getStringSafe("default")).isEqualTo("default")
        }

        @Test
        fun `STRING 셀은 trim된 문자열 반환`() {
            val sheet = workbook.createSheet("s1")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply {
                setCellValue("  hello  ")
            }
            assertThat(cell.getStringSafe()).isEqualTo("hello")
        }

        @Test
        fun `BLANK 셀은 default 반환`() {
            val sheet = workbook.createSheet("s2")
            val row = sheet.createRow(0)
            val cell = row.createCell(0)
            assertThat(cell.getStringSafe()).isEqualTo("")
        }
    }

    @Nested
    @DisplayName("getLocalDateSafe")
    inner class GetLocalDateSafe {

        @Test
        fun `null 셀은 default 반환`() {
            val default = LocalDate.of(2020, 1, 1)
            assertThat(null.getLocalDateSafe()).isNull()
            assertThat(null.getLocalDateSafe(default)).isEqualTo(default)
        }

        @Test
        fun `yyyy_MM_dd 문자열은 파싱된다`() {
            val sheet = workbook.createSheet("s3")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply {
                setCellValue("2002.03.07")
            }
            assertThat(cell.getLocalDateSafe()).isEqualTo(LocalDate.of(2002, 3, 7))
        }

        @Test
        fun `yyyy 하이픈 MM_dd 문자열은 파싱된다`() {
            val sheet = workbook.createSheet("s4")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply {
                setCellValue("2025-09-01")
            }
            assertThat(cell.getLocalDateSafe()).isEqualTo(LocalDate.of(2025, 9, 1))
        }

        @Test
        fun `파싱 불가 문자열은 default 반환`() {
            val sheet = workbook.createSheet("s5")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply {
                setCellValue("not-a-date")
            }
            assertThat(cell.getLocalDateSafe()).isNull()
        }
    }

    @Nested
    @DisplayName("isNullOrBlank")
    inner class IsNullOrBlank {

        @Test
        fun `null은 true`() {
            assertThat(null.isNullOrBlank()).isTrue()
        }

        @Test
        fun `빈 문자열 셀은 true`() {
            val sheet = workbook.createSheet("s6")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("") }
            assertThat(cell.isNullOrBlank()).isTrue()
        }

        @Test
        fun `공백만 있으면 true`() {
            val sheet = workbook.createSheet("s7")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("   ") }
            assertThat(cell.isNullOrBlank()).isTrue()
        }

        @Test
        fun `문자 있으면 false`() {
            val sheet = workbook.createSheet("s8")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("홍길동") }
            assertThat(cell.isNullOrBlank()).isFalse()
        }
    }

    @Nested
    @DisplayName("isStrikethrough")
    inner class IsStrikethrough {

        @Test
        fun `null 셀은 false`() {
            assertThat(null.isStrikethrough()).isFalse()
        }

        @Test
        fun `스타일 없이 만든 셀은 false`() {
            val sheet = workbook.createSheet("s9")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("test") }
            assertThat(cell.isStrikethrough()).isFalse()
        }

        @Test
        fun `취소선 폰트가 적용된 셀은 true`() {
            val sheet = workbook.createSheet("s10")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("취소됨") }

            val font = workbook.createFont().apply { strikeout = true }
            val style = workbook.createCellStyle().apply { setFont(font) }
            cell.cellStyle = style

            assertThat(cell.isStrikethrough()).isTrue()
        }

        @Test
        fun `취소선 없는 폰트는 false`() {
            val sheet = workbook.createSheet("s11")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("정상") }

            val font = workbook.createFont().apply { strikeout = false }
            val style = workbook.createCellStyle().apply { setFont(font) }
            cell.cellStyle = style

            assertThat(cell.isStrikethrough()).isFalse()
        }
    }
}
