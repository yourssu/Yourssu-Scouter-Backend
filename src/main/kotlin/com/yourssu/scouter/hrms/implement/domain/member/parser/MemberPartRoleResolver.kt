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

    private fun normalizeKey(value: String): String {
        return value.lowercase().replace(" ", "").replace("-", "")
    }

    private val aliasToCanonical: Map<String, String> = mapOf(

        // Head lead (member)
        normalizeKey("Yourssu Head Leader") to "Yourssu Head Lead",

        // PM (member)
        normalizeKey("PM") to "Product Manager",
        normalizeKey("Product Mananger") to "Product Manager",

        // PM Lead
        normalizeKey("PM Lead") to "Product Manager Lead",
        normalizeKey("Product Mananger Lead") to "Product Manager Lead",

        // PM Vice Lead
        normalizeKey("PM Vice Lead") to "Product Manager Vice Lead",
        normalizeKey("Product Mananger Vice Lead") to "Product Manager Vice Lead",

        // Legal (member)
        normalizeKey("Legal Partner") to "Legal Officer",
        normalizeKey("Legal") to "Legal Officer",

        // Legal Lead
        normalizeKey("Legal Leader") to "Legal Lead",

        // HR (member)
        normalizeKey("HR Manager") to "HR Partner",
        normalizeKey("HR") to "HR Partner",

        // Marketing (member)
        normalizeKey("Marketing") to "Marketer",

        // Backend (member)
        normalizeKey("Backend") to "Backend Engineer",

        // Web FE (member)
        normalizeKey("Web-Frontend") to "Web FE Engineer",
        normalizeKey("Web-Frontend Engineer") to "Web FE Engineer",

        // Android (member)
        normalizeKey("Android") to "Android Engineer",

        // iOS (member)
        normalizeKey("iOS") to "iOS Engineer",

        // Prd-Designer (member)
        normalizeKey("Prd-Design") to "Product Designer",
    )

    fun toPartAndRoles(roleCell: String, parts: Map<String, Part>): MemberPartAndRoles {
        val result = mutableSetOf<MemberPartAndRole>()

        roleCell.split("/").forEach { value ->
            val raw = value.trim()
            val canonical = aliasToCanonical[normalizeKey(raw)] ?: raw
            val partAndRole: MemberPartAndRole = resolveToPartAndRole(canonical, parts)
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
