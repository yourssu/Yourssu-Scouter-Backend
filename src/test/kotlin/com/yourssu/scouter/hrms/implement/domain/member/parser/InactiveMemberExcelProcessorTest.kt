package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.domain.member.SemesterPeriod
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.argThat
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@DisplayName("InactiveMemberExcelProcessor")
class InactiveMemberExcelProcessorTest {

    private lateinit var workbook: XSSFWorkbook
    private lateinit var basicMemberExcelProcessor: BasicMemberExcelProcessor
    private lateinit var memberReader: MemberReader
    private lateinit var memberWriter: MemberWriter
    private lateinit var semesterRepository: SemesterRepository
    private lateinit var processor: InactiveMemberExcelProcessor

    private val department = Department(id = 1L, collegeId = 1L, name = "컴퓨터학부")
    private val part = PartFixtureBuilder().id(1L).name("Backend").build()

    @BeforeEach
    fun setUp() {
        workbook = XSSFWorkbook()
        basicMemberExcelProcessor = mock()
        memberReader = mock()
        memberWriter = mock()
        semesterRepository = mock()
        processor = InactiveMemberExcelProcessor(
            basicMemberExcelProcessor = basicMemberExcelProcessor,
            memberReader = memberReader,
            memberWriter = memberWriter,
            semesterRepository = semesterRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        workbook.close()
    }

    private fun createSheetWithHeader(): org.apache.poi.ss.usermodel.Sheet {
        val sheet = workbook.createSheet("비액티브")
        val headerRow = sheet.createRow(0)
        listOf(
            "파트",
            "이름",
            "닉네임",
            "발음",
            "이메일",
            "연락처",
            "전공",
            "생년월일",
            "학번",
            "가입일",
            "사유",
            "활동 학기",
            "예정복귀 시기",
            "문자회신여부",
            "문자회신 희망시기",
            "비고",
        ).forEachIndexed { i, v ->
            headerRow.createCell(i).setCellValue(v)
        }
        return sheet
    }

    private fun addDataRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        name: String = "홍길동",
        studentId: String = "20219999",
        reason: String? = null,
        activitySemester: String? = null,
        expectedReturn: String? = null,
        smsReplied: String? = null,
        smsReplyDesiredPeriod: String? = null,
        note: String? = null,
    ) {
        val rowIndex = sheet.lastRowNum + 1
        val row = sheet.createRow(rowIndex)
        row.createCell(0).setCellValue("Backend")
        row.createCell(1).setCellValue(name)
        row.createCell(2).setCellValue("Nick")
        row.createCell(3).setCellValue("닉")
        row.createCell(4).setCellValue("test@yourssu.com")
        row.createCell(5).setCellValue("010-1234-5678")
        row.createCell(6).setCellValue("컴퓨터학부")
        row.createCell(7).setCellValue("2000.01.01")
        row.createCell(8).setCellValue(studentId)
        row.createCell(9).setCellValue("24.03.01")
        row.createCell(10).setCellValue(reason ?: "")
        row.createCell(11).setCellValue(activitySemester ?: "")
        row.createCell(12).setCellValue(expectedReturn ?: "")
        row.createCell(13).setCellValue(smsReplied ?: "")
        row.createCell(14).setCellValue(smsReplyDesiredPeriod ?: "")
        row.createCell(15).setCellValue(note ?: "")
    }

