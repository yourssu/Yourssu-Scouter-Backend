package com.yourssu.scouter.member.core.storage

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaWithdrawnMemberRepository : JpaRepository<WithdrawnMemberEntity, Long> {

    /** member·department를 한 번에 가져온다 (EAGER 연관이 엔티티마다 추가 SELECT로 나가는 N+1 방지). */
    @EntityGraph(attributePaths = ["member", "member.department"])
    override fun findAll(): MutableList<WithdrawnMemberEntity>

    fun findByMemberId(memberId: Long): WithdrawnMemberEntity?

    @Query("""
        SELECT wm FROM WithdrawnMemberEntity wm 
        WHERE wm.member.name = :name
    """)
    fun findAllByName(name: String): List<WithdrawnMemberEntity>

    @Query("""
        SELECT wm FROM WithdrawnMemberEntity wm 
        WHERE wm.member.nicknameKorean = :nicknameKorean
    """)
    fun findAllByNicknameKorean(nicknameKorean: String): List<WithdrawnMemberEntity>

    @Query("""
        SELECT wm FROM WithdrawnMemberEntity wm 
        WHERE LOWER(wm.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<WithdrawnMemberEntity>

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM WithdrawnMemberEntity w WHERE w.member.id = :memberId")
    fun deleteByMemberId(memberId: Long)
}
