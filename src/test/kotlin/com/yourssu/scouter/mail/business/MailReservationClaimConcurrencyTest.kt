package com.yourssu.scouter.mail.business

import com.yourssu.scouter.mail.implement.reservation.MailReservation
import com.yourssu.scouter.mail.implement.reservation.MailReservationRepository
import com.yourssu.scouter.mail.implement.reservation.MailReservationStatus
import com.yourssu.scouter.mail.implement.reservation.MailReservationWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Collections

@SpringBootTest
@ActiveProfiles("test")
@Suppress("NonAsciiCharacters")
class MailReservationClaimConcurrencyTest {
    @Autowired
    lateinit var mailReservationRepository: MailReservationRepository

    @Autowired
    lateinit var mailReservationWriter: MailReservationWriter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `동시에 claim 해도 한 워커만 SENDING을 가져온다`() {
        var reservationId: Long? = null
        try {
            val reservation =
                mailReservationRepository.save(
                    MailReservation(
                        reservedByUserId = null,
                        receiverEmailAddress = "to@example.com",
                        mailSubject = "claim-test",
                        mailBody = "body",
                        bodyFormat = MailBodyFormat.HTML,
                        reservationTime = Instant.now().minusSeconds(30),
                        status = MailReservationStatus.SCHEDULED,
                    ),
                )
            val id = reservation.id!!
            reservationId = id
            val now = Instant.now()
            val results = Collections.synchronizedList(mutableListOf<MailReservation?>())
            val threads =
                (1..10).map {
                    Thread {
                        results.add(mailReservationWriter.claimForSendingOrNull(id, now))
                    }
                }
            threads.forEach { it.start() }
            threads.forEach { it.join() }

            assertThat(results.count { it != null }).isEqualTo(1)
            val final = mailReservationRepository.findById(id)
            assertThat(final?.status).isEqualTo(MailReservationStatus.SENDING)
        } finally {
            reservationId?.let { rid ->
                jdbcTemplate.update("DELETE FROM mail_recipient_address WHERE mail_id = ?", rid)
                jdbcTemplate.update("DELETE FROM mail_attachment WHERE mail_id = ?", rid)
                jdbcTemplate.update("DELETE FROM mail WHERE id = ?", rid)
            }
        }
    }
}
