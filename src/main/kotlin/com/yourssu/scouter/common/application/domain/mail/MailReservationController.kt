package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.MailService
import com.yourssu.scouter.common.implement.domain.mail.MailReserveCommand
import io.swagger.v3.oas.annotations.Operation
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

    @Operation(summary = "메일 전송 예약 (파일 첨부)")
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

    @Operation(summary = "메일 전송 예약 (파일 없음)")
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
