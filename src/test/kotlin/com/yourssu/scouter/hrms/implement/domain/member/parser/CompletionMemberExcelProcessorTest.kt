package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@DisplayName("CompletionMemberExcelProcessor")
class CompletionMemberExcelProcessorTest {

    private lateinit var workbook: XSSFWorkbook
    private lateinit var memberPartRoleResolver: MemberPartRoleResolver
    private lateinit var mappingData: MemberParseMappingData
    private lateinit var basicMemberExcelProcessor: BasicMemberExcelProcessor
    private lateinit var memberReader: MemberReader
    private lateinit var memberWriter: MemberWriter
    private lateinit var processor: CompletionMemberExcelProcessor

    private val department = Department(id = 1L, collegeId = 1L, name = "컴퓨터학부")
    private val part = PartFixtureBuilder().id(1L).name("Backend").build()

    @BeforeEach
    fun setUp() {
        workbook = XSSFWorkbook()
        memberPartRoleResolver = mock()
        mappingData = mock()
        whenever(mappingData.departmentAliases).thenReturn(emptyMap())
        whenever(memberPartRoleResolver.toPartAndRoles(any(), any(), any()))
            .thenReturn(MemberPartAndRoles(setOf(MemberPartAndRole(part, com.yourssu.scouter.hrms.implement.domain.member.MemberRole.MEMBER))))

        basicMemberExcelProcessor = BasicMemberExcelProcessor(
            memberPartRoleResolver = memberPartRoleResolver,
            mappingData = mappingData,
        )
        memberReader = mock()
        memberWriter = mock()
        processor = CompletionMemberExcelProcessor(
            basicMemberExcelProcessor = basicMemberExcelProcessor,
            memberReader = memberReader,
            memberWriter = memberWriter,
        )
    }

    @AfterEach
    fun tearDown() {
        workbook.close()
    }

    private fun createSheetWithHeader(): org.apache.poi.ss.usermodel.Sheet {
        val sheet = workbook.createSheet("수료")
        val headerRow = sheet.createRow(0)
        // 0: 팀명, 1: 파트/역할, 2: 이름, 3: 닉네임, 4: 발음, 5: 이메일, 6: 연락처, 7: 전공, 8: 생년월일, 9: 학번, 10: 가입일, 11: 수료일자
        listOf(
            "팀명",
            "파트/역할",
            "이름",
            "닉네임",
            "닉네임(발음)",
            "유어슈 이메일",
            "연락처",
            "전공",
            "생년월일",
            "학번",
            "가입일",
            "수료일자",
        ).forEachIndexed { i, v ->
            headerRow.createCell(i).setCellValue(v)
        }
        return sheet
    }

    private fun addDataRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        name: String = "홍길동",
        nickname: String = "Roro",
        pronunciation: String = "로로",
        email: String = "test@yourssu.com",
        phone: String = "010-1234-5678",
        departmentName: String = "컴퓨터학부",
        birthDate: String = "2002.01.15",
        studentId: String = "20210001",
        joinDate: String = "23.03.01",
        completionDate: String = "2025-09-01",
    ) {
        val rowIndex = sheet.lastRowNum + 1
        val row = sheet.createRow(rowIndex)
        row.createCell(0).setCellValue("팀A")
        row.createCell(1).setCellValue("Backend") // 파트/역할
        row.createCell(2).setCellValue(name)
        row.createCell(3).setCellValue(nickname)
        row.createCell(4).setCellValue(pronunciation)
        row.createCell(5).setCellValue(email)
        row.createCell(6).setCellValue(phone)
        row.createCell(7).setCellValue(departmentName)
        row.createCell(8).setCellValue(birthDate)
        row.createCell(9).setCellValue(studentId)
        row.createCell(10).setCellValue(joinDate)
        row.createCell(11).setCellValue(completionDate)
    }

    @Nested
    @DisplayName("parse - 신규 COMPLETED 멤버")
    inner class ParseNewCompletedMember {

        @Test
        fun `DB에 학번이 없으면 수료일자로 writeMemberWithGraduatedState 호출`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, studentId = "20219999")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            whenever(memberReader.readByStudentIdOrNull("20219999")).thenReturn(null)

            val result = processor.parse(sheet, departments, parts, emptyMap())

            assertThat(result.hasErrors()).isFalse()
            val captor = argumentCaptor<Member>()
            val dateCaptor = argumentCaptor<LocalDate>()
            verify(memberWriter).writeMemberWithGraduatedState(captor.capture(), dateCaptor.capture())
            assertThat(captor.firstValue.state).isEqualTo(MemberState.COMPLETED)
            assertThat(dateCaptor.firstValue).isEqualTo(LocalDate.of(2025, 9, 1))
        }

        @Test
        fun `수료일자가 비어 있거나 파싱 불가면 fallback 수료일자가 사용된다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, completionDate = "not-a-date")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, emptyMap())

            assertThat(result.hasErrors()).isFalse()
            val captor = argumentCaptor<Member>()
            val dateCaptor = argumentCaptor<LocalDate>()
            verify(memberWriter).writeMemberWithGraduatedState(captor.capture(), dateCaptor.capture())
            assertThat(dateCaptor.firstValue).isEqualTo(LocalDate.of(2099, 12, 31))
        }
    }

    @Nested
    @DisplayName("parse - 기존 멤버 COMPLETED 전환")
    inner class ParseExistingMember {

        @Test
        fun `기존 ACTIVE 멤버면 상태를 COMPLETED로 바꾸고 졸업 엔티티를 갱신한다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "김철수", studentId = "20210001")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val existingMember = MemberFixtureBuilder()
                .name("김철수")
                .studentId("20210001")
                .email("existing@test.com")
                .build()
            whenever(memberReader.readByStudentIdOrNull("20210001")).thenReturn(existingMember)

            val result = processor.parse(sheet, departments, parts, emptyMap())

            assertThat(result.hasErrors()).isFalse()
            val memberCaptor = argumentCaptor<Member>()
            val dateCaptor = argumentCaptor<LocalDate>()
            verify(memberWriter).deleteFromActiveMember(any())
            verify(memberWriter).writeMemberWithGraduatedState(memberCaptor.capture(), dateCaptor.capture())
            assertThat(memberCaptor.firstValue.state).isEqualTo(MemberState.COMPLETED)
        }
    }
}

