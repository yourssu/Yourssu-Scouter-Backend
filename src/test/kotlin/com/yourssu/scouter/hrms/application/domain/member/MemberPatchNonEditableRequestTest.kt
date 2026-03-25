package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.support.exception.MemberFieldNotEditableException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MemberPatchNonEditableRequestTest {

    @Test
    fun `졸업 PATCH에 activePeriod가 있으면 Member-007`() {
        assertThatThrownBy {
            UpdateGraduatedMemberRequest(
                activePeriod = NonEditableSemesterPeriodBody(),
            ).toCommand(1L)
        }.isInstanceOf(MemberFieldNotEditableException::class.java)
    }

    @Test
    fun `비액티브 PATCH에 activePeriod가 있으면 Member-007`() {
        assertThatThrownBy {
            UpdateInactiveMemberRequest(
                activePeriod = NonEditableSemesterPeriodBody(),
            ).toCommand(1L)
        }.isInstanceOf(MemberFieldNotEditableException::class.java)
    }

    @Test
    fun `비액티브 PATCH에 activeSemesterCountLabel이 있으면 Member-007`() {
        assertThatThrownBy {
            UpdateInactiveMemberRequest(
                activeSemesterCountLabel = "3학기",
            ).toCommand(1L)
        }.isInstanceOf(MemberFieldNotEditableException::class.java)
    }
}
