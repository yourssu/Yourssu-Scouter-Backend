package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData.MemberParseMappingEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MemberPartRoleResolverTest {

    private val headEntry = MemberParseMappingEntry(
        part = "Head lead",
        leadRole = "Yourssu Head Lead",
        viceLeadRole = "Yourssu Head Vice Lead",
        member = "",
    )

    private val financeEntry = MemberParseMappingEntry(
        part = "Finance",
        leadRole = "Financial Manager",
        viceLeadRole = "",
        member = "",
    )

    private val headPart = PartFixtureBuilder().name("Head lead").build()
    private val financePart = PartFixtureBuilder().name("Finance").build()
    private val parts = mapOf("Head lead" to headPart, "Finance" to financePart)

    private fun resolver(roleAliases: Map<String, String> = emptyMap()) = MemberPartRoleResolver(
        MemberParseMappingData(
            partRoles = arrayOf(headEntry, financeEntry),
            roleAliases = roleAliases,
        ),
    )

    @Test
    fun `셀 전체 별칭으로 YOURSSU LEAD VICE LEAD 복합 표기는 Head Vice Lead로 해석된다`() {
        val r = resolver(
            mapOf("YOURSSU LEAD / YOURSSU VICE LEAD" to "Yourssu Head Vice Lead"),
        )
        val result = r.toPartAndRoles("YOURSSU LEAD / YOURSSU VICE LEAD", parts)
        assertThat(result.isEmpty()).isFalse()
        assertThat(result.getRole()).isEqualTo(MemberRole.VICE_LEAD)
        assertThat(result.getParts()).containsExactly(headPart)
    }

    @Test
    fun `Finance 단독 표기는 Financial Manager 별칭으로 파트가 잡힌다`() {
        val r = resolver(mapOf("Finance" to "Financial Manager"))
        val result = r.toPartAndRoles("Finance", parts)
        assertThat(result.isEmpty()).isFalse()
        assertThat(result.getParts()).containsExactly(financePart)
        assertThat(result.getRole()).isEqualTo(MemberRole.LEAD)
    }
}
