package com.yourssu.scouter.hrms.member.storage

import com.yourssu.scouter.common.part.storage.PartEntity
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

    fun deleteAllByMemberId(id: Long)
}
