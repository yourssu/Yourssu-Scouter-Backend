package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.business.domain.member.MemberSyncResult
import com.yourssu.scouter.hrms.business.domain.member.MemberSyncService
import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberSyncController(
    private val memberSyncService: MemberSyncService,
) {

    @PostMapping("/members/include-from-applicants")
    fun includeFromApplicants(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<MemberSyncResponse> {
        val result: MemberSyncResult = memberSyncService.includeAcceptedApplicants(authUserInfo.userId)
        val response = MemberSyncResponse(result.failureMessages)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/members/include-from-applicants/{semesterString}")
    fun includeFromApplicants(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable semesterString: String,
    ): ResponseEntity<MemberSyncResponse> {
        val result: MemberSyncResult =
            memberSyncService.includeAcceptedApplicants(authUserInfo.userId, semesterString)
        val response = MemberSyncResponse(result.failureMessages)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/members/lastUpdatedTime")
    fun lastSyncTime(): ResponseEntity<LastMemberSyncTimeResponse> {
        val lastSyncTime: LocalDateTime? = memberSyncService.readLastUpdatedTime()
        val response = LastMemberSyncTimeResponse(lastSyncTime)

        return ResponseEntity.ok(response)
    }
}
