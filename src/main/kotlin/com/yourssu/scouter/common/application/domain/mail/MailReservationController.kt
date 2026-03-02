package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.MailReserveCommand
import com.yourssu.scouter.common.business.domain.mail.MailService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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

    @Operation(
        summary = "예약 메일 발송 상태 조회",
        description = "현재 사용자의 미발송 예약 목록과 발송 실패 사유를 조회합니다. failureErrorCode가 OAuth-Token-Refresh-Fail이면 재로그인 후 다음 스케줄 실행 시 자동 발송됩니다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/status")
    fun getReservationStatus(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<MailReservationStatusResponse> {
        val statuses = mailService.getPendingReservationStatuses(authUserInfo.userId)
        return ResponseEntity.ok(MailReservationStatusResponse.from(statuses))
    }

    @Operation(
        summary = "예약 메일 목록 조회",
        description = "현재 사용자가 예약한 메일 중 아직 발송되지 않은 모든 예약 목록을 조회합니다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getReservations(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<MailReservationListResponse> {
        val details = mailService.getUserMailReservations(authUserInfo.userId)
        return ResponseEntity.ok(MailReservationListResponse.from(details))
    }

    @Operation(
        summary = "예약 메일 단건 조회",
        description = "예약 ID에 해당하는 메일 예약 상세를 조회합니다. 다른 사용자의 예약에는 접근할 수 없습니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(
            responseCode = "404",
            description = "예약을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "403",
            description = "다른 사용자의 예약에 대한 접근 거부",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
    )
    @GetMapping("/{reservationId}")
    fun getReservationDetail(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable reservationId: Long,
    ): ResponseEntity<MailReservationDetailResponse> {
        val detail = mailService.getUserMailReservation(authUserInfo.userId, reservationId)
        return ResponseEntity.ok(MailReservationDetailResponse.from(detail))
    }

    @Operation(
        summary = "예약 메일 수정",
        description = "기존 예약 메일의 발송 시각과 내용을 수정합니다. 요청 바디는 생성과 동일한 MailReserveRequest 형식을 사용하며, 전체를 교체합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(
            responseCode = "404",
            description = "예약을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "403",
            description = "다른 사용자의 예약에 대한 접근 거부",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
    )
    @PutMapping("/{reservationId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateReservation(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable reservationId: Long,
        @RequestBody request: MailReserveRequest,
    ): ResponseEntity<Unit> {
        val command: MailReserveCommand = request.toCommand(authUserInfo.userId)
        mailService.updateMailReservation(authUserInfo.userId, reservationId, command)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "예약 메일 취소",
        description = "아직 발송되지 않은 예약 메일을 취소합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "취소 성공"),
        ApiResponse(
            responseCode = "404",
            description = "예약을 찾을 수 없음",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "403",
            description = "다른 사용자의 예약에 대한 접근 거부",
            content = [
                Content(
                    schema = Schema(implementation = com.yourssu.scouter.common.application.support.exception.ExceptionResponse::class),
                ),
            ],
        ),
    )
    @DeleteMapping("/{reservationId}")
    fun cancelReservation(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable reservationId: Long,
    ): ResponseEntity<Unit> {
        mailService.cancelMailReservation(authUserInfo.userId, reservationId)
        return ResponseEntity.ok().build()
    }
}
