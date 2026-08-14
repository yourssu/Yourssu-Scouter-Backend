package com.yourssu.scouter.mail.core.business

import com.yourssu.scouter.mail.file.business.MailFileService
import com.yourssu.scouter.mail.core.implement.MailWriter
import com.yourssu.scouter.mail.core.implement.MailReservation
import com.yourssu.scouter.mail.core.implement.MailReservationGroup
import com.yourssu.scouter.mail.core.implement.MailReservationGroupReader
import com.yourssu.scouter.mail.core.implement.MailReservationGroupWriter
import com.yourssu.scouter.mail.core.implement.MailReservationReader
import com.yourssu.scouter.mail.core.implement.MailReservationRepository
import com.yourssu.scouter.mail.core.implement.MailReservationStatus
import com.yourssu.scouter.mail.core.implement.MailReservationWriter
import com.yourssu.scouter.mail.template.implement.MailRenderContext
import com.yourssu.scouter.mail.template.implement.MailTemplateRepository
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.mail.support.exception.InvalidMailRenderingException
import com.yourssu.scouter.mail.support.exception.InvalidTemplateException
import com.yourssu.scouter.mail.core.implement.MailFailedException
import com.yourssu.scouter.mail.core.implement.MailReservationAccessDeniedException
import com.yourssu.scouter.mail.core.implement.MailReservationAlreadyProcessedException
import com.yourssu.scouter.mail.core.implement.MailReservationGroupNotFoundException
import com.yourssu.scouter.mail.core.implement.MailReservationNotFoundException
import com.yourssu.scouter.mail.core.implement.MailReservationNotYetDueException
import com.yourssu.scouter.member.core.business.MemberPrivacyService
import com.yourssu.scouter.member.core.implement.MemberReader
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
    private val mailSender: MailSender,
    private val memberPrivacyService: MemberPrivacyService,
    private val mailTemplateRepository: MailTemplateRepository,
    private val mailReservationGroupReader: MailReservationGroupReader,
    private val mailRecipientLookup: MailRecipientLookup,
    private val mailReservationGroupWriter: MailReservationGroupWriter,
    private val memberReader: MemberReader,
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

        val recipientProfiles = mailRecipientLookup.findByIds(command.recipients.map { it.applicantId })
        val contexts =
            command.recipients.map { recipient ->
                val profile = recipientProfiles[recipient.applicantId] ?: throw InvalidMailRenderingException()
                MailRenderContext(
                    recipientEmail = profile.email,
                    ccEmails = command.ccEmailAddresses,
                    bccEmails = command.bccEmailAddresses,
                    sharedBindings = command.sharedBindings,
                    recipientBindings = recipient.bindings,
                    recipientAttributes = profile.attributes,
                )
            }

        val mails = template.createMailList(contexts)

        val group =
            mailReservationGroupWriter.save(
                MailReservationGroup(
                    reservedByUserId = sender.id!!,
                    templateId = template.id,
                    reservationTime = command.reservationTime,
                ),
            )

        mails.forEach { mail ->
            mailWriter.reserve(mail, command.reservationTime, group.id!!, sender.id)
        }
        log.info("메일 예약 등록 완료: groupId={}, 수신자 수={}", group.id, command.recipients.size)
    }

    fun getPendingReservationStatuses(userId: Long): List<PendingMailReservationStatus> {
        val now = Instant.now()
        val reservations =
            if (memberPrivacyService.isPrivilegedUser(userId)) {
                mailReservationReader.readAllBefore(now)
            } else {
                mailReservationReader.readAllBeforeByReservedByUserIds(now, resolveTeamUserIds(userId))
            }
        return reservations.map { reservation ->
            PendingMailReservationStatus(
                reservationId = reservation.id!!,
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
                "예약 메일 처리 시작: reservationId={}, reservationTime={}, 현재시각={}, 지연시간={}초",
                reservation.id,
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
                log.error(
                    "최대 재시도 기간({}시간) 초과로 예약 삭제: reservationId={}",
                    MAX_RETRY_HOURS,
                    claimed.id,
                )
                mailReservationWriter.delete(claimed)
            }
        }
    }

    fun retryReservation(
        userId: Long,
        reservationId: Long,
    ) {
        val reservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        if (!canManageReservation(userId, reservation.reservedByUserId)) {
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
            throw MailFailedException("메일 발송에 실패했습니다. reservationId=$reservationId")
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
            log.info(
                "예약 메일 발송 직전 제목 상태: reservationId={}, subject=[{}]",
                reservation.id,
                reservation.mailSubject,
            )
            val attachments = mailFileService.downloadAttachments(reservation.attachmentReferences)
            mailSender.send(MailData.from(reservation).copy(attachments = attachments))
            mailReservationWriter.markAsSent(reservation)
            log.info("예약 메일 발송 완료: reservationId={}", reservation.id)
            true
        } catch (e: Exception) {
            log.error(
                "예약 메일 발송 실패: reservationId={}, exception={}",
                reservation.id,
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
                mailReservationGroupReader.readAllByReservedByUserIds(resolveTeamUserIds(userId))
            }
        val reservers =
            userReader.readAllByIds(groups.mapNotNull { it.reservedByUserId }.distinct())
                .associateBy { it.id!! }
        val reserverNames = reservers.mapValues { (_, user) -> resolveReserverName(user) }
        return groups.map { group ->
            val reserver = group.reservedByUserId?.let(reservers::get)
            val mails =
                mailReservationReader.readAllByGroupId(group.id!!).map { reservation ->
                    MailGroupDetail.MailSummary(
                        reservationId = reservation.id!!,
                        receiverEmail = reservation.receiverEmailAddress,
                        mailSubject = reservation.mailSubject,
                    )
                }
            MailGroupDetail(
                groupId = group.id,
                reserverName = group.reservedByUserId?.let(reserverNames::get),
                reserverEmail = reserver?.getEmailAddress(),
                templateId = group.templateId,
                reservationTime = group.reservationTime,
                status = group.status,
                createdAt = group.createdAt,
                mails = mails,
            )
        }
    }

    fun getUserMailReservations(userId: Long): List<MailReservationDetail> {
        val reservations =
            if (memberPrivacyService.isPrivilegedUser(userId)) {
                mailReservationReader.readAll()
            } else {
                mailReservationReader.readAllByReservedByUserIds(resolveTeamUserIds(userId))
            }
        return toDetails(reservations)
    }

    fun getUserMailReservation(
        userId: Long,
        reservationId: Long,
    ): MailReservationDetail {
        val reservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        val privileged = memberPrivacyService.isPrivilegedUser(userId)
        if (!privileged && reservation.reservedByUserId !in resolveTeamUserIds(userId)) {
            throw MailReservationAccessDeniedException("예약에 접근할 수 없습니다. reservationId=$reservationId")
        }

        return toDetails(listOf(reservation)).single()
    }

    fun updateMailReservation(
        userId: Long,
        reservationId: Long,
        command: MailReserveCommand,
    ) {
        val existingReservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        if (!canManageReservation(userId, existingReservation.reservedByUserId)) {
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

        mailReservationRepository.updateReservationTime(reservationId, command.reservationTime)
    }

    fun cancelMailReservation(
        userId: Long,
        reservationId: Long,
    ) {
        val reservation =
            mailReservationReader.readById(reservationId)
                ?: throw MailReservationNotFoundException("예약을 찾을 수 없습니다. reservationId=$reservationId")

        if (!canManageReservation(userId, reservation.reservedByUserId)) {
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
        val group =
            mailReservationGroupReader.readById(groupId)
                ?: throw MailReservationGroupNotFoundException("메일 그룹을 찾을 수 없습니다. groupId=$groupId")

        if (!canManageReservation(userId, group.reservedByUserId)) {
            throw MailReservationAccessDeniedException("메일 그룹에 접근할 수 없습니다. groupId=$groupId")
        }

        val reservations = mailReservationReader.readAllByGroupId(groupId)

        if (reservations.any { it.status == MailReservationStatus.SENDING }) {
            throw MailReservationAlreadyProcessedException(
                "발송 처리 중인 예약이 포함된 그룹은 삭제할 수 없습니다. groupId=$groupId",
            )
        }

        reservations.forEach { mailReservationWriter.delete(it) }
        mailReservationGroupWriter.delete(groupId)
        log.info("메일 그룹 삭제 완료: groupId={}, 삭제된 예약 수={}", groupId, reservations.size)
    }

    private fun canManageReservation(
        requesterUserId: Long,
        reservedByUserId: Long?,
    ): Boolean {
        return requesterUserId == reservedByUserId || memberPrivacyService.isScouterTeamMember(requesterUserId)
    }

    /** 같은 파트(팀) Active 멤버들의 users.id 목록. 로그인 이력이 없는 팀원은 예약도 없으므로 제외돼도 무방하다. */
    private fun resolveTeamUserIds(userId: Long): List<Long> {
        val teamEmails = memberPrivacyService.getActiveTeamMemberEmails(userId)
        return userReader.readAllByEmails(teamEmails).map { it.id!! }
    }

    private fun resolveReserverName(user: User): String {
        return memberReader.readByEmailOrNull(user.getEmailAddress())?.nicknameEnglish ?: user.userInfo.name
    }

    private fun toDetails(reservations: List<MailReservation>): List<MailReservationDetail> {
        val reserverEmails =
            userReader.readAllByIds(reservations.mapNotNull { it.reservedByUserId }.distinct())
                .associate { it.id!! to it.getEmailAddress() }
        return reservations.map { reservation ->
            MailReservationDetail(
                reservationId = reservation.id!!,
                reservationTime = reservation.reservationTime,
                status = reservation.status,
                senderEmailAddress = reservation.reservedByUserId?.let(reserverEmails::get),
                receiverEmailAddresses = listOf(reservation.receiverEmailAddress),
                ccEmailAddresses = reservation.ccEmailAddresses,
                bccEmailAddresses = reservation.bccEmailAddresses,
                mailSubject = reservation.mailSubject,
                mailBody = reservation.mailBody,
                bodyFormat = reservation.bodyFormat,
                attachmentReferences = reservation.attachmentReferences,
            )
        }
    }
}
