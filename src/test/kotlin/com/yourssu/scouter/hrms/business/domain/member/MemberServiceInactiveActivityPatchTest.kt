package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.domain.member.SemesterPeriod
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.hrms.business.support.exception.IllegalMemberUpdateException
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MemberServiceInactiveActivityPatchTest {

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
    fun `activitySemestersPatch만내면 활동학기 표시 필드만 갱신한다`() {
        val memberId = 10L
        val s252 = Semester(2025, 2)
        val s261 = Semester(2026, 1)
        val member: Member = MemberFixtureBuilder().id(memberId).name("테스트").studentId("20999999").build().apply {
            state = MemberState.INACTIVE
        }
        val inactive = InactiveMember(
            id = 1L,
            member = member,
            activePeriod = SemesterPeriod(s252, s252),
            expectedReturnSemester = s261,
            inactivePeriod = SemesterPeriod(s252, s252),
            reason = "사유",
            activitySemestersLabel = "옛날",
            totalActiveSemesters = 1,
        )
        whenever(memberReader.readInactiveByMemberId(memberId)).thenReturn(inactive)

        service.updateInactiveById(
            UpdateInactiveMemberCommand(
                targetMemberId = memberId,
                activitySemestersPatch = InactiveActivitySemestersPatch(
                    activitySemestersLabel = "23-1, 24-2~25-1",
                    totalActiveSemesters = 3,
                ),
            ),
        )

        val captor = argumentCaptor<InactiveMember>()
        verify(memberWriter).update(captor.capture())
        assertThat(captor.firstValue.activitySemestersLabel).isEqualTo("23-1, 24-2~25-1")
        assertThat(captor.firstValue.totalActiveSemesters).isEqualTo(3)
        assertThat(captor.firstValue.reason).isEqualTo("사유")
        assertThat(captor.firstValue.expectedReturnSemester).isEqualTo(s261)
    }

    @Test
    fun `activitySemestersPatch와 예정복귀를 동시에내면 예외`() {
        assertThatThrownBy {
            service.updateInactiveById(
                UpdateInactiveMemberCommand(
                    targetMemberId = 1L,
                    expectedReturnSemesterId = 99L,
                    activitySemestersPatch = InactiveActivitySemestersPatch(totalActiveSemesters = 2),
                ),
            )
        }.isInstanceOf(IllegalMemberUpdateException::class.java)
    }
}
