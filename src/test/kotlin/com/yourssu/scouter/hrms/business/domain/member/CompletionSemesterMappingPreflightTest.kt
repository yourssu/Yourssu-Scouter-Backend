package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.sql.Date
import java.time.LocalDate

@DisplayName("CompletionSemesterMappingPreflight")
class CompletionSemesterMappingPreflightTest {

    private val workbook = XSSFWorkbook()

    @AfterEach
    fun tearDown() {
        workbook.close()
    }

    @Test
    fun `같은 rawKey 는 한 힌트로 묶이고 이름 닉네임이 누적된다`() {
        val sheet = workbook.createSheet("수료")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("팀")
        header.createCell(1).setCellValue("파트")
        header.createCell(2).setCellValue("이름")
        header.createCell(3).setCellValue("닉")

        fun addRow(r: Int, name: String, nick: String, col11: String) {
            val row = sheet.createRow(r)
            row.createCell(0).setCellValue("t")
            row.createCell(1).setCellValue("p")
            row.createCell(2).setCellValue(name)
            row.createCell(3).setCellValue(nick)
            row.createCell(11).setCellValue(col11)
        }

        addRow(1, "홍길동", "Roro", "애매한값")
        addRow(2, "김철수", "철", "애매한값")
        addRow(3, "이영희", "YH", "25-1")

        val hints = CompletionSemesterMappingPreflight.collectHints(
            sheet,
            resolveLabelToStoredSemester = { raw ->
                if (raw == "25-1") Semester(2025, 1) else null
            },
        )

        assertThat(hints).hasSize(1)
        assertThat(hints[0].rawKey).isEqualTo("애매한값")
        assertThat(hints[0].memberLabels).hasSize(2)
        assertThat(hints[0].memberLabels[0].name).isEqualTo("홍길동")
        assertThat(hints[0].memberLabels[0].nickname).isEqualTo("Roro")
        assertThat(hints[0].memberLabels[1].name).isEqualTo("김철수")
    }

    @Test
    fun `해석되는 학기 문자열 행은 힌트에서 제외된다`() {
        val sheet = workbook.createSheet("수료2")
        sheet.createRow(0).createCell(0).setCellValue("h")
        val row = sheet.createRow(1)
        row.createCell(0).setCellValue("t")
        row.createCell(2).setCellValue("나")
        row.createCell(3).setCellValue("닉")
        row.createCell(11).setCellValue("25-1")

        val hints = CompletionSemesterMappingPreflight.collectHints(
            sheet,
            resolveLabelToStoredSemester = { raw ->
                if (raw == "25-1") Semester(2025, 1) else null
            },
        )

        assertThat(hints).isEmpty()
    }

    @Test
    fun `11열이 날짜_시리얼_숫자_서식이어도_표시_문자열_기준으로_묶인다`() {
        val sheet = workbook.createSheet("수료3")
        sheet.createRow(0).createCell(0).setCellValue("h")
        val row = sheet.createRow(1)
        row.createCell(0).setCellValue("t")
        row.createCell(2).setCellValue("박날짜")
        row.createCell(3).setCellValue("D")
        val c11 = row.createCell(11)
        c11.cellStyle = workbook.createCellStyle().apply {
            dataFormat = workbook.creationHelper.createDataFormat().getFormat("yyyy.m.d")
        }
        c11.setCellValue(Date.valueOf(LocalDate.of(2026, 1, 25)))

        val hints = CompletionSemesterMappingPreflight.collectHints(sheet, resolveLabelToStoredSemester = { null })

        assertThat(hints).hasSize(1)
        assertThat(hints[0].rawKey).isEqualTo("2026.1.25")
        assertThat(hints[0].memberLabels.single().name).isEqualTo("박날짜")
    }
}
