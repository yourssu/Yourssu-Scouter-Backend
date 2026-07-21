package com.yourssu.scouter.hrms.member.application

import com.yourssu.scouter.hrms.member.application.dto.MemberSyncResponse

import com.yourssu.scouter.hrms.member.business.dto.MemberSyncResult
import java.net.URI
import org.springframework.http.ResponseEntity

fun MemberSyncResult.toResponse(location: URI): ResponseEntity<MemberSyncResponse> {
    val response = MemberSyncResponse(this.failureMessages, this.createdCount)
    return if (this.createdCount > 0) {
        ResponseEntity.created(location).body(response)
    } else {
        ResponseEntity.ok(response)
    }
}
