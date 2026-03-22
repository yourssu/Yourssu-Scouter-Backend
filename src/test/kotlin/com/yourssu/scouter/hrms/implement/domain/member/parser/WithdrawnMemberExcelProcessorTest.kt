package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.hrms.business.domain.member.MemberExcelImportOverrides
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

@DisplayName("WithdrawnMemberExcelProcessor")
class WithdrawnMemberExcelProcessorTest {

    private lateinit var workbook: XSSFWorkbook
    private lateinit var memberReader: MemberReader
    private lateinit var memberWriter: MemberWriter
    private lateinit var memberPartRoleResolver: MemberPartRoleResolver
    private lateinit var processor: WithdrawnMemberExcelProcessor

    private val department = Department(id = 1L, collegeId = 1L, name = "컴퓨터학부")
    private val part = PartFixtureBuilder().id(1L).name("백엔드").build()

    @BeforeEach
    fun setUp() {
        workbook = XSSFWorkbook()
        memberReader = mock()
        memberWriter = mock()
        memberPartRoleResolver = mock()
        whenever(memberPartRoleResolver.toPartAndRoles(any(), any(), anyOrNull()))
            .thenReturn(MemberPartAndRoles(setOf(MemberPartAndRole(part, MemberRole.MEMBER))))
        processor = WithdrawnMemberExcelProcessor(
            memberReader = memberReader,
            memberWriter = memberWriter,
            memberPartRoleResolver = memberPartRoleResolver,
        )
        whenever(memberReader.searchAllCompletedByNameOrNickname(any())).thenReturn(emptyList())
    }

    @AfterEach
    fun tearDown() {
        workbook.close()
    }

    private fun createSheetWithHeader(): org.apache.poi.ss.usermodel.Sheet {
        val sheet = workbook.createSheet("탈퇴")
        val headerRow = sheet.createRow(0)
        listOf("이름", "닉네임(발음)", "부서", "탈퇴일자", "비고").forEachIndexed { i, v ->
            headerRow.createCell(i).setCellValue(v)
        }
        return sheet
    }

