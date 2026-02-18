package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaInactiveMemberRepository : JpaRepository<InactiveMemberEntity, Long> {

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
    fun findAllByNicknameKoreanIgnoreCase(nicknameKorean: String): List<InactiveMemberEntity>
    @Query("""
        SELECT im FROM InactiveMemberEntity im 
        WHERE LOWER(im.member.nicknameEnglish) = LOWER(:nicknameEnglish)
    """)
    fun findAllByNicknameEnglishIgnoreCase(nicknameEnglish: String): List<InactiveMemberEntity>

    fun deleteByMemberId(memberId: Long)
}
