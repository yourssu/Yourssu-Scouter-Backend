package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaWithdrawnMemberRepository : JpaRepository<WithdrawnMemberEntity, Long> {

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
