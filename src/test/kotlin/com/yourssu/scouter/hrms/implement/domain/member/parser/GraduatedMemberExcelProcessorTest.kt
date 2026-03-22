package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMember
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Instant

@DisplayName("GraduatedMemberExcelProcessor")
class GraduatedMemberExcelProcessorTest {

    private lateinit var workbook: XSSFWorkbook
    private lateinit var memberPartRoleResolver: MemberPartRoleResolver
    private lateinit var mappingData: MemberParseMappingData
    private lateinit var basicMemberExcelProcessor: BasicMemberExcelProcessor
    private lateinit var semesterReader: SemesterReader
    private lateinit var memberReader: MemberReader
    private lateinit var memberWriter: MemberWriter
    private lateinit var processor: GraduatedMemberExcelProcessor

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
        semesterReader = mock()
        memberReader = mock()
        memberWriter = mock()
        processor = GraduatedMemberExcelProcessor(
            basicMemberExcelProcessor = basicMemberExcelProcessor,
            semesterReader = semesterReader,
            memberReader = memberReader,
            memberWriter = memberWriter,
        )
    }

    @AfterEach
    fun tearDown() {
        workbook.close()
    }

    private fun createSheetWithHeader(): org.apache.poi.ss.usermodel.Sheet {
        val sheet = workbook.createSheet("졸업")
        val headerRow = sheet.createRow(0)
        // 0: 파트, 1: 이름, 2: 닉네임, 3: 발음, 4: 연락처, 5: 이메일, 6: 전공, 7: 생년월일, 8: 학번, 9: 가입일, 10: 비고, 11: 졸업학기
        listOf(
            "파트",
            "이름",
            "닉네임",
            "닉네임(발음)",
            "연락처",
            "유어슈 이메일",
            "전공",
            "생년월일",
            "학번",
            "가입일",
            "비고",
            "졸업학기",
        ).forEachIndexed { i, v ->
            headerRow.createCell(i).setCellValue(v)
        }
        return sheet
    }

    private fun addDataRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        partName: String = "Backend",
        name: String = "홍길동",
        nickname: String = "Roro",
        pronunciation: String = "로로",
        phone: String = "010-1234-5678",
        email: String = "test@yourssu.com",
        departmentName: String = "컴퓨터학부",
        birthDate: String = "2002.01.15",
        studentId: String = "20210001",
        joinDate: String = "23.03.01",
        note: String = "",
        graduatedSemesterText: String = "2025-2",
    ) {
        val rowIndex = sheet.lastRowNum + 1
        val row = sheet.createRow(rowIndex)
        row.createCell(0).setCellValue(partName)
        row.createCell(1).setCellValue(name)
        row.createCell(2).setCellValue(nickname)
        row.createCell(3).setCellValue(pronunciation)
        row.createCell(4).setCellValue(phone)
        row.createCell(5).setCellValue(email)
        row.createCell(6).setCellValue(departmentName)
        row.createCell(7).setCellValue(birthDate)
        row.createCell(8).setCellValue(studentId)
        row.createCell(9).setCellValue(joinDate)
        row.createCell(10).setCellValue(note)
        row.createCell(11).setCellValue(graduatedSemesterText)
    }

    @Nested
    @DisplayName("parse - 이미 졸업 멤버 중복 업로드")
    inner class ParseExistingGraduatedMember {

        @Test
        fun `졸업 시트를 두 번 업로드해도 graduated_member는 delete+insert 패턴으로 처리되어 중복 insert가 발생하지 않는다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "홍길동", studentId = "20210001", graduatedSemesterText = "2025-2")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val existingMember = MemberFixtureBuilder()
                .name("홍길동")
                .studentId("20210001")
                .email("test@yourssu.com")
                .build().apply {
                    updateState(MemberState.GRADUATED, Instant.now())
                }

            val graduatedSemester = Semester.of(LocalDate.of(2025, 9, 1))

            whenever(memberReader.readByStudentIdOrNull("20210001")).thenReturn(existingMember)
            whenever(semesterReader.readByString("2025-2")).thenReturn(graduatedSemester)

            val result = processor.parse(sheet, departments, parts, emptyMap(), emptyMap())

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter, times(1)).writeMemberWithGraduatedState(any<Member>(), any<Semester>())
            verify(memberWriter, never()).update(any<GraduatedMember>())
        }
    }
}

