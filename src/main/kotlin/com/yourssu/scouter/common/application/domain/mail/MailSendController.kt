package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.MailService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "메일")
@RestController
@RequestMapping("/api/mails/send")
class MailSendController(
    private val mailService: MailService,
) {
    @Operation(
        summary = "메일 즉시 발송",
        description =
            "메일을 즉시 발송합니다. DB에 저장하지 않고 바로 발송됩니다. " +
                "파일은 사전에 /api/mails/files/* API로 업로드/확정한 뒤, fileId 참조만 전달합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "발송 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (bodyFormat 오류, fileId 검증 실패 등)",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "502",
            description = "메일 발송 실패 (OAuth 토큰/네트워크 등)",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendMail(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestBody request: MailSendRequest,
    ): ResponseEntity<Unit> {
        val command = request.toCommand(authUserInfo.userId)
        mailService.sendMail(command)
        return ResponseEntity.ok().build()
    }
}
