package com.yourssu.scouter.member.core.implement

interface CompletedMemberRepository {

    fun save(completedMember: CompletedMember): CompletedMember
    fun findByMemberId(memberId: Long): CompletedMember?
    fun findAll(): List<CompletedMember>
    fun findAllByName(name: String): List<CompletedMember>
    fun findAllByNicknameKorean(nicknameKorean: String): List<CompletedMember>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<CompletedMember>
    fun deleteByMemberId(memberId: Long)
}
