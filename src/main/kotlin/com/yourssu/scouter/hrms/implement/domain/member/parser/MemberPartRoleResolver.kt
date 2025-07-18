package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData.MemberParseMappingEntry
import org.springframework.stereotype.Component

@Component
class MemberPartRoleResolver(
    private val mappingData: MemberParseMappingData,
) {

    fun toPartAndRoles(roleCell: String, parts: Map<String, Part>): MemberPartAndRoles {
        val result = mutableSetOf<MemberPartAndRole>()

        roleCell.split("/").forEach { value ->
            val partAndRole: MemberPartAndRole = resolveToPartAndRole(value.trim(), parts)
            result.add(partAndRole)
        }

        return MemberPartAndRoles(result)
    }

    private fun resolveToPartAndRole(value: String, parts: Map<String, Part>): MemberPartAndRole {
        for (entry: MemberParseMappingEntry in mappingData.partRoles) {
            val part = parts[entry.part] ?: continue

            return when (value) {
                entry.leadRole -> MemberPartAndRole(part, MemberRole.LEAD)
                entry.viceLeadRole -> MemberPartAndRole(part, MemberRole.VICE_LEAD)
                entry.member -> MemberPartAndRole(part, MemberRole.MEMBER)
                else -> continue
            }
        }

        return MemberPartAndRole(null, null)
    }

    fun resolveToString(part: Part, role: MemberRole): String? {
        for (entry in mappingData.partRoles) {
            if (entry.part != part.name) continue

            return when (role) {
                MemberRole.LEAD -> entry.leadRole
                MemberRole.VICE_LEAD -> entry.viceLeadRole
                MemberRole.MEMBER -> entry.member
            }
        }

        return null
    }
}
