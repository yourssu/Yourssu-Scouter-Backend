package com.yourssu.scouter.hrms.implement.domain.member

interface ActiveMemberRepository {

    fun save(activeMember: ActiveMember): ActiveMember
    fun findByMemberId(memberId: Long): ActiveMember?
    fun findAll(): List<ActiveMember>
    fun findAllByName(name: String): List<ActiveMember>
    fun findAllByNicknameKorean(nicknameKorean: String): List<ActiveMember>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<ActiveMember>
}
