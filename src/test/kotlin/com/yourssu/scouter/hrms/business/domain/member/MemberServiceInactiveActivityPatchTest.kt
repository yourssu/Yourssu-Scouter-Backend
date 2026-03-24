package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.business.support.exception.IllegalMemberUpdateException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

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
    fun `activitySemestersPatch만내면 정책상 예외`() {
        assertThatThrownBy {
            service.updateInactiveById(
                UpdateInactiveMemberCommand(
                    targetMemberId = 10L,
                    activitySemestersPatch = InactiveActivitySemestersPatch(
                        activitySemestersLabel = "23-1, 24-2~25-1",
                        totalActiveSemesters = 3,
                    ),
                ),
            )
        }.isInstanceOf(IllegalMemberUpdateException::class.java)
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
