package com.yourssu.scouter.member.core.storage

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaInactiveMemberRepository : JpaRepository<InactiveMemberEntity, Long> {

    /** member·department를 한 번에 가져온다 (EAGER 연관이 엔티티마다 추가 SELECT로 나가는 N+1 방지). */
    @EntityGraph(attributePaths = ["member", "member.department"])
    override fun findAll(): MutableList<InactiveMemberEntity>

    fun findByMemberId(memberId: Long): InactiveMemberEntity?

    @Query("""
        SELECT im FROM InactiveMemberEntity im 
        WHERE im.member.name = :name
    """)
    fun findAllByName(name: String): List<InactiveMemberEntity>

    @Query("""
        SELECT im FROM InactiveMemberEntity im 
        WHERE im.member.nicknameKorean = :nicknameKorean
    """)
    fun findAllByNicknameKorean(nicknameKorean: String): List<InactiveMemberEntity>
    @Query("""
        SELECT im FROM InactiveMemberEntity im 
        WHERE LOWER(im.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<InactiveMemberEntity>

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM InactiveMemberEntity i WHERE i.member.id = :memberId")
    fun deleteByMemberId(memberId: Long)
}
