package com.yourssu.scouter.member.core.storage

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaCompletedMemberRepository : JpaRepository<CompletedMemberEntity, Long> {

    /** member·department를 한 번에 가져온다 (EAGER 연관이 엔티티마다 추가 SELECT로 나가는 N+1 방지). */
    @EntityGraph(attributePaths = ["member", "member.department"])
    override fun findAll(): MutableList<CompletedMemberEntity>

    fun findByMemberId(memberId: Long): CompletedMemberEntity?

    @Query("""
        SELECT cm FROM CompletedMemberEntity cm 
        WHERE cm.member.name = :name
    """)
    fun findAllByName(name: String): List<CompletedMemberEntity>

    @Query("""
        SELECT cm FROM CompletedMemberEntity cm 
        WHERE cm.member.nicknameKorean = :nicknameKorean
    """)
    fun findAllByNicknameKorean(nicknameKorean: String): List<CompletedMemberEntity>

    @Query("""
        SELECT cm FROM CompletedMemberEntity cm 
        WHERE LOWER(cm.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<CompletedMemberEntity>

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CompletedMemberEntity c WHERE c.member.id = :memberId")
    fun deleteByMemberId(memberId: Long)
}
