package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReservationReader
import com.yourssu.scouter.common.implement.domain.mail.MailReservationWriter
import com.yourssu.scouter.common.implement.domain.mail.MailReserveCommand
import com.yourssu.scouter.common.implement.domain.mail.MailWriter
import com.yourssu.scouter.common.implement.domain.user.UserReader
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailWriter: MailWriter,
    private val userReader: UserReader,
    private val mailReservationReader: MailReservationReader,
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
        val sender = userReader.readById(command.senderUserId)
        val mail: Mail = command.toMail(sender.getEmailAddress())

        mailWriter.reserve(mail, command.reservationTime)
    }

    fun sendReservedMails() {
        val now = LocalDateTime.now()
        val reservations = mailReservationReader.readAllBefore(now)
        for (reservation in reservations) {
            try {
                val mail = mailRepository.findById(reservation.mailId)
                if (mail == null) {
                    log.warn("예약 메일의 원본을 찾을 수 없어 삭제합니다: mailId={}", reservation.mailId)
                    mailReservationWriter.delete(reservation)
                    continue
                }
                val user = userReader.findByEmail(mail.senderEmailAddress)
                if (user == null) {
                    log.warn("발신자를 찾을 수 없어 예약을 삭제합니다: email={}", mail.senderEmailAddress)
                    mailReservationWriter.delete(reservation)
                    continue
                }
                val refreshedUser = oauth2Service.refreshOAuth2TokenBeforeExpiry(user.id!!, OAuth2Type.GOOGLE, 10L)
                val accessToken = refreshedUser.getBearerAccessToken()
                mailSender.send(MailData.from(mail), accessToken)
                mailReservationWriter.delete(reservation)
            } catch (e: Exception) {
                log.error("예약 메일 발송 실패: mailId={}", reservation.mailId, e)
                if (reservation.reservationTime.plusHours(MAX_RETRY_HOURS).isBefore(now)) {
                    log.error("최대 재시도 기간({}시간) 초과로 예약 삭제: mailId={}", MAX_RETRY_HOURS, reservation.mailId)
                    mailReservationWriter.delete(reservation)
                }
            }
        }
    }
}
