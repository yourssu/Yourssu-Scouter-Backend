package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.MailReservationReader
import com.yourssu.scouter.common.implement.domain.mail.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import com.yourssu.scouter.common.implement.domain.mail.MailReservationWriter
import com.yourssu.scouter.common.implement.support.exception.MailFailedException
import com.yourssu.scouter.common.implement.support.exception.MailReservationAccessDeniedException
import com.yourssu.scouter.common.implement.support.exception.MailReservationNotFoundException
import com.yourssu.scouter.common.implement.support.exception.CustomException
import com.yourssu.scouter.common.implement.domain.mail.MailWriter
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.support.exception.MailReservationAlreadyProcessedException
import com.yourssu.scouter.common.implement.support.exception.MailReservationNotYetDueException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class MailService(
    private val mailWriter: MailWriter,
    private val mailFileService: MailFileService,
    private val userReader: UserReader,
    private val mailReservationReader: MailReservationReader,
    private val mailReservationRepository: MailReservationRepository,
    private val mailReservationWriter: MailReservationWriter,
    private val mailRepository: MailRepository,
    private val oauth2Service: OAuth2Service,
    private val mailSender: MailSender,
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailService::class.java)
        private const val MAX_RETRY_HOURS = 24L
    }

    fun reserveMail(command: MailReserveCommand) {
        log.info("메일 예약 등록 요청: reservationTime={}, senderUserId={}", command.reservationTime, command.senderUserId)
        val sender = userReader.readById(command.senderUserId)
        val resolvedCommand =
            command.copy(
                attachmentReferences = mailFileService.resolveAttachmentReferences(command.senderUserId, command.attachmentReferences),
            )
        val mail: Mail = resolvedCommand.toMail(sender.getEmailAddress())

        mailWriter.reserve(mail, resolvedCommand.reservationTime)
        log.info("메일 예약 등록 완료: reservationTime={}, senderUserId={}", command.reservationTime, command.senderUserId)
    }

    /**
     * 현재 사용자의 미발송 예약 목록을 조회한다.
     * 각 예약에 대해 기존 OAuth2 토큰 갱신 로직을 호출해, 갱신 실패 시 동일한 [CustomException]의 errorCode를
     * 응답에만 담아 반환한다. (DB에 실패 정보를 저장하지 않음)
     */
    fun getPendingReservationStatuses(userId: Long): List<PendingMailReservationStatus> {
        val user = userReader.readById(userId)
        val now = Instant.now()
        val senderEmail = user.getEmailAddress()
        val reservations = mailReservationReader.readAllBeforeBySenderEmail(now, senderEmail)
        return reservations.map { reservation ->
            val mail = mailRepository.findById(reservation.mailId) ?: run {
                log.warn("예약의 메일을 찾을 수 없음: reservationId={}, mailId={}", reservation.id, reservation.mailId)
                return@map PendingMailReservationStatus(
                    reservationId = reservation.id!!,
                    mailId = reservation.mailId,
                    reservationTime = reservation.reservationTime,
                    failureErrorCode = null,
                    failedAt = null,
                )
            }
            val (failureErrorCode, failedAt) = try {
                oauth2Service.refreshOAuth2TokenBeforeExpiry(user.id!!, OAuth2Type.GOOGLE, 10L)
                null to null
            } catch (e: CustomException) {
                e.errorCode to now
            }
            PendingMailReservationStatus(
                reservationId = reservation.id!!,
                mailId = reservation.mailId,
                reservationTime = reservation.reservationTime,
                failureErrorCode = failureErrorCode,
                failedAt = failedAt,
            )
        }
    }

    fun sendReservedMails() {
        val now = Instant.now()
        val reservations = mailReservationReader.readAllPendingBefore(now)
        log.info("예약 메일 처리 시작: 기준시각={}, 발송대상건수={}", now, reservations.size)
        for (reservation in reservations) {
            val delaySeconds = java.time.Duration.between(reservation.reservationTime, now).seconds
            log.info("예약 메일 처리 시작: reservationId={}, mailId={}, reservationTime={}, 현재시각={}, 지연시간={}초",
                reservation.id, reservation.mailId, reservation.reservationTime, now, delaySeconds)
            val sent = trySendReservation(reservation, now)
            if (!sent && reservation.reservationTime.plus(MAX_RETRY_HOURS, ChronoUnit.HOURS).isBefore(now)) {
                log.error("최대 재시도 기간({}시간) 초과로 예약 삭제: reservationId={}, mailId={}", MAX_RETRY_HOURS, reservation.id, reservation.mailId)
                mailReservationWriter.delete(reservation)
            }
        }
    }

    /**
     * 단일 예약 메일을 즉시 발송 시도한다.
     * 성공 시 status를 SENT로 업데이트, 실패 시 SCHEDULED면 PENDING_SEND로 전환한다.
     * @return 발송 성공 여부
     */
    fun retryReservation(userId: Long, reservationId: Long) {
        val user = userReader.readById(userId)
        val reservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        val mail =
            mailRepository.findById(reservation.mailId)
                ?: throw MailReservationNotFoundException("예약 메일을 찾을 수 없습니다. reservationId=$reservationId, mailId=${reservation.mailId}")

        val senderEmail = user.getEmailAddress()
        if (mail.senderEmailAddress != senderEmail) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        if (reservation.status == MailReservationStatus.SENT) {
            throw MailReservationAlreadyProcessedException(
                "이미 발송된 메일은 재전송할 수 없습니다. reservationId=$reservationId",
            )
        }

        val now = Instant.now()
        if (now.isBefore(reservation.reservationTime)) {
            throw MailReservationNotYetDueException(
                "예약 시간이 지나지 않은 메일은 재전송할 수 없습니다. reservationId=$reservationId, reservationTime=${reservation.reservationTime}",
            )
        }

        val sent = trySendReservation(reservation, now)
        if (!sent) {
            throw MailFailedException(
                "메일 발송에 실패했습니다. OAuth 토큰 갱신 또는 네트워크 상태를 확인해 주세요. reservationId=$reservationId",
            )
        }
    }

    private fun trySendReservation(
        reservation: MailReservation,
        now: Instant,
    ): Boolean {
        if (reservation.status == MailReservationStatus.SENT) {
            log.warn("이미 발송된 예약에 대한 발송 시도 무시: reservationId={}", reservation.id)
            return false
        }
        return try {
            val mail = mailRepository.findById(reservation.mailId)
            if (mail == null) {
                log.warn("예약 메일의 원본을 찾을 수 없어 삭제합니다: reservationId={}, mailId={}", reservation.id, reservation.mailId)
                mailReservationWriter.delete(reservation)
                return@trySendReservation false
            }
            val user = userReader.findByEmail(mail.senderEmailAddress)
            if (user == null) {
                log.warn("발신자를 찾을 수 없어 예약메일을 삭제합니다: reservationId={}, mailId={}", reservation.id, reservation.mailId)
                mailReservationWriter.delete(reservation)
                return@trySendReservation false
            }
            log.debug("토큰 갱신 시도: reservationId={}, userId={}", reservation.id, user.id)
            val refreshedUser = oauth2Service.refreshOAuth2TokenBeforeExpiry(user.id!!, OAuth2Type.GOOGLE, 10L)
            val accessToken = refreshedUser.getBearerAccessToken()
            mailSender.send(MailData.from(mail), accessToken)
            mailReservationRepository.save(reservation.copy(status = MailReservationStatus.SENT))
            log.info("예약 메일 발송 완료: reservationId={}, mailId={}", reservation.id, reservation.mailId)
            true
        } catch (e: Exception) {
            log.error("예약 메일 발송 실패: reservationId={}, mailId={}, exception={}", reservation.id, reservation.mailId, e.javaClass.simpleName, e)
            if (reservation.status == MailReservationStatus.SCHEDULED) {
                mailReservationRepository.save(reservation.copy(status = MailReservationStatus.PENDING_SEND))
            }
            false
        }
    }

    fun getUserMailReservations(userId: Long): List<MailReservationDetail> {
        val user = userReader.readById(userId)
        val senderEmail = user.getEmailAddress()
        val reservations = mailReservationReader.readAllBySenderEmail(senderEmail)
        return reservations.map { reservation ->
            val mail =
                mailRepository.findById(reservation.mailId)
                    ?: throw MailReservationNotFoundException(
                        "예약 메일을 찾을 수 없습니다. reservationId=${reservation.id}, mailId=${reservation.mailId}",
                    )
            toDetail(reservation, mail)
        }
    }

    fun getUserMailReservation(
        userId: Long,
        reservationId: Long,
    ): MailReservationDetail {
        val user = userReader.readById(userId)
        val reservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        val mail =
            mailRepository.findById(reservation.mailId)
                ?: throw MailReservationNotFoundException("예약 메일을 찾을 수 없습니다. reservationId=$reservationId, mailId=${reservation.mailId}")

        val senderEmail = user.getEmailAddress()
        if (mail.senderEmailAddress != senderEmail) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        return toDetail(reservation, mail)
    }

    fun updateMailReservation(
        userId: Long,
        reservationId: Long,
        command: MailReserveCommand,
    ) {
        val user = userReader.readById(userId)
        val existingReservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        val existingMail =
            mailRepository.findById(existingReservation.mailId)
                ?: throw MailReservationNotFoundException("예약 메일을 찾을 수 없습니다. reservationId=$reservationId, mailId=${existingReservation.mailId}")

        val senderEmail = user.getEmailAddress()
        if (existingMail.senderEmailAddress != senderEmail) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        if (existingReservation.status == MailReservationStatus.SENT) {
            throw MailReservationAlreadyProcessedException(
                "이미 발송된 메일은 수정할 수 없습니다. reservationId=$reservationId",
            )
        }
        val now = Instant.now()
        if (!now.isBefore(existingReservation.reservationTime)) {
            throw MailReservationAlreadyProcessedException(
                "예약 시간이 지난 메일은 수정할 수 없습니다. reservationId=$reservationId, reservationTime=${existingReservation.reservationTime}",
            )
        }

        val resolvedCommand =
            command.copy(
                attachmentReferences = mailFileService.resolveAttachmentReferences(userId, command.attachmentReferences),
            )

        val updatedMail =
            Mail(
                id = existingMail.id,
                senderEmailAddress = existingMail.senderEmailAddress,
                receiverEmailAddresses = resolvedCommand.receiverEmailAddresses,
                ccEmailAddresses = resolvedCommand.ccEmailAddresses,
                bccEmailAddresses = resolvedCommand.bccEmailAddresses,
                mailSubject = resolvedCommand.mailSubject,
                mailBody = resolvedCommand.mailBody,
                bodyFormat = resolvedCommand.bodyFormat,
                attachmentReferences = resolvedCommand.attachmentReferences,
            )

        mailRepository.save(updatedMail)

        val updatedReservation = existingReservation.copy(reservationTime = resolvedCommand.reservationTime)
        mailReservationRepository.save(updatedReservation)
    }

    fun cancelMailReservation(
        userId: Long,
        reservationId: Long,
    ) {
        val user = userReader.readById(userId)
        val reservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        val mail =
            mailRepository.findById(reservation.mailId)
                ?: throw MailReservationNotFoundException("예약 메일을 찾을 수 없습니다. reservationId=$reservationId, mailId=${reservation.mailId}")

        val senderEmail = user.getEmailAddress()
        if (mail.senderEmailAddress != senderEmail) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        if (reservation.status == MailReservationStatus.SENT) {
            throw MailReservationAlreadyProcessedException(
                "이미 발송된 메일은 취소할 수 없습니다. reservationId=$reservationId",
            )
        }
        val now = Instant.now()
        if (!now.isBefore(reservation.reservationTime)) {
            throw MailReservationAlreadyProcessedException(
                "예약 시간이 지난 메일은 취소할 수 없습니다. reservationId=$reservationId, reservationTime=${reservation.reservationTime}",
            )
        }

        mailReservationWriter.delete(reservation)
    }

    private fun toDetail(
        reservation: com.yourssu.scouter.common.implement.domain.mail.MailReservation,
        mail: Mail,
    ): MailReservationDetail {
        return MailReservationDetail(
            reservationId = reservation.id!!,
            mailId = reservation.mailId,
            reservationTime = reservation.reservationTime,
            status = reservation.status,
            senderEmailAddress = mail.senderEmailAddress,
            receiverEmailAddresses = mail.receiverEmailAddresses,
            ccEmailAddresses = mail.ccEmailAddresses,
            bccEmailAddresses = mail.bccEmailAddresses,
            mailSubject = mail.mailSubject,
            mailBody = mail.mailBody,
            bodyFormat = mail.bodyFormat,
            hasAttachments = mail.attachments.isNotEmpty(),
        )
    }
}
