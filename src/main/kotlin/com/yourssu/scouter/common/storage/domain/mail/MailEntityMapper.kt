package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import com.yourssu.scouter.common.implement.domain.mail.MailInlineImageReference
import jakarta.mail.util.ByteArrayDataSource
import org.springframework.stereotype.Component

@Component
class MailEntityMapper(
    private val mailFileStorage: MailFileStorage,
) {
    fun toEntity(mail: Mail): MailEntity {
        val mailEntity =
            MailEntity(
                id = mail.id,
                senderEmailAddress = mail.senderEmailAddress,
                mailSubject = mail.mailSubject,
                mailBody = mail.mailBody,
                bodyFormat = mail.bodyFormat,
            )
        mailEntity.addReceiverEmailAddresses(mail.receiverEmailAddresses)
        mailEntity.addCcEmailAddresses(mail.ccEmailAddresses)
        mailEntity.addBccEmailAddresses(mail.bccEmailAddresses)
        mailEntity.inlineImages.addAll(toInlineImageEntitiesFromReferences(mail.inlineImageReferences, mailEntity))
        mailEntity.attachments.addAll(toAttachmentEntitiesFromReferences(mail.attachmentReferences, mailEntity))

        return mailEntity
    }

    fun toDomain(mailEntity: MailEntity): Mail {
        return Mail(
            id = mailEntity.id,
            senderEmailAddress = mailEntity.senderEmailAddress,
            receiverEmailAddresses = mailEntity.receiverEmailAddresses.map { it.emailAddress },
            ccEmailAddresses = mailEntity.ccEmailAddresses.map { it.emailAddress },
            bccEmailAddresses = mailEntity.bccEmailAddresses.map { it.emailAddress },
            mailSubject = mailEntity.mailSubject,
            mailBody = mailEntity.mailBody,
            bodyFormat = mailEntity.bodyFormat,
            inlineImages =
                mailEntity.inlineImages.associate {
                    it.name to ByteArrayDataSource(resolveBytes(it.storageKey), it.contentType ?: "image/*")
                },
            attachments =
                mailEntity.attachments.associate {
                    it.name to ByteArrayDataSource(resolveBytes(it.storageKey), it.contentType ?: "application/octet-stream")
                },
        )
    }

    private fun toInlineImageEntitiesFromReferences(
        inlineImages: List<MailInlineImageReference>,
        mailEntity: MailEntity,
    ): List<MailInlineImageEntity> {
        return inlineImages.map {
            MailInlineImageEntity(
                name = it.contentId,
                contentType = it.contentType,
                storageKey = it.storageKey,
                mail = mailEntity,
            )
        }
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

    private fun resolveBytes(storageKey: String?): ByteArray {
        val key =
            storageKey
                ?: throw IllegalStateException("Mail file storageKey is required.")

        return mailFileStorage.download(key)
    }
}
