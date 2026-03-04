package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaGraduatedMemberRepository : JpaRepository<GraduatedMemberEntity, Long> {

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
