package com.yourssu.scouter.member.core.business

import com.yourssu.scouter.member.core.business.dto.UpdateMemberInfoCommand

import com.yourssu.scouter.member.core.business.dto.UpdateInactiveMemberCommand

import com.yourssu.scouter.masterdata.department.implement.DepartmentReader
import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
import com.yourssu.scouter.member.support.exception.IllegalMemberUpdateException
import com.yourssu.scouter.member.core.fixture.MemberFixtureBuilder
import com.yourssu.scouter.member.core.implement.InactiveMember
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.member.core.implement.MemberState
import com.yourssu.scouter.member.core.implement.MemberWriter
import com.yourssu.scouter.member.core.implement.SemesterPeriod
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
    fun `activitySemestersPatch만 있으면 비액티브 메타 갱신으로 처리된다`() {
        val member = MemberFixtureBuilder().build().apply { state = MemberState.INACTIVE }
        val s2025_1 = Semester(2025, 1)
        val s2025_2 = Semester(2025, 2)
        val inactive = InactiveMember(
            id = 1L,
            member = member,
            activePeriod = SemesterPeriod(s2025_1, s2025_1),
            expectedReturnSemester = s2025_2,
            inactivePeriod = SemesterPeriod(s2025_1, s2025_1),
        )
        whenever(memberReader.readInactiveByMemberId(10L)).thenReturn(inactive)

        service.updateInactiveById(
            UpdateInactiveMemberCommand(
                targetMemberId = 10L,
                inactiveMetadataPatch = UpdateInactiveMemberMetadataPatch(
                    activitySemestersLabel = "23-1, 24-2",
                    totalActiveSemesters = 3,
                ),
            ),
        )

        val captor = argumentCaptor<InactiveMember>()
        verify(memberWriter).update(captor.capture())
        assertThat(captor.firstValue.activitySemestersLabel).isEqualTo("23-1, 24-2")
        assertThat(captor.firstValue.totalActiveSemesters).isEqualTo(3)
    }

    @Test
    fun `회원 프로필 수정과 비액티브 메타를 동시에내면 예외`() {
        assertThatThrownBy {
            service.updateInactiveById(
                UpdateInactiveMemberCommand(
                    targetMemberId = 10L,
                    updateMemberInfoCommand = UpdateMemberInfoCommand(
                        targetMemberId = 10L,
                        name = "새이름",
                    ),
                    inactiveMetadataPatch = UpdateInactiveMemberMetadataPatch(reason = "휴학"),
                ),
            )
        }.isInstanceOf(IllegalMemberUpdateException::class.java)
    }
}
