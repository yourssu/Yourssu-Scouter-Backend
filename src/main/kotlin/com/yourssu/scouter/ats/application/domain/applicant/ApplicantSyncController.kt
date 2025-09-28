package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncResult
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncService
import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDateTime

@Tag(name = "리크루팅 지원자")
@Tag(name = "지원자 동기화 API")
@RestController
class ApplicantSyncController(
    private val applicantSyncService: ApplicantSyncService,
) {

    private val applicantsLocation: URI = URI.create("/applicants")

    @Operation(
        summary = "구글폼 응답을 지원자 목록에 업데이트",
        description = "현재 로그인 되어있는 사용자의 계정을 이용해 지원자의 구글폼 응답을 동기화 합니다."
    )
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants")
        ]
    )
    @PostMapping("/applicants/include-from-forms")
    fun includeFromForms(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<ApplicantSyncResponse> {
        val result: ApplicantSyncResult = applicantSyncService.includeFromForms(authUserInfo.userId)
        return result.toCreatedResponse(applicantsLocation, ApplicantSyncResponse::from)
    }

    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants")
        ]
    )
    @PostMapping("/applicants/include-from-forms/semesters/{semesterId}")
    fun includeFromFormsBySemesterAndPart(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable semesterId: Long,
    ): ResponseEntity<ApplicantSyncResponse> {
        val result: ApplicantSyncResult = applicantSyncService.includeFromForms(authUserInfo.userId, semesterId)
        return result.toCreatedResponse(applicantsLocation, ApplicantSyncResponse::from)
    }

    @Operation(summary = "마지막 동기화 시간 조회")
    @GetMapping("/applicants/lastUpdatedTime")
    fun lastSyncTime(): ResponseEntity<LastApplicantSyncTimeResponse> {
        val lastSyncTime: LocalDateTime? = applicantSyncService.readLastUpdatedTime()
        val response = LastApplicantSyncTimeResponse(lastSyncTime)

        return ResponseEntity.ok(response)
    }
}