    @Nested
    @DisplayName("parse - 10~14열 파싱 검증")
    inner class ParseInactiveExtraColumns {

        @Test
        fun `사유·예정복귀·문자회신여부·문자회신희망시기가 10~14열에서 파싱되어 writeMemberWithInactiveState에 전달된다`() {
            val sheet = createSheetWithHeader()
            addDataRow(
                sheet,
                reason = "활동을 오래하기도 했고, 막학기여서 전환하려고 합니다",
                activitySemester = "2025-1",
                expectedReturn = "2026-1",
                smsReplied = "o",
                smsReplyDesiredPeriod = "26년 2월 복귀 희망",
            )
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)
            val parsedMember = MemberFixtureBuilder().name("홍길동").studentId("20219999").build()
            whenever(basicMemberExcelProcessor.rowToMember(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(parsedMember)
            whenever(memberReader.readByStudentIdOrNull("20219999")).thenReturn(null)
            whenever(semesterRepository.find(any<Semester>())).thenReturn(Semester(2026, 1))

            val result = processor.parse(sheet, departments, parts, emptyMap())

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter).writeMemberWithInactiveState(
                any(),
                any(),
                eq("활동을 오래하기도 했고, 막학기여서 전환하려고 합니다"),
                any(),
                eq("2026-1"),
                eq(true),
                eq("26년 2월 복귀 희망"),
            )
        }

        @Test
        fun `예정복귀 시기가 비학기 문구이면 reason에 예정복귀 메모로 붙이고 expectedReturnSemesterStr는 null로 전달한다`() {
            val sheet = createSheetWithHeader()
            addDataRow(
                sheet,
                reason = "활동을 오래하기도 했고, 막학기여서 전환하려고 합니다",
                expectedReturn = "26년 2월 졸업으로 복귀하지 않음",
                smsReplied = "x",
            )
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)
            val parsedMember = MemberFixtureBuilder().name("홍길동").studentId("20219999").build()
            whenever(basicMemberExcelProcessor.rowToMember(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(parsedMember)
            whenever(memberReader.readByStudentIdOrNull("20219999")).thenReturn(null)

            val result = processor.parse(sheet, departments, parts, emptyMap())

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter).writeMemberWithInactiveState(
                any(),
                any(),
                argThat { obj: Any? -> obj is String && obj.contains("활동을 오래하기도 했고") && obj.contains("예정복귀: 26년 2월 졸업으로 복귀하지 않음") },
                anyOrNull(),
                isNull(),
                anyOrNull(),
                anyOrNull(),
            )
        }

        @Test
        fun `기존 비액티브 멤버 수정 시 10~14열이 반영되어 update 호출된다`() {
            val sheet = createSheetWithHeader()
            addDataRow(
                sheet,
                studentId = "20219999",
                reason = "사유 텍스트",
                expectedReturn = "2026-1",
                smsReplied = "예",
                smsReplyDesiredPeriod = "26-2 복귀",
            )
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)
            val existingMember = MemberFixtureBuilder().name("홍길동").studentId("20219999").build().apply {
                state = MemberState.INACTIVE
            }
            val semester2026_1 = Semester(2026, 1)
            val semester2025_2 = Semester(2025, 2)
            val currentInactive = InactiveMember(
                id = 1L,
                member = existingMember,
                activePeriod = SemesterPeriod(semester2025_2, semester2025_2),
                expectedReturnSemester = semester2026_1,
                inactivePeriod = SemesterPeriod(semester2025_2, semester2025_2),
                reason = null,
                smsReplied = null,
                smsReplyDesiredPeriod = null,
            )
            whenever(basicMemberExcelProcessor.rowToMember(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(existingMember)
            whenever(basicMemberExcelProcessor.mergeForPatch(any(), any())).thenReturn(existingMember)
            whenever(memberReader.readByStudentIdOrNull("20219999")).thenReturn(existingMember)
            whenever(memberReader.readInactiveByMemberId(1L)).thenReturn(currentInactive)
            whenever(semesterRepository.find(any<Semester>())).thenReturn(semester2026_1)

            val result = processor.parse(sheet, departments, parts, emptyMap())

            assertThat(result.hasErrors()).isFalse()
            val updateCaptor = argumentCaptor<InactiveMember>()
            verify(memberWriter).update(updateCaptor.capture())
            assertThat(updateCaptor.firstValue.reason).isEqualTo("사유 텍스트")
            assertThat(updateCaptor.firstValue.smsReplied).isTrue()
            assertThat(updateCaptor.firstValue.smsReplyDesiredPeriod).isEqualTo("26-2 복귀")
        }
    }
}
