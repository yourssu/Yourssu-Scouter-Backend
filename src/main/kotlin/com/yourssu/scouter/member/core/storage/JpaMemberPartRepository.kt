package com.yourssu.scouter.member.core.storage

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

    fun deleteAllByMemberId(id: Long)

    fun existsByMemberIdAndPartId(memberId: Long, partId: Long): Boolean

    fun findAllByMemberIdAndPartId(memberId: Long, partId: Long): List<MemberPartEntity>

    // member_part 에는 (member_id, part_id) 유니크 제약이 없다. 같은 파트가 두 행으로 들어가 있어도
    // 파트 개수는 하나로 세야 "마지막 파트" 판정이 맞는다.
    @Query("""
        SELECT COUNT(DISTINCT mp.part.id) FROM MemberPartEntity mp
        WHERE mp.member.id = :memberId
    """)
    fun countDistinctPartsByMemberId(memberId: Long): Long
}
