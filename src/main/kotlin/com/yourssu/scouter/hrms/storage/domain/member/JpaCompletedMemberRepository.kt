package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaCompletedMemberRepository : JpaRepository<CompletedMemberEntity, Long> {

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
    fun findAllByNicknameKoreanIgnoreCase(nicknameKorean: String): List<CompletedMemberEntity>

    @Query("""
        SELECT cm FROM CompletedMemberEntity cm 
        WHERE LOWER(cm.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<CompletedMemberEntity>

    fun deleteByMemberId(memberId: Long)
}
