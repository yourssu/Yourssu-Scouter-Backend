package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@DisplayName("ApplicantPassSheetProcessor")
class ApplicantPassSheetProcessorTest {

    private lateinit var workbook: XSSFWorkbook
    private lateinit var memberPartRoleResolver: MemberPartRoleResolver
    private lateinit var mappingData: MemberParseMappingData
    private lateinit var memberReader: MemberReader
    private lateinit var memberWriter: MemberWriter
    private lateinit var processor: ApplicantPassSheetProcessor

    private val division = Division(id = 1L, name = "개발", sortPriority = 1)
    private val part = Part(id = 1L, division = division, name = "Backend", sortPriority = 1)
    private val department = Department(id = 1L, collegeId = 1L, name = "컴퓨터학부")
    private val joinDate = LocalDate.of(2025, 9, 1)

    @BeforeEach
    fun setUp() {
        workbook = XSSFWorkbook()
        memberPartRoleResolver = mock()
        mappingData = mock()
        memberReader = mock()
        memberWriter = mock()
        processor = ApplicantPassSheetProcessor(
            memberPartRoleResolver = memberPartRoleResolver,
            mappingData = mappingData,
            memberReader = memberReader,
            memberWriter = memberWriter,
        )
        whenever(mappingData.departmentAliases).thenReturn(emptyMap())
        whenever(memberPartRoleResolver.toPartAndRoles(any(), any(), any()))
            .thenReturn(MemberPartAndRoles(setOf(MemberPartAndRole(part, com.yourssu.scouter.hrms.implement.domain.member.MemberRole.MEMBER))))
        whenever(memberReader.existsByPhoneNumber(any())).thenReturn(false)
        whenever(memberReader.existsByStudentId(any())).thenReturn(false)
    }

    @AfterEach
    fun tearDown() {
        workbook.close()
    }

    private fun createSheetWithHeader(): org.apache.poi.ss.usermodel.Sheet {
        val sheet = workbook.createSheet("합격자")
        val headerRow = sheet.createRow(0)
        listOf("일시", "지원 포지션", "이름", "닉네임", "소속", "전화번호", "생년월일", "학번").forEachIndexed { i, v ->
            headerRow.createCell(i).setCellValue(v)
        }
        return sheet
    }

