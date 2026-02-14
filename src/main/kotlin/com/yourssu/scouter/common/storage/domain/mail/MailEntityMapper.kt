package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import jakarta.mail.util.ByteArrayDataSource
import org.springframework.stereotype.Component
import java.util.UUID

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
        mailEntity.inlineImages.addAll(toInlineImageEntities(mail.inlineImages, mailEntity))
        mailEntity.attachments.addAll(toAttachmentEntities(mail.attachments, mailEntity))

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

    private fun toInlineImageEntities(
        inlineImages: Map<String, ByteArrayDataSource>,
        mailEntity: MailEntity,
    ): List<MailInlineImageEntity> {
        return inlineImages.map { (name, dataSource) ->
            val contentType = dataSource.contentType ?: "image/*"
            val bytes = dataSource.inputStream.use { it.readBytes() }
            val key = upload("inline", name, bytes, contentType)

            MailInlineImageEntity(
                name = name,
                contentType = contentType,
                storageKey = key,
                mail = mailEntity,
            )
        }
    }

    private fun toAttachmentEntities(
        attachments: Map<String, ByteArrayDataSource>,
        mailEntity: MailEntity,
    ): List<MailAttachmentEntity> {
        return attachments.map { (name, dataSource) ->
            val contentType = dataSource.contentType ?: "application/octet-stream"
            val bytes = dataSource.inputStream.use { it.readBytes() }
            val key = upload("attachment", name, bytes, contentType)

            MailAttachmentEntity(
                name = name,
                contentType = contentType,
                storageKey = key,
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

    private fun upload(
        category: String,
        name: String,
        bytes: ByteArray,
        contentType: String,
    ): String {
        val key = "$category/${UUID.randomUUID()}-${sanitize(name)}"

        return mailFileStorage.upload(key, bytes, contentType)
    }

    private fun sanitize(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
