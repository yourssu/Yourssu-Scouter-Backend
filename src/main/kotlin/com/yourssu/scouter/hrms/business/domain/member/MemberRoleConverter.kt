package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.MemberRole

object MemberRoleConverter {

    private val roleToString = mapOf(
        MemberRole.LEAD to "Lead",
        MemberRole.VICE_LEAD to "ViceLead",
        MemberRole.MEMBER to "Member",
    )

    fun convertToString(role: MemberRole): String = roleToString[role]
        ?: throw IllegalArgumentException("Unknown role: $role")
}
