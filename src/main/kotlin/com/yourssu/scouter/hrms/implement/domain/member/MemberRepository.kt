package com.yourssu.scouter.hrms.implement.domain.member

interface MemberRepository {

    fun save(member: Member): Member
    fun findById(memberId: Long): Member?
    fun findAll(): List<Member>
    fun findAllByState(state: MemberState): List<Member>
    fun findAllByName(name: String): List<Member>
    fun findAllByNicknameKorean(nicknameKorean: String): List<Member>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<Member>
    fun deleteById(memberId: Long)
}
