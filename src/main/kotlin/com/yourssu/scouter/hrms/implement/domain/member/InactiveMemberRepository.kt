package com.yourssu.scouter.hrms.implement.domain.member

interface InactiveMemberRepository {

    fun save(inactiveMember: InactiveMember): InactiveMember
    fun findByMemberId(memberId: Long): InactiveMember?
    fun findAll(): List<InactiveMember>
    fun findAllByName(name: String): List<InactiveMember>
    fun findAllByNicknameKorean(nicknameKorean: String): List<InactiveMember>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<InactiveMember>
}
