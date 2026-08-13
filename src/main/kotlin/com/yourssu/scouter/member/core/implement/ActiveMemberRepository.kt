package com.yourssu.scouter.member.core.implement

interface ActiveMemberRepository {

    fun save(activeMember: ActiveMember): ActiveMember
    fun findByMemberId(memberId: Long): ActiveMember?
    fun findAll(): List<ActiveMember>
    fun findAllByName(name: String): List<ActiveMember>
    fun findAllByNicknameKorean(nicknameKorean: String): List<ActiveMember>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<ActiveMember>
    fun findAllByMemberIds(memberIds: List<Long>): List<ActiveMember>
    fun deleteByMemberId(memberId: Long)
}
