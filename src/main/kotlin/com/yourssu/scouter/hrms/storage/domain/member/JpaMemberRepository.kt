package com.yourssu.scouter.hrms.storage.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberRepository : JpaRepository<MemberEntity, Long> {

    fun existsByStudentId(studentId: String): Boolean
}
