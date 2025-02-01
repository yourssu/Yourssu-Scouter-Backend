package com.yourssu.scouter.hrms.implement.domain.member

interface MemberRepository {

    fun save(member: Member): Member
}
