package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberRepository : JpaRepository<MemberEntity, Long> {

    fun findAllByName(name: String): List<MemberEntity>
    fun findAllByNicknameKoreanIgnoreCase(nicknameKorean: String): List<MemberEntity>
    fun findAllByNicknameEnglishIgnoreCase
                (nicknameEnglish: String): List<MemberEntity>
}