    private fun addDataRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        name: String = "홍길동",
        nickname: String = "Roro(로로)",
        departmentName: String = "컴퓨터학부",
        withdrawnDate: String = "2025-09-01",
        note: String = "개인 사정",
    ) {
        val rowIndex = sheet.lastRowNum + 1
        val row = sheet.createRow(rowIndex)
        row.createCell(0).setCellValue(name)
        row.createCell(1).setCellValue(nickname)
        row.createCell(2).setCellValue(departmentName)
        row.createCell(3).setCellValue(withdrawnDate)
        row.createCell(4).setCellValue(note)
    }

    @Nested
    @DisplayName("parse - 정상 행")
    inner class ParseNormalRow {

        @Test
        fun `이름+부서로 단일 멤버를 찾아 WITHDRAWN 상태로 전환하고 비고에 탈퇴일자를 추가한다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "홍길동", departmentName = "백엔드", withdrawnDate = "2025-09-01", note = "개인 사정")
            val departments = mapOf("컴퓨터학부" to department)

            val existingMember = MemberFixtureBuilder()
                .name("홍길동")
                .build()

            whenever(memberReader.searchAllActiveByNameOrNickname("홍길동"))
                .thenReturn(listOf(com.yourssu.scouter.hrms.implement.domain.member.ActiveMember(id = 1L, member = existingMember, isMembershipFeePaid = false)))
            whenever(memberReader.searchAllInactiveByNameOrNickname("홍길동"))
                .thenReturn(emptyList())
            whenever(memberReader.searchAllGraduatedByNameOrNickname("홍길동"))
                .thenReturn(emptyList())
            whenever(memberReader.searchAllWithdrawnByNameOrNickname("홍길동"))
                .thenReturn(emptyList())

            val result = processor.parse(sheet, departments, mapOf("백엔드" to part), MemberExcelImportOverrides.EMPTY)

            assertThat(result.hasErrors()).isFalse()
            val memberCaptor = argumentCaptor<Member>()
            verify(memberWriter).deleteFromActiveMember(memberCaptor.capture())
            val withdrawnMemberCaptor = argumentCaptor<Member>()
            verify(memberWriter).writeMemberWithWithdrawnState(withdrawnMemberCaptor.capture(), eq(LocalDate.of(2025, 9, 1)))
            val updated = withdrawnMemberCaptor.firstValue
            assertThat(updated.state).isEqualTo(MemberState.WITHDRAWN)
            assertThat(updated.note).contains("탈퇴일자: 2025-09-01").contains("개인 사정")
        }
    }

    @Nested
    @DisplayName("parse - 멤버 검색 실패 및 중복")
    inner class ParseSearchFailures {

        @Test
        fun `이름+부서에 해당하는 멤버가 없으면 에러 메시지에 닉네임이 포함된다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "없는사람", nickname = "Nick(닉)", departmentName = "Backend")
            val departments = mapOf("컴퓨터학부" to department)

            whenever(memberReader.searchAllActiveByNameOrNickname("없는사람")).thenReturn(emptyList())
            whenever(memberReader.searchAllInactiveByNameOrNickname("없는사람")).thenReturn(emptyList())
            whenever(memberReader.searchAllGraduatedByNameOrNickname("없는사람")).thenReturn(emptyList())
            whenever(memberReader.searchAllWithdrawnByNameOrNickname("없는사람")).thenReturn(emptyList())

            val result = processor.parse(sheet, departments, mapOf("백엔드" to part), MemberExcelImportOverrides.EMPTY)

            assertThat(result.hasErrors()).isTrue()
            assertThat(result.errorMessages.first()).contains("닉네임").contains("Nick(닉)")
            verify(memberWriter, never()).writeMemberWithWithdrawnState(any(), any())
        }

        @Test
        fun `이름+부서에 해당하는 멤버가 여러 명이면 에러 메시지에 닉네임이 포함된다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, name = "중복이름", nickname = "Nick(닉)", departmentName = "Backend")
            val departments = mapOf("컴퓨터학부" to department)

            val member1 = MemberFixtureBuilder().name("중복이름").build()
            val member2 = MemberFixtureBuilder().name("중복이름").build()

            whenever(memberReader.searchAllActiveByNameOrNickname("중복이름"))
                .thenReturn(
                    listOf(
                        com.yourssu.scouter.hrms.implement.domain.member.ActiveMember(id = 1L, member = member1, isMembershipFeePaid = false),
                        com.yourssu.scouter.hrms.implement.domain.member.ActiveMember(id = 2L, member = member2, isMembershipFeePaid = false),
                    ),
                )
            whenever(memberReader.searchAllInactiveByNameOrNickname("중복이름")).thenReturn(emptyList())
            whenever(memberReader.searchAllGraduatedByNameOrNickname("중복이름")).thenReturn(emptyList())
            whenever(memberReader.searchAllWithdrawnByNameOrNickname("중복이름")).thenReturn(emptyList())

            val result = processor.parse(sheet, departments, mapOf("백엔드" to part), MemberExcelImportOverrides.EMPTY)

            assertThat(result.hasErrors()).isTrue()
            assertThat(result.errorMessages.first()).contains("닉네임").contains("Nick(닉)")
            verify(memberWriter, never()).writeMemberWithWithdrawnState(any(), any())
        }
    }

    @Nested
    @DisplayName("parse - 탈퇴일자 파싱")
    inner class ParseWithdrawnDate {

        @Test
        fun `탈퇴일자가 잘못된 형식이면 일자만 폴백하고 탈퇴 처리는 진행한다`() {
            val sheet = createSheetWithHeader()
            addDataRow(sheet, departmentName = "백엔드", withdrawnDate = "not-a-date")
            val departments = mapOf("컴퓨터학부" to department)

            val existingMember = MemberFixtureBuilder()
                .name("홍길동")
                .build()

            whenever(memberReader.searchAllActiveByNameOrNickname("홍길동"))
                .thenReturn(listOf(com.yourssu.scouter.hrms.implement.domain.member.ActiveMember(id = 1L, member = existingMember, isMembershipFeePaid = false)))
            whenever(memberReader.searchAllInactiveByNameOrNickname("홍길동")).thenReturn(emptyList())
            whenever(memberReader.searchAllGraduatedByNameOrNickname("홍길동")).thenReturn(emptyList())
            whenever(memberReader.searchAllWithdrawnByNameOrNickname("홍길동")).thenReturn(emptyList())

            val result = processor.parse(sheet, departments, mapOf("백엔드" to part), MemberExcelImportOverrides.EMPTY)

            assertThat(result.hasErrors()).isFalse()
            verify(memberWriter).writeMemberWithWithdrawnState(any(), eq(LocalDate.of(2099, 12, 31)))
        }
    }
}
