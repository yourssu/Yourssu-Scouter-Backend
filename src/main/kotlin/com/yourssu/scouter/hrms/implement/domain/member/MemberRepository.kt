package com.yourssu.scouter.hrms.implement.domain.member

interface MemberRepository {

    fun save(member: Member): Member
    fun findById(memberId: Long): Member?
    fun findAll(): List<Member>
    fun deleteById(memberId: Long)
}
