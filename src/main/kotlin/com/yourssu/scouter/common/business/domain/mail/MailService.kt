package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReserveCommand
import com.yourssu.scouter.common.implement.domain.mail.MailWriter
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserReader
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailWriter: MailWriter,
    private val userReader: UserReader,
    private val mailReservationRepository: MailReservationRepository,
    private val mailRepository: MailRepository,
    private val oauth2Service: OAuth2Service,
    private val mailSender: MailSender,
) {

    fun reserveMail(command: MailReserveCommand) {
        val sender: User = userReader.readById(command.senderUserId)
        val mail: Mail = command.toMail(sender.getEmailAddress())

        mailWriter.reserve(mail, command.reservationTime)
    }

    fun sendReservedMails() {
        val now = LocalDateTime.now()
        val reservations = mailReservationRepository.findAllByReservationTimeLessThanEqual(now)
        for (reservation in reservations) {
            try {
                val mail = mailRepository.findById(reservation.mailId) ?: continue
                val user = userReader.findByEmail(mail.senderEmailAddress) ?: continue
                val refreshedUser = oauth2Service.refreshOAuth2TokenBeforeExpiry(user.id!!, OAuth2Type.GOOGLE, 10L)
                val accessToken = refreshedUser.getBearerAccessToken()
                mailSender.send(MailData.from(mail), accessToken)
                mailReservationRepository.deleteById(reservation.id!!)
            } catch (e: Exception) {
                LoggerFactory.getLogger(MailService::class.java).error("예약 메일 발송 실패: ", e)
            }
        }
    }
}
