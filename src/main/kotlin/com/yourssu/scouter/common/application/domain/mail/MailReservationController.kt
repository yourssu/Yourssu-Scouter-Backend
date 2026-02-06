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
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "메일")
@RestController
@RequestMapping("/api/mails/reservation")
class MailReservationController(
    private val mailService: MailService
) {

    @Operation(
        summary = "메일 전송 예약 (파일 첨부)",
        description = "인라인 이미지나 첨부파일이 있는 경우 multipart/form-data로 요청합니다.\n\n" +
                "- `request` 파트: JSON 형식의 예약 정보\n" +
                "- `inlineImages` 파트: 본문에 삽입할 이미지 파일 (선택)\n" +
                "- `attachments` 파트: 일반 첨부파일 (선택)"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "예약 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (bodyFormat 오류, multipart 파싱 실패 등)",
            content = [Content(schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class))]
        ),
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun reserveMailWithFiles(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestPart request: MailReserveRequest,
        @RequestPart(required = false) inlineImages: List<MultipartFile>?,
        @RequestPart(required = false) attachments: List<MultipartFile>?
    ): ResponseEntity<Unit> {
        val command: MailReserveCommand = request.toCommand(
            userId = authUserInfo.userId,
            request = request,
            inlineImages = inlineImages,
            attachments = attachments
        )
        mailService.reserveMail(command)

        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "메일 전송 예약 (파일 없음)",
        description = "첨부파일 없이 메일을 예약할 때 application/json으로 요청합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "예약 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (bodyFormat 오류 등)",
            content = [Content(schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class))]
        ),
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun reserveMailJson(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestBody request: MailReserveRequest,
    ): ResponseEntity<Unit> {
        val command: MailReserveCommand = request.toCommand(
            userId = authUserInfo.userId,
            request = request,
            inlineImages = null,
            attachments = null
        )
        mailService.reserveMail(command)

        return ResponseEntity.ok().build()
    }
}
