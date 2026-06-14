package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.common.implement.domain.mail.message.Mail
import com.yourssu.scouter.common.implement.domain.mail.message.MailRepository
import com.yourssu.scouter.common.implement.domain.mail.message.MailWriter
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationGroup
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationGroupReader
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationGroupWriter
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationReader
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationWriter
import com.yourssu.scouter.common.implement.domain.mail.template.MailRenderContext
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.support.exception.*
import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    private val mailSender: MailSender,
    private val memberPrivacyService: MemberPrivacyService,
    private val mailTemplateRepository: MailTemplateRepository,
    private val mailReservationGroupReader: MailReservationGroupReader,
    private val applicantReader: ApplicantReader,
    private val mailReservationGroupWriter: MailReservationGroupWriter,
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailService::class.java)
        private const val MAX_RETRY_HOURS = 24L
        private const val STUCK_SENDING_MINUTES = 15L
    }

    fun sendMail(command: MailSendCommand) {
        log.info(
            "메일 즉시 발송 요청: senderUserId={}, subject=[{}]",
            command.senderUserId,
            command.mailSubject,
        )
        val sender = userReader.readById(command.senderUserId)
        val resolvedReferences = mailFileService.resolveAttachmentReferences(command.attachmentReferences)
        val attachments = mailFileService.downloadAttachments(resolvedReferences)

        val mailData =
            MailData(
                senderEmailAddress = sender.getEmailAddress(),
                receiverEmailAddresses = command.receiverEmailAddresses,
                mailSubject = command.mailSubject,
                mailBody = command.mailBody,
                bodyFormat = command.bodyFormat,
                attachments = attachments,
            )

        mailSender.send(mailData)
        log.info("메일 즉시 발송 완료: senderUserId={}", command.senderUserId)
    }

    fun reserveMail(command: MailReserveCommand) {
        log.info(
            "메일 예약 등록 요청: reservationTime={}, senderUserId={}, templateId={}",
            command.reservationTime,
            command.senderUserId,
            command.templateId,
        )
        val sender = userReader.readById(command.senderUserId)
        val template =
            mailTemplateRepository.findById(command.templateId)
                ?: throw InvalidTemplateException("템플릿을 찾을 수 없습니다. templateId=${command.templateId}")

        val applicants = applicantReader.readByIds(command.recipients.map { it.applicantId })
        val contexts =
            command.recipients.map { recipient ->
                if (applicants[recipient.applicantId] == null) {
                    throw InvalidMailRenderingException()
                }
                MailRenderContext(
                    recipientEmail = applicants[recipient.applicantId]!!.email,
                    ccEmails = command.ccEmailAddresses,
                    bccEmails = command.bccEmailAddresses,
                    sharedBindings = command.sharedBindings,
                    recipientBindings = recipient.bindings,
                    recipientAttributes = applicants[recipient.applicantId]!!.toAttributeMap(),
                )
            }

        val mails = template.createMailList(sender, contexts)

        val group =
            mailReservationGroupWriter.save(
                MailReservationGroup(
                    senderEmail = sender.getEmailAddress(),
                    templateId = template.id,
                    reservationTime = command.reservationTime,
                ),
            )

        mails.forEach { mail ->
            mailWriter.reserve(mail, command.reservationTime, group.id!!)
        }
        log.info("메일 예약 등록 완료: groupId={}, 수신자 수={}", group.id, command.recipients.size)
    }

    fun getPendingReservationStatuses(userId: Long): List<PendingMailReservationStatus> {
        val now = Instant.now()
        val reservations =
            if (memberPrivacyService.isPrivilegedUser(userId)) {
                mailReservationReader.readAllBefore(now)
            } else {
                val senderEmails = memberPrivacyService.getActiveTeamMemberEmails(userId).toList()
                mailReservationReader.readAllBeforeBySenderEmails(now, senderEmails)
            }
        return reservations.map { reservation ->
            PendingMailReservationStatus(
                reservationId = reservation.id!!,
                mailId = reservation.mailId,
                reservationTime = reservation.reservationTime,
                failureErrorCode = null,
                failedAt = null,
            )
        }
    }

    fun sendReservedMails() {
        val now = Instant.now()
        val resetCount =
            mailReservationWriter.resetStuckSendingReservations(
                now.minus(STUCK_SENDING_MINUTES, ChronoUnit.MINUTES),
            )
        if (resetCount > 0) {
            log.warn("SENDING 고착 복구: {}건을 PENDING_SEND로 되돌렸습니다.", resetCount)
        }
        val reservations = mailReservationReader.readAllPendingBefore(now)
        log.info("예약 메일 처리 시작: 기준시각={}, 발송대상건수={}", now, reservations.size)
        for (reservation in reservations) {
            val delaySeconds = java.time.Duration.between(reservation.reservationTime, now).seconds
            log.info(
                "예약 메일 처리 시작: reservationId={}, mailId={}, reservationTime={}, 현재시각={}, 지연시간={}초",
                reservation.id,
                reservation.mailId,
                reservation.reservationTime,
                now,
                delaySeconds,
            )
            val claimed = mailReservationWriter.claimForSendingOrNull(reservation.id!!, now)
            if (claimed == null) {
                log.debug("예약 claim 실패(다른 인스턴스 처리 중 또는 상태 변경): reservationId={}", reservation.id)
                continue
            }
            val sent = trySendClaimedReservation(claimed)
            if (!sent && claimed.reservationTime.plus(MAX_RETRY_HOURS, ChronoUnit.HOURS).isBefore(now)) {
                log.error("최대 재시도 기간({}시간) 초과로 예약 삭제: reservationId={}, mailId={}", MAX_RETRY_HOURS, claimed.id, claimed.mailId)
                mailReservationWriter.delete(claimed)
            }
        }
    }

    fun retryReservation(
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
        if (!canManageReservation(senderEmail, userId, mail.senderEmailAddress)) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        val now = Instant.now()
        if (!reservation.canRetry(now)) {
            when (reservation.status) {
                MailReservationStatus.SENT ->
                    throw MailReservationAlreadyProcessedException(
                        "이미 발송된 메일은 재전송할 수 없습니다. reservationId=$reservationId",
                    )
                else ->
                    throw MailReservationNotYetDueException(
                        "예약 시간이 지나지 않은 메일은 재전송할 수 없습니다. reservationId=$reservationId, reservationTime=${reservation.reservationTime}",
                    )
            }
        }

        val claimed =
            mailReservationWriter.claimForSendingOrNull(reservation.id!!, now)
                ?: run {
                    val current =
                        mailReservationReader.readById(reservationId)
                            ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")
                    when (current.status) {
                        MailReservationStatus.SENT ->
                            throw MailReservationAlreadyProcessedException(
                                "이미 발송된 메일은 재전송할 수 없습니다. reservationId=$reservationId",
                            )
                        MailReservationStatus.SENDING ->
                            throw MailFailedException(
                                "다른 작업에서 발송 처리 중입니다. 잠시 후 다시 시도해 주세요. reservationId=$reservationId",
                            )
                        else ->
                            throw MailFailedException(
                                "예약 메일을 처리할 수 없습니다. reservationId=$reservationId",
                            )
                    }
                }

        val sent = trySendClaimedReservation(claimed)
        if (!sent) {
            throw MailFailedException(
                "메일 발송에 실패했습니다. reservationId=$reservationId",
            )
        }
    }

    private fun trySendClaimedReservation(reservation: MailReservation): Boolean {
        if (reservation.status == MailReservationStatus.SENT) {
            log.warn("이미 발송된 예약에 대한 발송 시도 무시: reservationId={}", reservation.id)
            return false
        }
        if (reservation.status != MailReservationStatus.SENDING) {
            log.warn(
                "SENDING이 아닌 예약에 대한 발송 시도 무시: reservationId={}, status={}",
                reservation.id,
                reservation.status,
            )
            return false
        }
        return try {
            val mail = mailRepository.findById(reservation.mailId)
            if (mail == null) {
                log.warn("예약 메일의 원본을 찾을 수 없어 삭제합니다: reservationId={}, mailId={}", reservation.id, reservation.mailId)
                mailReservationWriter.delete(reservation)
                return@trySendClaimedReservation false
            }
            log.info(
                "예약 메일 발송 직전 제목 상태: reservationId={}, mailId={}, subject=[{}]",
                reservation.id,
                reservation.mailId,
                mail.mailSubject,
            )
            val attachments = mailFileService.downloadAttachments(mail.attachmentReferences)
            mailSender.send(MailData.from(mail).copy(attachments = attachments))
            mailReservationWriter.markAsSent(reservation)
            log.info("예약 메일 발송 완료: reservationId={}, mailId={}", reservation.id, reservation.mailId)
            true
        } catch (e: Exception) {
            log.error(
                "예약 메일 발송 실패: reservationId={}, mailId={}, exception={}",
                reservation.id,
                reservation.mailId,
                e.javaClass.simpleName,
                e,
            )
            mailReservationWriter.markAsPendingSend(reservation)
            false
        }
    }

    fun getMailGroups(userId: Long): List<MailGroupDetail> {
        val groups =
            if (memberPrivacyService.isPrivilegedUser(userId)) {
                mailReservationGroupReader.readAll()
            } else {
                val senderEmails = memberPrivacyService.getActiveTeamMemberEmails(userId).toList()
                mailReservationGroupReader.readAllBySenderEmails(senderEmails)
            }
        return groups.map { group ->
            val mailIds = mailReservationReader.readAllByGroupId(group.id!!).map { it.mailId }
            MailGroupDetail(
                groupId = group.id,
                senderEmail = group.senderEmail,
                templateId = group.templateId,
                reservationTime = group.reservationTime,
                status = group.status,
                createdAt = group.createdAt,
                mailIds = mailIds,
            )
        }
    }

    fun getUserMailReservations(userId: Long): List<MailReservationDetail> {
        val reservations =
            if (memberPrivacyService.isPrivilegedUser(userId)) {
                mailReservationReader.readAll()
            } else {
                val senderEmails = memberPrivacyService.getActiveTeamMemberEmails(userId).toList()
                mailReservationReader.readAllBySenderEmails(senderEmails)
            }
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
        val reservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        val mail =
            mailRepository.findById(reservation.mailId)
                ?: throw MailReservationNotFoundException("예약 메일을 찾을 수 없습니다. reservationId=$reservationId, mailId=${reservation.mailId}")

        val privileged = memberPrivacyService.isPrivilegedUser(userId)
        val canAccess =
            if (privileged) {
                true
            } else {
                memberPrivacyService.getActiveTeamMemberEmails(userId).contains(mail.senderEmailAddress)
            }
        if (!canAccess) {
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
        if (!canManageReservation(senderEmail, userId, existingMail.senderEmailAddress)) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        val now = Instant.now()
        if (!existingReservation.canEdit(now)) {
            throw MailReservationAlreadyProcessedException(
                buildString {
                    when (existingReservation.status) {
                        MailReservationStatus.SENT -> append("이미 발송된 메일은 수정할 수 없습니다.")
                        MailReservationStatus.SENDING -> append("발송 처리 중인 예약은 수정할 수 없습니다.")
                        else -> append("예약 시간이 지난 메일은 수정할 수 없습니다.")
                    }
                    append(" reservationId=$reservationId")
                },
            )
        }

        // 템플릿 기반 예약은 수신자별 N개의 Mail row로 구성되므로 전체 재렌더링이 필요합니다.
        // 현재는 예약 시간 변경만 지원합니다.
        val updatedReservation = existingReservation.copy(reservationTime = command.reservationTime)
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
        if (!canManageReservation(senderEmail, userId, mail.senderEmailAddress)) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        val now = Instant.now()
        if (!reservation.canCancel(now)) {
            throw MailReservationAlreadyProcessedException(
                buildString {
                    when (reservation.status) {
                        MailReservationStatus.SENT -> append("이미 발송된 메일은 취소할 수 없습니다.")
                        MailReservationStatus.SENDING -> append("발송 처리 중인 예약은 취소할 수 없습니다.")
                        else -> append("예약 시간이 지난 메일은 취소할 수 없습니다.")
                    }
                    append(" reservationId=$reservationId")
                },
            )
        }

        mailReservationWriter.delete(reservation)
    }

    @Transactional
    fun deleteMailGroup(
        userId: Long,
        groupId: Long,
    ) {
        val user = userReader.readById(userId)
        val group =
            mailReservationGroupReader.readById(groupId)
                ?: throw MailReservationGroupNotFoundException("메일 그룹을 찾을 수 없습니다. groupId=$groupId")

        val senderEmail = user.getEmailAddress()
        if (!canManageReservation(senderEmail, userId, group.senderEmail)) {
            throw MailReservationAccessDeniedException("메일 그룹에 접근할 수 없습니다. groupId=$groupId")
        }

        val reservations = mailReservationReader.readAllByGroupId(groupId)

        // 발송 처리 중(SENDING)인 예약이 있으면 삭제를 막습니다. 스케줄러가 claim해 발송 중인 행을
        // 지우면 cascade로 그 Mail까지 사라져 발송 결과를 잃을 수 있기 때문입니다.
        if (reservations.any { it.status == MailReservationStatus.SENDING }) {
            throw MailReservationAlreadyProcessedException(
                "발송 처리 중인 예약이 포함된 그룹은 삭제할 수 없습니다. groupId=$groupId",
            )
        }

        // 예약을 행 단위로 삭제해야 OneToOne(cascade=ALL) 덕분에 연결된 Mail 행까지 함께 삭제됩니다.
        reservations.forEach { mailReservationWriter.delete(it) }
        mailReservationGroupWriter.delete(groupId)
        log.info("메일 그룹 삭제 완료: groupId={}, 삭제된 예약 수={}", groupId, reservations.size)
    }

    private fun canManageReservation(
        requesterEmail: String,
        requesterUserId: Long,
        reservationSenderEmail: String,
    ): Boolean {
        return requesterEmail == reservationSenderEmail || memberPrivacyService.isScouterTeamMember(requesterUserId)
    }

    private fun toDetail(
        reservation: MailReservation,
        mail: Mail,
    ): MailReservationDetail {
        return MailReservationDetail(
            reservationId = reservation.id!!,
            mailId = reservation.mailId,
            reservationTime = reservation.reservationTime,
            status = reservation.status,
            senderEmailAddress = mail.senderEmailAddress,
            receiverEmailAddresses = listOf(mail.receiverEmailAddress),
            ccEmailAddresses = mail.ccEmailAddresses,
            bccEmailAddresses = mail.bccEmailAddresses,
            mailSubject = mail.mailSubject,
            mailBody = mail.mailBody,
            bodyFormat = mail.bodyFormat,
            attachmentReferences = mail.attachmentReferences,
        )
    }
}
