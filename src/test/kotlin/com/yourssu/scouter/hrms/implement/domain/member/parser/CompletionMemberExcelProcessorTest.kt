package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.common.implement.domain.semester.Term
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Year

@DisplayName("CompletionMemberExcelProcessor")
class CompletionMemberExcelProcessorTest {

    private lateinit var workbook: XSSFWorkbook
    private lateinit var memberPartRoleResolver: MemberPartRoleResolver
    private lateinit var mappingData: MemberParseMappingData
    private lateinit var basicMemberExcelProcessor: BasicMemberExcelProcessor
    private lateinit var memberReader: MemberReader
    private lateinit var memberWriter: MemberWriter
    private lateinit var semesterRepository: SemesterRepository
    private lateinit var processor: CompletionMemberExcelProcessor

    private val department = Department(id = 1L, collegeId = 1L, name = "컴퓨터학부")
    private val part = PartFixtureBuilder().id(1L).name("Backend").build()
    private val semester2025_1 = Semester(id = 100L, year = Year.of(2025), term = Term.SPRING)

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
        semesterRepository = mock()
        whenever(semesterRepository.find(any())).thenAnswer { inv ->
            val arg = inv.getArgument<Semester>(0)
            if (arg.year == Year.of(2025) && arg.term == Term.SPRING) semester2025_1 else null
        }
        processor = CompletionMemberExcelProcessor(
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
        val sheet = workbook.createSheet("수료")
        val headerRow = sheet.createRow(0)
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
        completionSemesterCell: String = "25-1",
    ) {
        val rowIndex = sheet.lastRowNum + 1
        val row = sheet.createRow(rowIndex)
        row.createCell(0).setCellValue("팀A")
        row.createCell(1).setCellValue("Backend")
        row.createCell(2).setCellValue(name)
        row.createCell(3).setCellValue(nickname)
        row.createCell(4).setCellValue(pronunciation)
        row.createCell(5).setCellValue(email)
        row.createCell(6).setCellValue(phone)
        row.createCell(7).setCellValue(departmentName)
        row.createCell(8).setCellValue(birthDate)
        row.createCell(9).setCellValue(studentId)
        row.createCell(10).setCellValue(joinDate)
        row.createCell(11).setCellValue(completionSemesterCell)
    }

    @Nested
    @DisplayName("parse - 신규 COMPLETED 멤버")
    inner class ParseNewCompletedMember {

        @Test
        fun `DB에 학번이 없으면 시트 수료 학기로 writeMemberWithCompletedState 호출`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, studentId = "20219999", completionSemesterCell = "25-1")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            whenever(memberReader.readByStudentIdOrNull("20219999")).thenReturn(null)

            val result = processor.parse(sheet, departments, parts, emptyMap(), emptyMap())

            assertThat(result.hasErrors()).isFalse()
            val memberCaptor = argumentCaptor<Member>()
            val semesterCaptor = argumentCaptor<Semester>()
            verify(memberWriter).writeMemberWithCompletedState(memberCaptor.capture(), semesterCaptor.capture())
            assertThat(memberCaptor.firstValue.state).isEqualTo(MemberState.COMPLETED)
            assertThat(semesterCaptor.firstValue).isEqualTo(semester2025_1)
        }

        @Test
        fun `시트 수료 학기가 잘못되었으면 raw 키로 넘긴 yy-s 매핑으로 채운다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, studentId = "20218888", completionSemesterCell = "not-a-semester")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            whenever(memberReader.readByStudentIdOrNull("20218888")).thenReturn(null)

            val result = processor.parse(
                sheet,
                departments,
                parts,
                emptyMap(),
                mapOf("not-a-semester" to "25-1"),
            )

            assertThat(result.hasErrors()).isFalse()
            val semesterCaptor = argumentCaptor<Semester>()
            verify(memberWriter).writeMemberWithCompletedState(any(), semesterCaptor.capture())
            assertThat(semesterCaptor.firstValue).isEqualTo(semester2025_1)
        }

        @Test
        fun `시트와 매핑 입력 모두 해석 실패하면 행 오류`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, studentId = "20217777", completionSemesterCell = "bad")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            whenever(memberReader.readByStudentIdOrNull("20217777")).thenReturn(null)

            val result = processor.parse(
                sheet,
                departments,
                parts,
                emptyMap(),
                mapOf("bad" to "also-bad"),
            )

            assertThat(result.hasErrors()).isTrue()
        }
    }

    @Nested
    @DisplayName("parse - 기존 멤버 COMPLETED 전환")
    inner class ParseExistingMember {

        @Test
        fun `기존 ACTIVE 멤버면 상태를 COMPLETED로 바꾸고 수료 엔티티를 갱신한다`() {
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

            val result = processor.parse(sheet, departments, parts, emptyMap(), emptyMap())

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter).deleteFromActiveMember(any())
            val semesterCaptor = argumentCaptor<Semester>()
            verify(memberWriter).writeMemberWithCompletedState(any(), semesterCaptor.capture())
            assertThat(semesterCaptor.firstValue).isEqualTo(semester2025_1)
        }
    }
}
