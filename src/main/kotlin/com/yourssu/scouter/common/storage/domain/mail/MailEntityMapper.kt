package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.message.Mail
import com.yourssu.scouter.common.implement.domain.mail.message.MailAttachmentReference
import org.springframework.stereotype.Component

@Component
class MailEntityMapper {
    fun toEntity(mail: Mail): MailEntity {
        val mailEntity =
            MailEntity(
                id = mail.id,
                senderEmailAddress = mail.senderEmailAddress,
                mailSubject = mail.mailSubject,
                mailBody = mail.mailBody,
                bodyFormat = mail.bodyFormat,
            )
        mailEntity.addReceiverEmailAddresses(mail.receiverEmailAddress)
        mailEntity.addCcEmailAddresses(mail.ccEmailAddresses)
        mailEntity.addBccEmailAddresses(mail.bccEmailAddresses)
        mailEntity.attachments.addAll(
            toAttachmentEntitiesFromReferences(mail.attachmentReferences, mailEntity),
        )
        mailEntity.assignReservation(MailReservationEntity.from(mail.reservation))

        return mailEntity
    }

    fun toDomain(mailEntity: MailEntity): Mail {
        return Mail(
            id = mailEntity.id,
            senderEmailAddress = mailEntity.senderEmailAddress,
            receiverEmailAddress = mailEntity.receiverEmailAddress.emailAddress,
            ccEmailAddresses = mailEntity.ccEmailAddresses.map { it.emailAddress },
            bccEmailAddresses = mailEntity.bccEmailAddresses.map { it.emailAddress },
            mailSubject = mailEntity.mailSubject,
            mailBody = mailEntity.mailBody,
            bodyFormat = mailEntity.bodyFormat,
            attachmentReferences = mailEntity.attachments.map(MailAttachmentEntity::toDomain),
        )
    }

    private fun toAttachmentEntitiesFromReferences(
        attachments: List<MailAttachmentReference>,
        mailEntity: MailEntity,
    ): List<MailAttachmentEntity> {
        return attachments.map {
            MailAttachmentEntity(
                name = it.fileName,
                contentType = it.contentType,
                storageKey = it.storageKey,
                mail = mailEntity,
            )
        }
    }
}
