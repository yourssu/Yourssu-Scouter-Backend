package com.yourssu.scouter.hrms.implement.support

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "member-parse-mapping-data")
class MemberParseMappingData(
    val partRoles: Array<MemberParseMappingEntry>,
    val departmentAliases: Map<String, String> = emptyMap(),
    val roleAliases: Map<String, String> = emptyMap(),
) {

    data class MemberParseMappingEntry(
        val part: String,
        val leadRole: String,
        val viceLeadRole: String,
        val member: String
    )
}
