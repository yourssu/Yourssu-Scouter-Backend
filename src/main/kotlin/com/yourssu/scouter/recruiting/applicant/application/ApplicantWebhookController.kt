package com.yourssu.scouter.recruiting.applicant.application

import com.yourssu.scouter.recruiting.applicant.application.dto.ApplicantSyncResponse
import com.yourssu.scouter.recruiting.applicant.application.dto.ApplicantWebhookRequest
import com.yourssu.scouter.recruiting.applicant.business.ApplicantSyncService
import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantSyncResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Tag(name = "리크루팅 지원자")
@Tag(name = "지원자 동기화 API")
@RestController
class ApplicantWebhookController(
    private val applicantSyncService: ApplicantSyncService,
) {

    private val applicantsLocation: URI = URI.create("/applicants")

    @Operation(
        summary = "구글폼 제출 웹훅 수신",
        description = "Google Apps Script가 폼 제출 시점에 직접 호출한다. JWT 대신 " +
            "${ApplicantWebhookSecretInterceptor.SECRET_HEADER} 헤더의 공유 시크릿으로 인증한다.",
    )
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants")
        ]
    )
    @PostMapping("/applicants/webhook")
    fun receiveWebhook(
        @RequestBody @Valid request: ApplicantWebhookRequest,
    ): ResponseEntity<ApplicantSyncResponse> {
        val result: ApplicantSyncResult = applicantSyncService.includeFromWebhook(request.toCommand())
        return result.toCreatedResponse(applicantsLocation, ApplicantSyncResponse::from)
    }
}
