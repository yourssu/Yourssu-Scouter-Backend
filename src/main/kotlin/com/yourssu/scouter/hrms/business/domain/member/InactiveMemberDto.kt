package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember

data class InactiveMemberDto(
    val id: Long,
    val member: MemberDto,
    val activePeriod: SemesterPeriodDto,
    val expectedReturnSemester: SemesterDto,
    val inactivePeriod: SemesterPeriodDto,
    val reason: String? = null,
    val smsReplied: Boolean? = null,
    val smsReplyDesiredPeriod: String? = null,
    val activitySemestersLabel: String? = null,
    val totalActiveSemesters: Int? = null,
    val totalInactiveSemesters: Int? = null,
) {

    companion object {
        fun from(inactiveMember: InactiveMember): InactiveMemberDto = InactiveMemberDto(
            id = inactiveMember.id!!,
            member = MemberDto.from(inactiveMember.member),
            activePeriod = SemesterPeriodDto.from(inactiveMember.activePeriod),
            expectedReturnSemester = SemesterDto.from(inactiveMember.expectedReturnSemester),
            inactivePeriod = SemesterPeriodDto.from(inactiveMember.inactivePeriod),
            reason = inactiveMember.reason,
            smsReplied = inactiveMember.smsReplied,
            smsReplyDesiredPeriod = inactiveMember.smsReplyDesiredPeriod,
            activitySemestersLabel = inactiveMember.activitySemestersLabel,
            totalActiveSemesters = inactiveMember.totalActiveSemesters,
            totalInactiveSemesters = inactiveMember.totalInactiveSemesters,
        )
    }
}
