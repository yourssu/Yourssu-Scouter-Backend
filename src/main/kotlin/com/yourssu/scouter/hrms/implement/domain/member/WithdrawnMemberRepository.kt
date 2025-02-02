package com.yourssu.scouter.hrms.implement.domain.member

interface WithdrawnMemberRepository {

    fun save(withdrawnMember: WithdrawnMember): WithdrawnMember
    fun findAll(): List<WithdrawnMember>
    fun findAllByName(name: String): List<WithdrawnMember>
    fun findAllByNicknameKorean(nicknameKorean: String): List<WithdrawnMember>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<WithdrawnMember>
    fun deleteByMemberId(memberId: Long)
}
