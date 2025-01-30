package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.hrms.storage.domain.member.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberRepository : JpaRepository<MemberEntity, Long> {
}
