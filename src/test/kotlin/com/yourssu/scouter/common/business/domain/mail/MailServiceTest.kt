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
import com.yourssu.scouter.common.implement.domain.mail.MailWriter
import com.yourssu.scouter.common.implement.domain.user.TokenInfo
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.support.exception.MailFailedException
import com.yourssu.scouter.common.implement.support.exception.MailReservationAccessDeniedException
import com.yourssu.scouter.common.implement.support.exception.MailReservationAlreadyProcessedException
import com.yourssu.scouter.common.implement.support.exception.MailReservationNotFoundException
import com.yourssu.scouter.common.implement.support.exception.MailReservationNotYetDueException
import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailServiceTest {
    private val mailWriter = mock<MailWriter>()
    private val mailFileService = mock<MailFileService>()
    private val userReader = mock<UserReader>()
    private val mailReservationReader = mock<MailReservationReader>()
    private val mailReservationRepository = mock<MailReservationRepository>()
    private val mailReservationWriter = mock<MailReservationWriter>()
    private val mailRepository = mock<MailRepository>()
    private val oauth2Service = mock<OAuth2Service>()
    private val mailSender = mock<MailSender>()
    private val memberPrivacyService = mock<MemberPrivacyService>()

    private fun createService(): MailService {
        return MailService(
            mailWriter = mailWriter,
            mailFileService = mailFileService,
            userReader = userReader,
            mailReservationReader = mailReservationReader,
            mailReservationRepository = mailReservationRepository,
            mailReservationWriter = mailReservationWriter,
            mailRepository = mailRepository,
            oauth2Service = oauth2Service,
            mailSender = mailSender,
            memberPrivacyService = memberPrivacyService,
        )
    }

    private fun createUser(
        id: Long,
        email: String,
    ): User {
        return User(
            id = id,
            userInfo =
                UserInfo(
                    name = "tester",
                    email = email,
                    profileImageUrl = "http://example.com/profile.png",
                    oauthId = "oauth-$id",
                    oauth2Type = OAuth2Type.GOOGLE,
                ),
            tokenInfo =
                TokenInfo(
                    tokenPrefix = "Bearer",
                    accessToken = "access",
                    refreshToken = "refresh",
                    accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
                ),
        )
    }

    @Test
    fun `getUserMailReservations는 로그인 사용자의 발신자 이메일 기준으로 예약 목록을 조회한다`() {
        // given
        val userId = 1L
        val senderEmail = "user@example.com"
        val user = createUser(userId, senderEmail)
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.getActiveTeamMemberEmails(userId)).thenReturn(setOf(senderEmail))

        val reservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.parse("2026-03-01T00:00:00Z"),
            )
        whenever(mailReservationReader.readAllBySenderEmails(any())).thenReturn(listOf(reservation))

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = senderEmail,
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // when
        val results = service.getUserMailReservations(userId)

        // then
        verify(mailReservationReader).readAllBySenderEmails(listOf(senderEmail))
        assertThat(results).hasSize(1)
        val detail = results[0]
        assertThat(detail.reservationId).isEqualTo(10L)
        assertThat(detail.mailId).isEqualTo(100L)
        assertThat(detail.mailSubject).isEqualTo("제목")
        assertThat(detail.receiverEmailAddresses).containsExactly("to@example.com")
    }

    @Test
    fun `getUserMailReservation는 존재하지 않는 예약 ID에 대해 예외를 던진다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(mailReservationReader.readById(999L)).thenReturn(null)

        val service = createService()

        // expect
        assertThatThrownBy { service.getUserMailReservation(userId, 999L) }
            .isInstanceOf(MailReservationNotFoundException::class.java)
            .hasMessageContaining("예약을 찾을 수 없습니다")
    }

    @Test
    fun `getUserMailReservation는 다른 사용자의 예약에 접근하면 예외를 던진다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.getActiveTeamMemberEmails(userId)).thenReturn(setOf("user@example.com"))

        val reservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.parse("2026-03-01T00:00:00Z"),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(reservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // expect
        assertThatThrownBy { service.getUserMailReservation(userId, 10L) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)
            .hasMessageContaining("예약에 접근할 수 없습니다")
    }

    @Test
    fun `updateMailReservation는 메일 내용과 예약 시간을 전체 교체한다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val futureReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().plusSeconds(600),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(futureReservation)

        val existingMail =
            Mail(
                id = 100L,
                senderEmailAddress = "user@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "old-subject",
                mailBody = "old-body",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(existingMail)

        whenever(
            mailFileService.resolveAttachmentReferences(
                any(),
            ),
        ).thenAnswer { invocation -> invocation.getArgument(0) }

        val command =
            MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = listOf("new-to@example.com"),
                ccEmailAddresses = listOf("new-cc@example.com"),
                bccEmailAddresses = listOf("new-bcc@example.com"),
                mailSubject = "new-subject",
                mailBody = "new-body",
                bodyFormat = MailBodyFormat.PLAIN_TEXT,
                reservationTime = Instant.parse("2026-03-02T00:00:00Z"),
            )

        val service = createService()

        // when
        service.updateMailReservation(userId, 10L, command)

        // then: MailRepository와 MailReservationRepository에 기대하는 값이 전달되는지 검증
        val mailCaptor = argumentCaptor<Mail>()
        verify(mailRepository).save(mailCaptor.capture())
        val savedMail = mailCaptor.firstValue
        assertThat(savedMail.id).isEqualTo(100L)
        assertThat(savedMail.mailSubject).isEqualTo("new-subject")
        assertThat(savedMail.mailBody).isEqualTo("new-body")
        assertThat(savedMail.receiverEmailAddresses).containsExactly("new-to@example.com")
        assertThat(savedMail.ccEmailAddresses).containsExactly("new-cc@example.com")
        assertThat(savedMail.bccEmailAddresses).containsExactly("new-bcc@example.com")
        assertThat(savedMail.bodyFormat).isEqualTo(MailBodyFormat.PLAIN_TEXT)

        val reservationCaptor = argumentCaptor<MailReservation>()
        verify(mailReservationRepository).save(reservationCaptor.capture())
        val savedReservation = reservationCaptor.firstValue
        assertThat(savedReservation.id).isEqualTo(10L)
        assertThat(savedReservation.mailId).isEqualTo(100L)
        assertThat(savedReservation.reservationTime).isEqualTo(Instant.parse("2026-03-02T00:00:00Z"))
    }

    @Test
    fun `updateMailReservation는 화이트리스트 사용자면 타인의 예약도 수정할 수 있다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "umi.urssu@gmail.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(true)

        val futureReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().plusSeconds(600),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(futureReservation)

        val existingMail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "old-subject",
                mailBody = "old-body",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(existingMail)
        whenever(mailFileService.resolveAttachmentReferences(any())).thenAnswer { invocation -> invocation.getArgument(0) }

        val command =
            MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = listOf("new-to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "new-subject",
                mailBody = "new-body",
                bodyFormat = MailBodyFormat.PLAIN_TEXT,
                reservationTime = Instant.parse("2026-03-02T00:00:00Z"),
            )

        val service = createService()

        // when
        service.updateMailReservation(userId, 10L, command)

        // then
        verify(mailRepository).save(any())
        verify(mailReservationRepository).save(any())
    }

    @Test
    fun `updateMailReservation는 화이트리스트가 아니면 타인의 예약 수정 시 예외를 던진다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val futureReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().plusSeconds(600),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(futureReservation)

        val existingMail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "old-subject",
                mailBody = "old-body",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(existingMail)

        val command =
            MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = listOf("new-to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "new-subject",
                mailBody = "new-body",
                bodyFormat = MailBodyFormat.PLAIN_TEXT,
                reservationTime = Instant.now().plusSeconds(60),
            )

        val service = createService()

        // expect
        assertThatThrownBy { service.updateMailReservation(userId, 10L, command) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)

        verify(mailRepository, org.mockito.kotlin.never()).save(any())
        verify(mailReservationRepository, org.mockito.kotlin.never()).save(any())
    }

    @Test
    fun `updateMailReservation는 예약 시간이 지난 경우 예외를 던지고 저장을 수행하지 않는다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val pastReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().minusSeconds(60),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(pastReservation)

        val existingMail =
            Mail(
                id = 100L,
                senderEmailAddress = "user@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "old-subject",
                mailBody = "old-body",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(existingMail)

        val command =
            MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = listOf("new-to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "new-subject",
                mailBody = "new-body",
                bodyFormat = MailBodyFormat.PLAIN_TEXT,
                reservationTime = Instant.now().plusSeconds(60),
            )

        val service = createService()

        // expect
        assertThatThrownBy { service.updateMailReservation(userId, 10L, command) }
            .isInstanceOf(MailReservationAlreadyProcessedException::class.java)

        verify(mailRepository, org.mockito.kotlin.never()).save(any())
        verify(mailReservationRepository, org.mockito.kotlin.never()).save(any())
    }

    @Test
    fun `cancelMailReservation는 화이트리스트가 아니면 타인의 예약 삭제 시 예외를 던지고 삭제를 수행하지 않는다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val futureReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().plusSeconds(600),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(futureReservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // expect
        assertThatThrownBy { service.cancelMailReservation(userId, 10L) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)

        verify(mailReservationWriter, org.mockito.kotlin.never()).delete(any())
    }

    @Test
    fun `cancelMailReservation는 화이트리스트 사용자면 타인의 예약도 삭제할 수 있다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "umi.urssu@gmail.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(true)

        val futureReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().plusSeconds(600),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(futureReservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // when
        service.cancelMailReservation(userId, 10L)

        // then
        verify(mailReservationWriter).delete(futureReservation)
    }

    @Test
    fun `cancelMailReservation는 예약 시간이 지난 경우 예외를 던지고 삭제를 수행하지 않는다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val pastReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().minusSeconds(60),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(pastReservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "user@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // expect
        assertThatThrownBy { service.cancelMailReservation(userId, 10L) }
            .isInstanceOf(MailReservationAlreadyProcessedException::class.java)

        verify(mailReservationWriter, org.mockito.kotlin.never()).delete(any())
    }

    @Test
    fun `retryReservation는 PENDING_SEND 상태이고 예약 시간이 지난 경우 발송을 시도하고 성공 시 SENT로 저장한다`() {
        // given
        val userId = 1L
        val senderEmail = "user@example.com"
        val user = createUser(userId, senderEmail)
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val pastReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(pastReservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = senderEmail,
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)
        whenever(userReader.findByEmail(senderEmail)).thenReturn(user)
        whenever(oauth2Service.refreshOAuth2TokenBeforeExpiry(userId, OAuth2Type.GOOGLE, 10L)).thenReturn(user)
        whenever(mailReservationWriter.claimForSendingOrNull(eq(10L), any())).thenReturn(
            pastReservation.copy(status = MailReservationStatus.SENDING),
        )

        val service = createService()

        // when
        service.retryReservation(userId, 10L)

        // then: mailReservationRepository.save가 SENT 상태로 호출됨
        val reservationCaptor = argumentCaptor<MailReservation>()
        verify(mailReservationRepository).save(reservationCaptor.capture())
        assertThat(reservationCaptor.firstValue.status).isEqualTo(MailReservationStatus.SENT)
    }

    @Test
    fun `retryReservation는 이미 SENT인 경우 예외를 던진다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val sentReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.SENT,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(sentReservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "user@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // expect
        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailReservationAlreadyProcessedException::class.java)
            .hasMessageContaining("이미 발송된 메일은 재전송할 수 없습니다")

        verify(mailReservationRepository, org.mockito.kotlin.never()).save(any())
    }

    @Test
    fun `retryReservation는 예약 시간이 지나지 않은 경우 예외를 던진다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val futureReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().plusSeconds(600),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(futureReservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "user@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // expect
        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailReservationNotYetDueException::class.java)
            .hasMessageContaining("예약 시간이 지나지 않은 메일은 재전송할 수 없습니다")

        verify(mailReservationRepository, org.mockito.kotlin.never()).save(any())
    }

    @Test
    fun `retryReservation는 화이트리스트가 아니면 다른 사용자의 예약에 대해 예외를 던진다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val reservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(reservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // expect
        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)
            .hasMessageContaining("예약에 접근할 수 없습니다")

        verify(mailReservationRepository, org.mockito.kotlin.never()).save(any())
    }

    @Test
    fun `retryReservation는 화이트리스트 사용자면 다른 사용자의 예약도 재전송할 수 있다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "umi.urssu@gmail.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(true)

        val reservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(reservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val senderUser = createUser(2L, "other@example.com")
        whenever(userReader.findByEmail("other@example.com")).thenReturn(senderUser)
        whenever(oauth2Service.refreshOAuth2TokenBeforeExpiry(2L, OAuth2Type.GOOGLE, 10L)).thenReturn(senderUser)
        whenever(mailReservationWriter.claimForSendingOrNull(eq(10L), any())).thenReturn(
            reservation.copy(status = MailReservationStatus.SENDING),
        )

        val service = createService()

        // when
        service.retryReservation(userId, 10L)

        // then
        val reservationCaptor = argumentCaptor<MailReservation>()
        verify(mailReservationRepository).save(reservationCaptor.capture())
        assertThat(reservationCaptor.firstValue.status).isEqualTo(MailReservationStatus.SENT)
    }

    @Test
    fun `retryReservation는 존재하지 않는 예약에 대해 예외를 던진다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(mailReservationReader.readById(999L)).thenReturn(null)

        val service = createService()

        // expect
        assertThatThrownBy { service.retryReservation(userId, 999L) }
            .isInstanceOf(MailReservationNotFoundException::class.java)
            .hasMessageContaining("예약을 찾을 수 없습니다")
    }

    @Test
    fun `retryReservation는 발송 실패 시 MailFailedException을 던진다`() {
        // given
        val userId = 1L
        val senderEmail = "user@example.com"
        val user = createUser(userId, senderEmail)
        whenever(userReader.readById(userId)).thenReturn(user)

        val pastReservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(pastReservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = senderEmail,
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)
        whenever(userReader.findByEmail(senderEmail)).thenReturn(user)
        whenever(oauth2Service.refreshOAuth2TokenBeforeExpiry(userId, OAuth2Type.GOOGLE, 10L)).thenReturn(user)
        whenever(mailSender.send(any(), any())).thenThrow(RuntimeException("발송 실패"))
        whenever(mailReservationWriter.claimForSendingOrNull(eq(10L), any())).thenReturn(
            pastReservation.copy(status = MailReservationStatus.SENDING),
        )

        val service = createService()

        // expect
        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailFailedException::class.java)
            .hasMessageContaining("메일 발송에 실패했습니다")

        val reservationCaptor = argumentCaptor<MailReservation>()
        verify(mailReservationRepository).save(reservationCaptor.capture())
        assertThat(reservationCaptor.firstValue.status).isEqualTo(MailReservationStatus.PENDING_SEND)
    }

    @Test
    fun `getUserMailReservation는 같은 팀(발신자 이메일이 팀 이메일 목록에 포함)이면 타인의 예약도 조회할 수 있다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "viewer@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.getActiveTeamMemberEmails(userId)).thenReturn(setOf("viewer@example.com", "teammate@example.com"))

        val reservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.parse("2026-03-01T00:00:00Z"),
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(reservation)

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "teammate@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // when
        val detail = service.getUserMailReservation(userId, 10L)

        // then
        assertThat(detail.mailId).isEqualTo(100L)
        assertThat(detail.senderEmailAddress).isEqualTo("teammate@example.com")
    }

    @Test
    fun `특권 유저는 예약 목록 조회 시 전체를 조회한다`() {
        // given
        val userId = 1L
        val user = createUser(userId, "privileged@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isPrivilegedUser(userId)).thenReturn(true)

        val reservation =
            MailReservation(
                id = 10L,
                mailId = 100L,
                reservationTime = Instant.parse("2026-03-01T00:00:00Z"),
            )
        whenever(mailReservationReader.readAll()).thenReturn(listOf(reservation))

        val mail =
            Mail(
                id = 100L,
                senderEmailAddress = "other@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "본문",
                bodyFormat = MailBodyFormat.HTML,
            )
        whenever(mailRepository.findById(100L)).thenReturn(mail)

        val service = createService()

        // when
        val results = service.getUserMailReservations(userId)

        // then
        verify(mailReservationReader).readAll()
        assertThat(results).hasSize(1)
        assertThat(results.first().senderEmailAddress).isEqualTo("other@example.com")
    }
}
