package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.business.domain.member.MemberSyncResult
import com.yourssu.scouter.hrms.business.domain.member.MemberSyncService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Instant

@Tag(name = "유어슈 멤버")
@Tag(name = "합격자 동기화 API")
@RestController
class MemberSyncController(
    private val memberSyncService: MemberSyncService,
) {

    private val membersLocation: URI = URI.create("/members")

    @Operation(summary = "지원자 중 합격자 멤버 동기화", description = "리크루팅 지원자 중 합격자를 멤버에 동기화 합니다.")
    @ApiResponse(description = "OK", responseCode = "200")
    @ApiResponse(description = "CREATED", responseCode = "201", headers = [
        Header(name = "Location", description = "/members")
    ])
    @PostMapping("/members/include-from-applicants")
    fun includeFromApplicants(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<MemberSyncResponse> {
        val result: MemberSyncResult = memberSyncService.includeAcceptedApplicants(authUserInfo.userId)
        return result.toResponse(membersLocation)
    }

    @Operation(
        summary = "지정 학기 합격자 멤버 동기화",
        description = "지정한 학기(semesterString, yy-term 예: 25-1)의 합격자만 멤버에 동기화합니다. 없으면 전체 동기화와 동일.",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "CREATED", responseCode = "201", headers = [
                Header(name = "Location", description = "/members")
            ]),
        ],
    )
    @PostMapping("/members/include-from-applicants/{semesterString}")
    fun includeFromApplicants(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "학기 문자열 (yy-term, 예: 25-1)") @PathVariable semesterString: String,
    ): ResponseEntity<MemberSyncResponse> {
        val result: MemberSyncResult =
            memberSyncService.includeAcceptedApplicants(authUserInfo.userId, semesterString)
        return result.toResponse(membersLocation)
    }

    @Operation(summary = "마지막 동기화 시간 조회", description = "유어슈 멤버의 마지막 동기화 시간을 조회합니다.")
    @GetMapping("/members/lastUpdatedTime")
    fun lastSyncTime(): ResponseEntity<LastMemberSyncTimeResponse> {
        val lastSyncTime: Instant? = memberSyncService.readLastUpdatedTime()
        val response = LastMemberSyncTimeResponse(lastSyncTime)

        return ResponseEntity.ok(response)
    }
}
