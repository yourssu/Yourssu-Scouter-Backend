package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.MemberState

object MemberStateConverter {

    private val stateToString = mapOf(
        MemberState.ACTIVE to "액티브",
        MemberState.INACTIVE to "비액티브",
        MemberState.GRADUATED to "졸업",
        MemberState.WITHDRAWN to "탈퇴",
    )

    fun convertToString(state: MemberState): String = stateToString[state]
        ?: throw IllegalArgumentException("Unknown role: $state")
}
