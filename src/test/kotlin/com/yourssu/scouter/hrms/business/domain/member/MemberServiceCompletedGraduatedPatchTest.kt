package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.hrms.business.support.exception.IllegalMemberUpdateException
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.CompletedMember
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MemberServiceCompletedGraduatedPatchTest {

    private val memberWriter: MemberWriter = mock()
    private val memberReader: MemberReader = mock()
    private val departmentReader: DepartmentReader = mock()
    private val partReader: PartReader = mock()
    private val semesterReader: SemesterReader = mock()

    private val service = MemberService(
        memberWriter = memberWriter,
        memberReader = memberReader,
        departmentReader = departmentReader,
        partReader = partReader,
        semesterReader = semesterReader,
    )

    @Test
    fun `수료 completionSemester만내면 memberWriter update`() {
        val member = MemberFixtureBuilder().build().apply { state = MemberState.COMPLETED }
        val s2025_1 = Semester(2025, 1)
        val current = CompletedMember(id = 2L, member = member, completionSemester = Semester(2024, 2))
        whenever(memberReader.readCompletedByMemberId(20L)).thenReturn(current)
        whenever(semesterReader.readByString("25-1")).thenReturn(s2025_1)

        service.updateCompletedById(
            UpdateCompletedMemberCommand(
                targetMemberId = 20L,
                completionSemester = "25-1",
            ),
        )

        val captor = argumentCaptor<CompletedMember>()
        verify(memberWriter).update(captor.capture())
        assertThat(captor.firstValue.completionSemester).isEqualTo(s2025_1)
    }

    @Test
    fun `수료 프로필과 completionSemester 동시 요청이면 예외`() {
        assertThatThrownBy {
            service.updateCompletedById(
                UpdateCompletedMemberCommand(
                    targetMemberId = 1L,
                    updateMemberInfoCommand = UpdateMemberInfoCommand(targetMemberId = 1L, name = "a"),
                    completionSemester = "25-1",
                ),
            )
        }.isInstanceOf(IllegalMemberUpdateException::class.java)
    }
}
