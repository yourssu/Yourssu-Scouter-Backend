package com.yourssu.scouter.hrms.business.support.utils

import com.yourssu.scouter.hrms.implement.domain.member.MemberState

object MemberStateConverter {

    private val stateToString = mapOf(
        MemberState.ACTIVE to "액티브",
        MemberState.INACTIVE to "비액티브",
        MemberState.GRADUATED to "졸업",
        MemberState.WITHDRAWN to "탈퇴",
    )

    private val stringToState = stateToString.entries.associate { it.value to it.key }

    fun convertToString(state: MemberState): String = stateToString[state]
        ?: throw IllegalArgumentException("Unknown role: $state")

    fun convertToEnum(state: String): MemberState {
        val blankRemovedState = state.replace(" ", "")

        return stringToState[blankRemovedState]
            ?: throw IllegalArgumentException("Unknown Member State: $state")
    }
}
