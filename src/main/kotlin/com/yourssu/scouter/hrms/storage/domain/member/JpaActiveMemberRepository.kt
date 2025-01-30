package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaActiveMemberRepository : JpaRepository<ActiveMemberEntity, Long> {

    @Query(
        """
        SELECT am FROM ActiveMemberEntity am 
        WHERE am.member.name = :name
    """
    )
    fun findAllByName(name: String): List<ActiveMemberEntity>

    @Query("""
        SELECT am FROM ActiveMemberEntity am 
        WHERE am.member.nicknameKorean = :nicknameKorean
    """)
    fun findAllByNicknameKoreanIgnoreCase(nicknameKorean: String): List<ActiveMemberEntity>

    @Query("""
        SELECT am FROM ActiveMemberEntity am 
        WHERE LOWER(am.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<ActiveMemberEntity>
}
