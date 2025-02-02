package com.yourssu.scouter.hrms.implement.domain.member

interface GraduatedMemberRepository {

    fun save(graduatedMember: GraduatedMember): GraduatedMember
    fun findAll(): List<GraduatedMember>
    fun findAllByName(name: String): List<GraduatedMember>
    fun findAllByNicknameKorean(nicknameKorean: String): List<GraduatedMember>
    fun findAllByNicknameEnglish(nicknameEnglish: String): List<GraduatedMember>
}
