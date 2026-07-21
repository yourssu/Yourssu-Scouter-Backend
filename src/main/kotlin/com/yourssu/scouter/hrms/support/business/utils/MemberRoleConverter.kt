package com.yourssu.scouter.hrms.support.business.utils

import com.yourssu.scouter.hrms.member.implement.MemberRole

object MemberRoleConverter {

    private val roleToString = mapOf(
        MemberRole.LEAD to "Lead",
        MemberRole.VICE_LEAD to "ViceLead",
        MemberRole.MEMBER to "Member",
    )

    private val stringToRole = roleToString.entries.associate { it.value to it.key }

    fun convertToString(role: MemberRole): String = roleToString[role]
        ?: throw IllegalArgumentException("Unknown role: $role")

    fun convertToEnum(role: String): MemberRole {
        val blankRemovedRole = role.replace(" ", "")

        return stringToRole[blankRemovedRole]
            ?: throw IllegalArgumentException("Unknown Member Role: $role")
    }
}
