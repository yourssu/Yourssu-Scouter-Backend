package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData.MemberParseMappingEntry
import org.springframework.stereotype.Component

@Component
class MemberPartRoleResolver(
    private val mappingData: MemberParseMappingData,
) {

    private val roleAliasNormalized: Map<String, String> by lazy {
        mappingData.roleAliases.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
    }

    fun toPartAndRoles(
        roleCell: String,
        parts: Map<String, Part>,
        normalizedParts: Map<String, Part>? = null,
    ): MemberPartAndRoles {
        val result = mutableSetOf<MemberPartAndRole>()

        roleCell.split("/").forEach { value ->
            val raw = value.trim()
            val canonical = roleAliasNormalized[AliasMappingUtils.normalizeKey(raw)] ?: raw
            val partAndRole: MemberPartAndRole = resolveToPartAndRole(canonical, parts, normalizedParts)
            result.add(partAndRole)
        }

        return MemberPartAndRoles(result)
    }

    private fun resolveToPartAndRole(
        value: String,
        parts: Map<String, Part>,
        normalizedParts: Map<String, Part>?,
    ): MemberPartAndRole {
        for (entry: MemberParseMappingEntry in mappingData.partRoles) {
            val partKeyNormalized = AliasMappingUtils.normalizeKey(entry.part)
            val part = normalizedParts?.get(partKeyNormalized) ?: parts[entry.part] ?: continue

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
