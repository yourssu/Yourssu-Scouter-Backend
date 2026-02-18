package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberRepository : JpaRepository<MemberEntity, Long> {

    fun findByStudentId(studentId: String): MemberEntity?
    fun findByEmail(email: String): MemberEntity?
    fun existsByStudentId(studentId: String): Boolean
}
