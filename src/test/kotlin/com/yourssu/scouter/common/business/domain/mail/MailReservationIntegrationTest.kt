package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.mail.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import com.yourssu.scouter.common.implement.domain.user.TokenInfo
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import com.yourssu.scouter.common.implement.domain.user.UserRepository
import com.yourssu.scouter.common.implement.support.exception.MailReservationNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@ActiveProfiles(profiles = ["local", "local-dev-db"])
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@Transactional
@Suppress("NonAsciiCharacters")
class MailReservationIntegrationTest {

    @Autowired
    lateinit var mailService: MailService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var mailReservationRepository: MailReservationRepository

    @MockBean
    lateinit var oauth2Service: OAuth2Service

    @MockBean
    lateinit var mailSender: MailSender

    private fun createUser(): User {
        val email = "reservation-it-${UUID.randomUUID()}@example.com"
        val user =
            User(
                userInfo =
                    UserInfo(
                        name = "reservation-it",
                        email = email,
                        profileImageUrl = "http://example.com/profile.png",
                        oauthId = "reservation-it-${UUID.randomUUID()}",
                        oauth2Type = OAuth2Type.GOOGLE,
                    ),
                tokenInfo =
                    TokenInfo(
                        tokenPrefix = "Bearer",
                        accessToken = "access-token",
                        refreshToken = "refresh-token",
                        accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
                    ),
            )
        return userRepository.save(user)
    }

    @Test
    fun `메일 예약 생성부터 조회, 수정, 취소까지 end-to-end로 동작한다`() {
        // given: dev 프로필 DB에 사용자를 하나 생성한다.
        val savedUser = createUser()
        val userId = savedUser.id!!
        val now = Instant.now()
        val initialReservationTime = now.plusSeconds(600)

        // when 1: 메일 예약 생성
        val reserveCommand =
            MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "통합테스트-초기제목",
                mailBody = "초기 본문",
                bodyFormat = MailBodyFormat.HTML,
                attachmentReferences = emptyList(),
                reservationTime = initialReservationTime,
            )
        mailService.reserveMail(reserveCommand)

        // then 1: 사용자 기준 예약 목록에 방금 예약한 메일이 조회된다.
        val reservationsAfterCreate = mailService.getUserMailReservations(userId)
        assertThat(reservationsAfterCreate).hasSize(1)
        val createdDetail = reservationsAfterCreate.first()
        assertThat(createdDetail.mailSubject).isEqualTo("통합테스트-초기제목")
        assertThat(createdDetail.receiverEmailAddresses).containsExactly("to@example.com")
        assertThat(createdDetail.reservationTime).isEqualTo(initialReservationTime)
        assertThat(createdDetail.status).isEqualTo(MailReservationStatus.SCHEDULED)

        val reservationId = createdDetail.reservationId
        val mailId = createdDetail.mailId

        // when 2: 예약 메일을 수정해 제목, 본문, 수신자, 예약 시간을 모두 변경한다.
        val updatedReservationTime = now.plusSeconds(1200)
        val updateCommand =
            MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = listOf("new-to@example.com"),
                ccEmailAddresses = listOf("cc@example.com"),
                bccEmailAddresses = listOf("bcc@example.com"),
                mailSubject = "통합테스트-수정제목",
                mailBody = "수정된 본문",
                bodyFormat = MailBodyFormat.PLAIN_TEXT,
                attachmentReferences = emptyList(),
                reservationTime = updatedReservationTime,
            )
        mailService.updateMailReservation(userId, reservationId, updateCommand)

        // then 2: 단건 조회 시 수정된 내용이 반영되어 있다.
        val updatedDetail = mailService.getUserMailReservation(userId, reservationId)
        assertThat(updatedDetail.mailId).isEqualTo(mailId)
        assertThat(updatedDetail.mailSubject).isEqualTo("통합테스트-수정제목")
        assertThat(updatedDetail.mailBody).isEqualTo("수정된 본문")
        assertThat(updatedDetail.bodyFormat).isEqualTo(MailBodyFormat.PLAIN_TEXT)
        assertThat(updatedDetail.receiverEmailAddresses).containsExactly("new-to@example.com")
        assertThat(updatedDetail.ccEmailAddresses).containsExactly("cc@example.com")
        assertThat(updatedDetail.bccEmailAddresses).containsExactly("bcc@example.com")
        assertThat(updatedDetail.reservationTime).isEqualTo(updatedReservationTime)

        // when 3: 예약을 취소한다.
        mailService.cancelMailReservation(userId, reservationId)

        // then 3-1: 사용자 기준 예약 목록에서는 더 이상 조회되지 않는다.
        val reservationsAfterCancel = mailService.getUserMailReservations(userId)
        assertThat(reservationsAfterCancel).isEmpty()

        // then 3-2: 저장소 기준으로도 예약 엔티티가 삭제된다.
        val deleted: MailReservation? = mailReservationRepository.findById(reservationId)
        assertThat(deleted).isNull()

        // then 3-3: 서비스 단건 조회는 NotFound 예외를 던진다.
        assertThatThrownBy { mailService.getUserMailReservation(userId, reservationId) }
            .isInstanceOf(MailReservationNotFoundException::class.java)
    }

    @Test
    fun `재전송 API는 PENDING_SEND 예약을 발송 성공 시 SENT로 전환한다`() {
        // given: 예약 생성
        val savedUser = createUser()
        val userId = savedUser.id!!
        val now = Instant.now()
        val reserveCommand =
            MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "재전송테스트",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
                attachmentReferences = emptyList(),
                reservationTime = now.plusSeconds(600),
            )
        mailService.reserveMail(reserveCommand)

        val createdDetail = mailService.getUserMailReservations(userId).first()
        val reservationId = createdDetail.reservationId
        val mailId = createdDetail.mailId

        // 예약 시간을 과거로, status를 PENDING_SEND로 직접 변경
        mailReservationRepository.save(
            MailReservation(
                id = reservationId,
                mailId = mailId,
                reservationTime = now.minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            ),
        )

        whenever(oauth2Service.refreshOAuth2TokenBeforeExpiry(eq(userId), eq(OAuth2Type.GOOGLE), any()))
            .thenReturn(savedUser)

        // when: 재전송
        mailService.retryReservation(userId, reservationId)

        // then: status가 SENT로 변경됨
        val afterRetry = mailService.getUserMailReservation(userId, reservationId)
        assertThat(afterRetry.status).isEqualTo(MailReservationStatus.SENT)
    }
}

