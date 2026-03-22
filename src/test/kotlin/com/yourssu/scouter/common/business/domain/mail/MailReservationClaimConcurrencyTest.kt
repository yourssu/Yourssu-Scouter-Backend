package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import com.yourssu.scouter.common.implement.domain.mail.MailReservationWriter
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import java.time.Instant
import java.util.Collections
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Suppress("NonAsciiCharacters")
class MailReservationClaimConcurrencyTest {

    @Autowired
    lateinit var mailRepository: MailRepository

    @Autowired
    lateinit var mailReservationRepository: MailReservationRepository

    @Autowired
    lateinit var mailReservationWriter: MailReservationWriter

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `동시에 claim 해도 한 워커만 SENDING을 가져온다`() {
        val mail =
            mailRepository.save(
                Mail(
                    senderEmailAddress = "claim-concurrency@example.com",
                    receiverEmailAddresses = listOf("to@example.com"),
                    mailSubject = "claim-test",
                    mailBody = "body",
                    bodyFormat = MailBodyFormat.HTML,
                ),
            )
        val reservation =
            mailReservationRepository.save(
                MailReservation(
                    mailId = mail.id!!,
                    reservationTime = Instant.now().minusSeconds(30),
                    status = MailReservationStatus.SCHEDULED,
                ),
            )
        val id = reservation.id!!
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
    }
}
