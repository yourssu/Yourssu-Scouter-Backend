package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.mail.message.MailWriter
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationGroupReader
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationGroupWriter
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationReader
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationWriter
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
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
    private val mailSender = mock<MailSender>()
    private val memberPrivacyService = mock<MemberPrivacyService>()
    private val mailTemplateRepository = mock<MailTemplateRepository>()
    private val mailReservationGroupReader = mock<MailReservationGroupReader>()
    private val applicantReader = mock<ApplicantReader>()
    private val mailReservationGroupWriter = mock<MailReservationGroupWriter>()

    private fun createService(): MailService {
        return MailService(
            mailWriter = mailWriter,
            mailFileService = mailFileService,
            userReader = userReader,
            mailReservationReader = mailReservationReader,
            mailReservationRepository = mailReservationRepository,
            mailReservationWriter = mailReservationWriter,
            mailSender = mailSender,
            memberPrivacyService = memberPrivacyService,
            mailTemplateRepository = mailTemplateRepository,
            mailReservationGroupReader = mailReservationGroupReader,
            applicantReader = applicantReader,
            mailReservationGroupWriter = mailReservationGroupWriter,
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

    private fun reservation(
        id: Long = 10L,
        senderEmailAddress: String = "user@example.com",
        receiverEmailAddress: String = "to@example.com",
        mailSubject: String = "제목",
        mailBody: String = "본문",
        reservationTime: Instant = Instant.parse("2026-03-01T00:00:00Z"),
        status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    ): MailReservation =
        MailReservation(
            id = id,
            senderEmailAddress = senderEmailAddress,
            receiverEmailAddress = receiverEmailAddress,
            mailSubject = mailSubject,
            mailBody = mailBody,
            bodyFormat = MailBodyFormat.HTML,
            reservationTime = reservationTime,
            status = status,
        )

    @Test
    fun `getUserMailReservations는 로그인 사용자의 발신자 이메일 기준으로 예약 목록을 조회한다`() {
        val userId = 1L
        val senderEmail = "user@example.com"
        whenever(memberPrivacyService.getActiveTeamMemberEmails(userId)).thenReturn(setOf(senderEmail))

        val r = reservation(senderEmailAddress = senderEmail)
        whenever(mailReservationReader.readAllBySenderEmails(any())).thenReturn(listOf(r))

        val service = createService()
        val results = service.getUserMailReservations(userId)

        verify(mailReservationReader).readAllBySenderEmails(listOf(senderEmail))
        assertThat(results).hasSize(1)
        val detail = results[0]
        assertThat(detail.reservationId).isEqualTo(10L)
        assertThat(detail.mailSubject).isEqualTo("제목")
        assertThat(detail.receiverEmailAddresses).containsExactly("to@example.com")
    }

    @Test
    fun `getUserMailReservation는 존재하지 않는 예약 ID에 대해 예외를 던진다`() {
        whenever(mailReservationReader.readById(999L)).thenReturn(null)

        val service = createService()

        assertThatThrownBy { service.getUserMailReservation(1L, 999L) }
            .isInstanceOf(MailReservationNotFoundException::class.java)
            .hasMessageContaining("예약을 찾을 수 없습니다")
    }

    @Test
    fun `getUserMailReservation는 다른 사용자의 예약에 접근하면 예외를 던진다`() {
        val userId = 1L
        whenever(memberPrivacyService.getActiveTeamMemberEmails(userId)).thenReturn(setOf("user@example.com"))

        val r = reservation(senderEmailAddress = "other@example.com")
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()

        assertThatThrownBy { service.getUserMailReservation(userId, 10L) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)
            .hasMessageContaining("예약에 접근할 수 없습니다")
    }

    @Test
    fun `updateMailReservation는 메일 내용과 예약 시간을 전체 교체한다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r = reservation(reservationTime = Instant.now().plusSeconds(600))
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val command =
            MailReserveCommand(
                senderUserId = userId,
                templateId = 1L,
                recipients = listOf(MailReserveCommand.RecipientCommand(applicantId = 1L)),
                ccEmailAddresses = listOf("new-cc@example.com"),
                bccEmailAddresses = listOf("new-bcc@example.com"),
                reservationTime = Instant.parse("2026-03-02T00:00:00Z"),
            )

        val service = createService()
        service.updateMailReservation(userId, 10L, command)

        verify(mailReservationRepository).updateReservationTime(
            eq(10L),
            eq(Instant.parse("2026-03-02T00:00:00Z")),
        )
    }

    @Test
    fun `updateMailReservation는 화이트리스트 사용자면 타인의 예약도 수정할 수 있다`() {
        val userId = 1L
        val user = createUser(userId, "umi.urssu@gmail.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(true)

        val r = reservation(senderEmailAddress = "other@example.com", reservationTime = Instant.now().plusSeconds(600))
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val command =
            MailReserveCommand(
                senderUserId = userId,
                templateId = 1L,
                recipients = listOf(MailReserveCommand.RecipientCommand(applicantId = 1L)),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                reservationTime = Instant.parse("2026-03-02T00:00:00Z"),
            )

        val service = createService()
        service.updateMailReservation(userId, 10L, command)

        verify(mailReservationRepository).updateReservationTime(any(), any())
    }

    @Test
    fun `updateMailReservation는 화이트리스트가 아니면 타인의 예약 수정 시 예외를 던진다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r = reservation(senderEmailAddress = "other@example.com", reservationTime = Instant.now().plusSeconds(600))
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val command =
            MailReserveCommand(
                senderUserId = userId,
                templateId = 1L,
                recipients = listOf(MailReserveCommand.RecipientCommand(applicantId = 1L)),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                reservationTime = Instant.now().plusSeconds(60),
            )

        val service = createService()

        assertThatThrownBy { service.updateMailReservation(userId, 10L, command) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)

        verify(mailReservationRepository, never()).updateReservationTime(any(), any())
    }

    @Test
    fun `updateMailReservation는 예약 시간이 지난 경우 예외를 던지고 저장을 수행하지 않는다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r = reservation(reservationTime = Instant.now().minusSeconds(60))
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val command =
            MailReserveCommand(
                senderUserId = userId,
                templateId = 1L,
                recipients = listOf(MailReserveCommand.RecipientCommand(applicantId = 1L)),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                reservationTime = Instant.now().plusSeconds(60),
            )

        val service = createService()

        assertThatThrownBy { service.updateMailReservation(userId, 10L, command) }
            .isInstanceOf(MailReservationAlreadyProcessedException::class.java)

        verify(mailReservationRepository, never()).updateReservationTime(any(), any())
    }

    @Test
    fun `cancelMailReservation는 화이트리스트가 아니면 타인의 예약 삭제 시 예외를 던지고 삭제를 수행하지 않는다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r = reservation(senderEmailAddress = "other@example.com", reservationTime = Instant.now().plusSeconds(600))
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()

        assertThatThrownBy { service.cancelMailReservation(userId, 10L) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)

        verify(mailReservationWriter, never()).delete(any())
    }

    @Test
    fun `cancelMailReservation는 화이트리스트 사용자면 타인의 예약도 삭제할 수 있다`() {
        val userId = 1L
        val user = createUser(userId, "umi.urssu@gmail.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(true)

        val r = reservation(senderEmailAddress = "other@example.com", reservationTime = Instant.now().plusSeconds(600))
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()
        service.cancelMailReservation(userId, 10L)

        verify(mailReservationWriter).delete(r)
    }

    @Test
    fun `cancelMailReservation는 예약 시간이 지난 경우 예외를 던지고 삭제를 수행하지 않는다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r = reservation(reservationTime = Instant.now().minusSeconds(60))
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()

        assertThatThrownBy { service.cancelMailReservation(userId, 10L) }
            .isInstanceOf(MailReservationAlreadyProcessedException::class.java)

        verify(mailReservationWriter, never()).delete(any())
    }

    @Test
    fun `retryReservation는 PENDING_SEND 상태이고 예약 시간이 지난 경우 발송을 시도하고 성공 시 SENT로 저장한다`() {
        val userId = 1L
        val senderEmail = "user@example.com"
        val user = createUser(userId, senderEmail)
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r =
            reservation(
                senderEmailAddress = senderEmail,
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(r)
        whenever(mailReservationWriter.claimForSendingOrNull(eq(10L), any())).thenReturn(
            r.copy(status = MailReservationStatus.SENDING),
        )

        val service = createService()
        service.retryReservation(userId, 10L)

        val claimed = r.copy(status = MailReservationStatus.SENDING)
        verify(mailReservationWriter).markAsSent(claimed)
    }

    @Test
    fun `retryReservation는 이미 SENT인 경우 예외를 던진다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r =
            reservation(
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.SENT,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()

        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailReservationAlreadyProcessedException::class.java)
            .hasMessageContaining("이미 발송된 메일은 재전송할 수 없습니다")

        verify(mailReservationRepository, never()).updateReservationTime(any(), any())
    }

    @Test
    fun `retryReservation는 예약 시간이 지나지 않은 경우 예외를 던진다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r =
            reservation(
                reservationTime = Instant.now().plusSeconds(600),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()

        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailReservationNotYetDueException::class.java)
            .hasMessageContaining("예약 시간이 지나지 않은 메일은 재전송할 수 없습니다")

        verify(mailReservationRepository, never()).updateReservationTime(any(), any())
    }

    @Test
    fun `retryReservation는 화이트리스트가 아니면 다른 사용자의 예약에 대해 예외를 던진다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r =
            reservation(
                senderEmailAddress = "other@example.com",
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()

        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailReservationAccessDeniedException::class.java)
            .hasMessageContaining("예약에 접근할 수 없습니다")

        verify(mailReservationRepository, never()).updateReservationTime(any(), any())
    }

    @Test
    fun `retryReservation는 화이트리스트 사용자면 다른 사용자의 예약도 재전송할 수 있다`() {
        val userId = 1L
        val user = createUser(userId, "umi.urssu@gmail.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(true)

        val r =
            reservation(
                senderEmailAddress = "other@example.com",
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(r)
        whenever(mailReservationWriter.claimForSendingOrNull(eq(10L), any())).thenReturn(
            r.copy(status = MailReservationStatus.SENDING),
        )

        val service = createService()
        service.retryReservation(userId, 10L)

        val claimed = r.copy(status = MailReservationStatus.SENDING)
        verify(mailReservationWriter).markAsSent(claimed)
    }

    @Test
    fun `retryReservation는 존재하지 않는 예약에 대해 예외를 던진다`() {
        val userId = 1L
        val user = createUser(userId, "user@example.com")
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(mailReservationReader.readById(999L)).thenReturn(null)

        val service = createService()

        assertThatThrownBy { service.retryReservation(userId, 999L) }
            .isInstanceOf(MailReservationNotFoundException::class.java)
            .hasMessageContaining("예약을 찾을 수 없습니다")
    }

    @Test
    fun `retryReservation는 발송 실패 시 MailFailedException을 던진다`() {
        val userId = 1L
        val senderEmail = "user@example.com"
        val user = createUser(userId, senderEmail)
        whenever(userReader.readById(userId)).thenReturn(user)
        whenever(memberPrivacyService.isScouterTeamMember(userId)).thenReturn(false)

        val r =
            reservation(
                senderEmailAddress = senderEmail,
                reservationTime = Instant.now().minusSeconds(60),
                status = MailReservationStatus.PENDING_SEND,
            )
        whenever(mailReservationReader.readById(10L)).thenReturn(r)
        whenever(mailSender.send(any())).thenThrow(RuntimeException("발송 실패"))
        whenever(mailReservationWriter.claimForSendingOrNull(eq(10L), any())).thenReturn(
            r.copy(status = MailReservationStatus.SENDING),
        )

        val service = createService()

        assertThatThrownBy { service.retryReservation(userId, 10L) }
            .isInstanceOf(MailFailedException::class.java)
            .hasMessageContaining("메일 발송에 실패했습니다")

        val claimed = r.copy(status = MailReservationStatus.SENDING)
        verify(mailReservationWriter).markAsPendingSend(claimed)
    }

    @Test
    fun `getUserMailReservation는 같은 팀(발신자 이메일이 팀 이메일 목록에 포함)이면 타인의 예약도 조회할 수 있다`() {
        val userId = 1L
        whenever(memberPrivacyService.getActiveTeamMemberEmails(userId)).thenReturn(
            setOf("viewer@example.com", "teammate@example.com"),
        )

        val r = reservation(senderEmailAddress = "teammate@example.com")
        whenever(mailReservationReader.readById(10L)).thenReturn(r)

        val service = createService()
        val detail = service.getUserMailReservation(userId, 10L)

        assertThat(detail.senderEmailAddress).isEqualTo("teammate@example.com")
    }

    @Test
    fun `특권 유저는 예약 목록 조회 시 전체를 조회한다`() {
        val userId = 1L
        whenever(memberPrivacyService.isPrivilegedUser(userId)).thenReturn(true)

        val r = reservation(senderEmailAddress = "other@example.com")
        whenever(mailReservationReader.readAll()).thenReturn(listOf(r))

        val service = createService()
        val results = service.getUserMailReservations(userId)

        verify(mailReservationReader).readAll()
        assertThat(results).hasSize(1)
        assertThat(results.first().senderEmailAddress).isEqualTo("other@example.com")
    }
}
