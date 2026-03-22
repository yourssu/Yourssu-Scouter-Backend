package com.yourssu.scouter.hrms.implement.support

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.Date
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
    @DisplayName("getFormattedStringSafe")
    inner class GetFormattedStringSafe {

        @Test
        fun `null과 BLANK는 default 반환`() {
            assertThat(null.getFormattedStringSafe()).isEqualTo("")
            val sheet = workbook.createSheet("fmtBlank")
            val row = sheet.createRow(0)
            val cell = row.createCell(0)
            assertThat(cell.getFormattedStringSafe()).isEqualTo("")
        }

        @Test
        fun `날짜로 저장된 NUMERIC 셀은 시리얼 숫자 문자열이 아니라 표시용 문자열`() {
            val sheet = workbook.createSheet("fmtDate")
            val row = sheet.createRow(0)
            val dateStyle = workbook.createCellStyle().apply {
                dataFormat = workbook.creationHelper.createDataFormat().getFormat("yyyy.m.d")
            }
            val cell = row.createCell(0).apply {
                cellStyle = dateStyle
                setCellValue(Date.valueOf(LocalDate.of(2026, 1, 25)))
            }
            assertThat(cell.cellType).isEqualTo(CellType.NUMERIC)
            assertThat(cell.getStringSafe()).matches("\\d+")
            val display = cell.getFormattedStringSafe()
            assertThat(display).isNotEqualTo(cell.getStringSafe())
            assertThat(display).contains("2026")
        }

        @Test
        fun `STRING 셀은 getStringSafe와 동일하게 trim`() {
            val sheet = workbook.createSheet("fmtStr")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("  25-1  ") }
            assertThat(cell.getFormattedStringSafe()).isEqualTo("25-1")
        }

        @Test
        fun `STRING으로_적은_숫자만_같은_문자열은_날짜_시리얼로_바꾸지_않는다`() {
            val sheet = workbook.createSheet("fmtTextLooksSerial")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("46047") }
            assertThat(cell.cellType).isEqualTo(CellType.STRING)
            assertThat(cell.getFormattedStringSafe()).isEqualTo("46047")
        }

        @Test
        fun `일반_숫자_서식_날짜_시리얼은_46047_0_같은_표기가_아니라_yyyy_M_d`() {
            val sheet = workbook.createSheet("fmtSerialPlain")
            val row = sheet.createRow(0)
            val serial = DateUtil.getExcelDate(LocalDate.of(2026, 1, 25), false)
            val cell = row.createCell(0).apply {
                cellStyle = workbook.createCellStyle()
                setCellValue(serial)
            }
            assertThat(DateUtil.isCellDateFormatted(cell)).isFalse()
            val display = cell.getFormattedStringSafe()
            assertThat(display).doesNotContain("46047")
            assertThat(display).isEqualTo("2026.1.25")
        }

        @Test
        fun `shouldRewrite는_소수_없는_시리얼_문자열도_인식`() {
            assertThat(shouldRewriteSerialAsDisplayDate("46047", 46047.0)).isTrue()
        }

        @Test
        fun `shouldRewrite는_46047_0_형태를_인식`() {
            assertThat(shouldRewriteSerialAsDisplayDate("46047.0", 46047.0)).isTrue()
        }

        @Test
        fun `shouldRewrite는_천단위_콤마가_있어도_인식`() {
            assertThat(shouldRewriteSerialAsDisplayDate("46,047.0", 46047.0)).isTrue()
        }

        @Test
        fun `shouldRewrite는_날짜가_아닌_작은_숫자는_거부`() {
            assertThat(shouldRewriteSerialAsDisplayDate("1.0", 1.0)).isFalse()
        }
    }

    @Nested
    @DisplayName("getFlexibleLocalDateSafe")
    inner class GetFlexibleLocalDateSafe {

        @Test
        fun `일반_숫자_서식_날짜_시리얼은_표시문자열_경유로_LocalDate`() {
            val sheet = workbook.createSheet("flexSerial")
            val row = sheet.createRow(0)
            val serial = DateUtil.getExcelDate(LocalDate.of(2026, 3, 10), false)
            val cell = row.createCell(0).apply {
                cellStyle = workbook.createCellStyle()
                setCellValue(serial)
            }
            assertThat(cell.getFlexibleLocalDateSafe(null)).isEqualTo(LocalDate.of(2026, 3, 10))
        }

        @Test
        fun `문자열_날짜는_getLocalDate와_동일하게_파싱`() {
            val sheet = workbook.createSheet("flexStrDate")
            val row = sheet.createRow(0)
            val cell = row.createCell(0).apply { setCellValue("2002.03.07") }
            assertThat(cell.getFlexibleLocalDateSafe(null)).isEqualTo(LocalDate.of(2002, 3, 7))
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
