package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.MailService
import com.yourssu.scouter.common.implement.domain.mail.MailReserveCommand
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
@RequestMapping("/api/mails/reservation")
class MailReservationController(
    private val mailService: MailService,
) {
    @Operation(
        summary = "메일 전송 예약",
        description =
            "application/json으로 요청합니다. 파일은 사전에 /api/mails/files/* API로 업로드/확정한 뒤, 예약 시 fileId 참조만 전달합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "예약 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (bodyFormat 오류, fileId 검증 실패 등)",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun reserveMail(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestBody request: MailReserveRequest,
    ): ResponseEntity<Unit> {
        val command: MailReserveCommand = request.toCommand(authUserInfo.userId)
        mailService.reserveMail(command)
        return ResponseEntity.ok().build()
    }
}
