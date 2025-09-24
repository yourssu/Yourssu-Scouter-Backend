package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.MemberSyncResult
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
