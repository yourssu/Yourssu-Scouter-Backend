package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservation
import org.springframework.stereotype.Component

@Component
class MailEntityMapper {
    fun toEntity(reservation: MailReservation): MailEntity {
        val mailEntity =
            MailEntity(
                id = reservation.id,
                reservedByUserId = reservation.reservedByUserId,
                mailSubject = reservation.mailSubject,
                mailBody = reservation.mailBody,
                bodyFormat = reservation.bodyFormat,
                groupId = reservation.groupId,
                reservationTime = reservation.reservationTime,
                status = reservation.status,
                claimedAt = reservation.claimedAt,
            )
        mailEntity.addReceiverEmailAddresses(reservation.receiverEmailAddress)
        mailEntity.addCcEmailAddresses(reservation.ccEmailAddresses)
        mailEntity.addBccEmailAddresses(reservation.bccEmailAddresses)
        mailEntity.attachments.addAll(
            reservation.attachmentReferences.map {
                MailAttachmentEntity(
                    name = it.fileName,
                    contentType = it.contentType,
                    storageKey = it.storageKey,
                    mail = mailEntity,
                )
            },
        )
        return mailEntity
    }
}
