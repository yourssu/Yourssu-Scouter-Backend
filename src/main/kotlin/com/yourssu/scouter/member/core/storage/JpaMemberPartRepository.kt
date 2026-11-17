package com.yourssu.scouter.member.core.storage

import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaMemberPartRepository : JpaRepository<MemberPartEntity, Long> {

    @Query("""
        SELECT mp.part FROM MemberPartEntity mp 
        WHERE mp.member.id = :memberId
    """)
    fun findAllPartsByMemberId(memberId: Long): List<PartEntity>

    @Query("""
        SELECT mp.member.id FROM MemberPartEntity mp
        WHERE mp.part.id IN :partIds
    """)
    fun findAllMemberIdsByPartIds(partIds: Set<Long>): List<Long>

    @Query("""
        SELECT mp FROM MemberPartEntity mp
        JOIN FETCH mp.part
        WHERE mp.member.id IN :memberIds
    """)
    fun findAllWithPartByMemberIds(memberIds: Collection<Long>): List<MemberPartEntity>

    fun deleteAllByMemberId(id: Long)
}

/** 여러 멤버의 파트를 쿼리 1번으로 읽어 memberId별로 묶는다 (멤버마다 조회하는 N+1 방지). */
internal fun JpaMemberPartRepository.findPartsGroupedByMemberId(memberIds: Collection<Long>): Map<Long, List<Part>> {
    if (memberIds.isEmpty()) {
        return emptyMap()
    }

    return findAllWithPartByMemberIds(memberIds)
        .groupBy({ it.member.id!! }, { it.part.toDomain() })
}