    private fun addDataRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        timestamp: String = "2025. 9. 18 15:59:02",
        position: String = "Backend Engineer",
        name: String = "홍길동",
        nickname: String = "Roro (로로)",
        departmentName: String = "컴퓨터학부",
        phone: String = "010-1234-5678",
        birthDate: String = "2002.01.15",
        studentId: String = "20210001",
    ) {
        val rowIndex = sheet.lastRowNum + 1
        val row = sheet.createRow(rowIndex)
        row.createCell(0).setCellValue(timestamp)
        row.createCell(1).setCellValue(position)
        row.createCell(2).setCellValue(name)
        row.createCell(3).setCellValue(nickname)
        row.createCell(4).setCellValue(departmentName)
        row.createCell(5).setCellValue(phone)
        row.createCell(6).setCellValue(birthDate)
        row.createCell(7).setCellValue(studentId)
    }

    @Nested
    @DisplayName("parse - 정상 한 행")
    inner class ParseSingleRow {

        @Test
        fun `유효한 한 행이면 멤버가 ACTIVE로 저장되고 에러 없음`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "김철수", studentId = "20219999", phone = "010-9999-8888")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            assertThat(result.hasErrors()).isFalse()
            val captor = argumentCaptor<com.yourssu.scouter.hrms.implement.domain.member.Member>()
            verify(memberWriter).writeMemberWithActiveStatus(captor.capture(), eq(false))
            val member = captor.firstValue
            assertThat(member.name).isEqualTo("김철수")
            assertThat(member.studentId).isEqualTo("20219999")
            assertThat(member.phoneNumber).isEqualTo("010-9999-8888")
            assertThat(member.joinDate).isEqualTo(joinDate)
            assertThat(member.state).isEqualTo(com.yourssu.scouter.hrms.implement.domain.member.MemberState.ACTIVE)
            assertThat(member.nicknameEnglish).isEqualTo("Roro")
            assertThat(member.nicknameKorean).isEqualTo("로로")
        }
    }

    @Nested
    @DisplayName("parse - 행 스킵")
    inner class ParseSkipRows {

        @Test
        fun `이름이 비어 있으면 해당 행은 처리하지 않음`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "", studentId = "20210001")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            verify(memberWriter, org.mockito.kotlin.never()).writeMemberWithActiveStatus(any(), any())
            assertThat(result.hasErrors()).isFalse()
        }

        @Test
        fun `이름 셀에 취소선이 있으면 해당 행은 스킵`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "취소된사람", studentId = "20210002", phone = "010-1111-2222")
            val nameCell = sheet.getRow(1).getCell(2)
            val font = workbook.createFont().apply { strikeout = true }
            val style = workbook.createCellStyle().apply { setFont(font) }
            nameCell.cellStyle = style

            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            verify(memberWriter, org.mockito.kotlin.never()).writeMemberWithActiveStatus(any(), any())
            assertThat(result.hasErrors()).isFalse()
        }
    }

    @Nested
    @DisplayName("parse - 시트 내 중복 시 패치")
    inner class ParseDuplicateInSheet {

        @Test
        fun `같은 전화번호가 두 행이어도 에러 없이 첫 행은 신규 저장 후 둘째 행은 해당 멤버 업데이트`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "A", studentId = "20210001", phone = "010-1234-5678")
            addDataRow(sheet, name = "B", studentId = "20210002", phone = "010-1234-5678")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)
            val existingMember = MemberFixtureBuilder()
                .name("A")
                .studentId("20210001")
                .email("a@test.com")
                .build()
            whenever(memberReader.readByPhoneNumberOrNull("010-1234-5678"))
                .thenReturn(null)
                .thenReturn(existingMember)
            whenever(memberReader.readActiveByMemberId(existingMember.id!!)).thenReturn(
                com.yourssu.scouter.hrms.implement.domain.member.ActiveMember(
                    id = 1L,
                    member = existingMember,
                    isMembershipFeePaid = false,
                )
            )

            val result = processor.parse(sheet, departments, parts, joinDate)

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter, org.mockito.kotlin.times(1)).writeMemberWithActiveStatus(any(), eq(false))
            verify(memberWriter, org.mockito.kotlin.times(1)).update(any<com.yourssu.scouter.hrms.implement.domain.member.ActiveMember>())
        }
    }

    @Nested
    @DisplayName("parse - DB에 이미 있으면 패치")
    inner class ParseDuplicateInDb {

        @Test
        fun `전화번호가 이미 DB에 있으면 해당 멤버를 시트 값으로 패치`() {
            val existingMember = MemberFixtureBuilder()
                .name("기존이름")
                .studentId("20210001")
                .email("existing@test.com")
                .build()
            whenever(memberReader.readByPhoneNumberOrNull("010-1234-5678")).thenReturn(existingMember)
            whenever(memberReader.readActiveByMemberId(existingMember.id!!)).thenReturn(
                com.yourssu.scouter.hrms.implement.domain.member.ActiveMember(
                    id = 1L,
                    member = existingMember,
                    isMembershipFeePaid = false,
                )
            )
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "홍길동", studentId = "20210001", phone = "010-1234-5678")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter, org.mockito.kotlin.never()).writeMemberWithActiveStatus(any(), any())
            val captor = argumentCaptor<com.yourssu.scouter.hrms.implement.domain.member.ActiveMember>()
            verify(memberWriter).update(captor.capture())
            assertThat(captor.firstValue.member.name).isEqualTo("홍길동")
            assertThat(captor.firstValue.member.email).isEqualTo("existing@test.com")
            assertThat(captor.firstValue.member.phoneNumber).isEqualTo("010-1234-5678")
        }

        @Test
        fun `학번이 이미 DB에 있으면 해당 멤버를 시트 값으로 패치`() {
            val existingMember = MemberFixtureBuilder()
                .studentId("20210001")
                .email("existing@test.com")
                .build()
            whenever(memberReader.readByStudentIdOrNull("20210001")).thenReturn(existingMember)
            whenever(memberReader.readActiveByMemberId(existingMember.id!!)).thenReturn(
                com.yourssu.scouter.hrms.implement.domain.member.ActiveMember(
                    id = 1L,
                    member = existingMember,
                    isMembershipFeePaid = false,
                )
            )
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "홍길동", studentId = "20210001", phone = "010-1234-5678")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter).update(any<com.yourssu.scouter.hrms.implement.domain.member.ActiveMember>())
        }
    }

    @Nested
    @DisplayName("parse - 학과/직무 매핑 실패")
    inner class ParseMappingFailure {

        @Test
        fun `없는 학과면 해당 행 에러`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, departmentName = "없는학과", name = "홍길동", studentId = "20210001")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            assertThat(result.errorMessages).anyMatch { it.contains("학과") && it.contains("찾을 수 없습니다") }
            verify(memberWriter, org.mockito.kotlin.never()).writeMemberWithActiveStatus(any(), any())
        }

        @Test
        fun `매핑되지 않은 직무면 해당 행 에러`() {
            whenever(memberPartRoleResolver.toPartAndRoles(any(), any(), any()))
                .thenReturn(MemberPartAndRoles(emptySet()))
            val sheet = createSheetWithHeader()
            addDataRow(sheet, position = "Unknown Role", name = "홍길동", studentId = "20210001")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            assertThat(result.errorMessages).anyMatch { it.contains("파트/역할을 찾을 수 없습니다") }
            verify(memberWriter, org.mockito.kotlin.never()).writeMemberWithActiveStatus(any(), any())
        }

    }

    @Nested
    @DisplayName("parse - 복수 행 정상")
    inner class ParseMultipleRows {

        @Test
        fun `서로 다른 전화번호와 학번인 두 행이면 두 명 모두 저장`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "A", studentId = "20210001", phone = "010-1111-1111")
            addDataRow(sheet, name = "B", studentId = "20210002", phone = "010-2222-2222")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)

            val result = processor.parse(sheet, departments, parts, joinDate)

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter, org.mockito.kotlin.times(2)).writeMemberWithActiveStatus(any(), eq(false))
        }
    }

    @Nested
    @DisplayName("collectUnknownDepartments")
    inner class CollectUnknownDepartments {

        @Test
        fun `DB에 없는 학과명만 수집하고 정렬하여 반환`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, departmentName = "컴퓨터학부", name = "A", studentId = "20210001")
            addDataRow(sheet, departmentName = "컴퓨터학과", name = "B", studentId = "20210002")
            addDataRow(sheet, departmentName = "소프트웨어학과", name = "C", studentId = "20210003")
            val departments = mapOf("컴퓨터학부" to department)

            val unknown = processor.collectUnknownDepartments(sheet, departments)

            assertThat(unknown).containsExactlyInAnyOrder("소프트웨어학과", "컴퓨터학과")
            assertThat(unknown).isSortedAccordingTo(String::compareTo)
        }

        @Test
        fun `모든 학과가 DB에 있으면 빈 목록`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, departmentName = "컴퓨터학부", name = "A", studentId = "20210001")
            val departments = mapOf("컴퓨터학부" to department)

            val unknown = processor.collectUnknownDepartments(sheet, departments)

            assertThat(unknown).isEmpty()
        }
    }

    @Nested
    @DisplayName("parse - departmentOverrides")
    inner class ParseWithOverrides {

        @Test
        fun `오타난 학과명을 override로 넘기면 해당 학부로 매핑되어 저장됨`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, departmentName = "컴퓨터학과", name = "홍길동", studentId = "20210001")
            val departments = mapOf("컴퓨터학부" to department)
            val parts = mapOf("Backend" to part)
            val overrides = mapOf("컴퓨터학과" to "컴퓨터학부")

            val result = processor.parse(sheet, departments, parts, joinDate, overrides)

            assertThat(result.hasErrors()).isFalse()
            val captor = argumentCaptor<com.yourssu.scouter.hrms.implement.domain.member.Member>()
            verify(memberWriter).writeMemberWithActiveStatus(captor.capture(), eq(false))
            assertThat(captor.firstValue.department.name).isEqualTo("컴퓨터학부")
        }
    }
}
