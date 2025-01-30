package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.storage.domain.part.PartEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaMemberPartRepository : JpaRepository<MemberPartEntity, Long> {

    @Query("""
        SELECT mp.part FROM MemberPartEntity mp 
        WHERE mp.member.id = :memberId
    """)
    fun findAllPartsByMemberId(memberId: Long): List<PartEntity>
}
