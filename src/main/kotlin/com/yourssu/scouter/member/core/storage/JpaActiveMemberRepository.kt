package com.yourssu.scouter.member.core.storage

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaActiveMemberRepository : JpaRepository<ActiveMemberEntity, Long> {

    /** member·department를 한 번에 가져온다 (EAGER 연관이 엔티티마다 추가 SELECT로 나가는 N+1 방지). */
    @EntityGraph(attributePaths = ["member", "member.department"])
    override fun findAll(): MutableList<ActiveMemberEntity>

    @Query("""
        SELECT am FROM ActiveMemberEntity am 
        WHERE am.member.name = :name
    """)
    fun findAllByName(name: String): List<ActiveMemberEntity>

    @Query("""
        SELECT am FROM ActiveMemberEntity am 
        WHERE am.member.nicknameKorean = :nicknameKorean
    """)
    fun findAllByNicknameKorean(nicknameKorean: String): List<ActiveMemberEntity>

    @Query("""
        SELECT am FROM ActiveMemberEntity am 
        WHERE LOWER(am.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<ActiveMemberEntity>

    @Query("""
        SELECT am FROM ActiveMemberEntity am
        WHERE am.member.id IN :memberIds
    """)
    fun findAllByMemberIds(memberIds: List<Long>): List<ActiveMemberEntity>

    fun findByMemberId(memberId: Long): ActiveMemberEntity?

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ActiveMemberEntity a WHERE a.member.id = :memberId")
    fun deleteByMemberId(memberId: Long)
}
