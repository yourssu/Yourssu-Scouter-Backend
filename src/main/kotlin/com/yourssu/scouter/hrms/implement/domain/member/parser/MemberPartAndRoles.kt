package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole

data class MemberPartAndRoles(
    val partAndRoles: Set<MemberPartAndRole>,
) {

    fun isEmpty(): Boolean {
        return partAndRoles.isEmpty() ||
               partAndRoles.any { it.part == null || it.role == null }
    }

    fun getRole(): MemberRole {
        val isLead = partAndRoles.any { it.role == MemberRole.LEAD }
        val isViceLead = partAndRoles.any { it.role == MemberRole.VICE_LEAD }

        return when {
            isLead -> MemberRole.LEAD
            isViceLead -> MemberRole.VICE_LEAD
            else -> MemberRole.MEMBER
        }
    }

    fun getParts(): Set<Part> {
        return partAndRoles.mapNotNull { it.part }.toSet()
    }
}

data class MemberPartAndRole (
    val part: Part?,
    val role: MemberRole?,
)
