package com.yourssu.scouter.member.core.storage

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaGraduatedMemberRepository : JpaRepository<GraduatedMemberEntity, Long> {

    /** member·department를 한 번에 가져온다 (EAGER 연관이 엔티티마다 추가 SELECT로 나가는 N+1 방지). */
    @EntityGraph(attributePaths = ["member", "member.department"])
    override fun findAll(): MutableList<GraduatedMemberEntity>

    fun findByMemberId(memberId: Long): GraduatedMemberEntity?

    @Query("""
        SELECT gm FROM GraduatedMemberEntity gm 
        WHERE gm.member.name = :name
    """)
    fun findAllByName(name: String): List<GraduatedMemberEntity>

    @Query("""
        SELECT gm FROM GraduatedMemberEntity gm 
        WHERE gm.member.nicknameKorean = :nicknameKorean
    """)
    fun findAllByNicknameKorean(nicknameKorean: String): List<GraduatedMemberEntity>

    @Query("""
        SELECT gm FROM GraduatedMemberEntity gm 
        WHERE LOWER(gm.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<GraduatedMemberEntity>

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM GraduatedMemberEntity g WHERE g.member.id = :memberId")
    fun deleteByMemberId(memberId: Long)
}
