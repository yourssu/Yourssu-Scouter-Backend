package com.yourssu.scouter.hrms.implement.domain.member

interface WithdrawnMemberRepository {

    fun findAll(): List<WithdrawnMember>
    fun findAllByName(name: String): List<WithdrawnMember>
    fun findAllByNicknameKorean(nicknameKorean: String): List<WithdrawnMember>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<WithdrawnMember>
}
