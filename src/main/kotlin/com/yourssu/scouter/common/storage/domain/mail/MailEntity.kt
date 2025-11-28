package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.implement.domain.mail.Mail
import jakarta.mail.util.ByteArrayDataSource
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "mail")
class MailEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val senderEmailAddress: String,

    @Column(nullable = false)
    val mailSubject: String,

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "LONGTEXT")
    val mailBody: String,

    @Enumerated(EnumType.STRING)
    val bodyFormat: MailBodyFormat,

    @OneToMany(mappedBy = "mail", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val recipientEmailAddress: MutableList<MailRecipientAddressEntity> = mutableListOf(),

    @OneToMany(mappedBy = "mail", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val inlineImages: MutableList<MailInlineImageEntity> = mutableListOf(),

    @OneToMany(mappedBy = "mail", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val attachments: MutableList<MailAttachmentEntity> = mutableListOf(),
) {

    companion object {
        fun from(mail: Mail): MailEntity {
            val mailEntity = MailEntity(
                id = mail.id,
                senderEmailAddress = mail.senderEmailAddress,
                mailSubject = mail.mailSubject,
                mailBody = mail.mailBody,
                bodyFormat = mail.bodyFormat,
            )
            mailEntity.addReceiverEmailAddresses(mail.receiverEmailAddresses)
            mailEntity.addCcEmailAddresses(mail.ccEmailAddresses)
            mailEntity.addBccEmailAddresses(mail.bccEmailAddresses)
            mailEntity.addInlineImages(mail.inlineImages)
            mailEntity.addAttachments(mail.attachments)

            return mailEntity
        }
    }

    fun addReceiverEmailAddresses(receiverEmailAddresses: List<String>) {
        val receiverEmailAddressEntities = MailRecipientAddressEntity.from(
            mailAddresses = receiverEmailAddresses,
            type = MailRecipientType.TO,
            mailEntity = this
        )
        this.recipientEmailAddress.addAll(receiverEmailAddressEntities)
    }

    fun addCcEmailAddresses(ccEmailAddresses: List<String>) {
        val ccEmailAddressEntities = MailRecipientAddressEntity.from(
            mailAddresses = ccEmailAddresses,
            type = MailRecipientType.CC,
            mailEntity = this
        )
        this.recipientEmailAddress.addAll(ccEmailAddressEntities)
    }

    fun addBccEmailAddresses(bccEmailAddresses: List<String>) {
        val bccEmailAddressEntities = MailRecipientAddressEntity.from(
            mailAddresses = bccEmailAddresses,
            type = MailRecipientType.BCC,
            mailEntity = this
        )
        this.recipientEmailAddress.addAll(bccEmailAddressEntities)
    }

    fun addInlineImages(inlineImages: Map<String, ByteArrayDataSource>) {
        val inlineImageEntities = inlineImages.map { (name, dataSource) ->
            MailInlineImageEntity(
                name = name,
                data = dataSource.inputStream.readBytes(),
                mail = this
            )
        }

        this.inlineImages.addAll(inlineImageEntities)
    }

    fun addAttachments(attachments: Map<String, ByteArrayDataSource>) {
        val attachmentEntities = attachments.map { (name, dataSource) ->
            MailAttachmentEntity(
                name = name,
                data = dataSource.inputStream.readBytes(),
                mail = this
            )
        }

        this.attachments.addAll(attachmentEntities)
    }

    fun toDomain() : Mail {
        return Mail(
            id = id,
            senderEmailAddress = senderEmailAddress,
            receiverEmailAddresses = receiverEmailAddresses.map { it.emailAddress },
            ccEmailAddresses = ccEmailAddresses.map { it.emailAddress },
            bccEmailAddresses = bccEmailAddresses.map { it.emailAddress },
            mailSubject = mailSubject,
            mailBody = mailBody,
            bodyFormat = bodyFormat,
            inlineImages = inlineImages.associate { it.name to ByteArrayDataSource(it.data, "image/*") },
            attachments = attachments.associate { it.name to ByteArrayDataSource(it.data, "application/octet-stream") }
        )
    }

    @get:Transient
    val receiverEmailAddresses: List<MailRecipientAddressEntity>
        get() = recipientEmailAddress.filter { it.type == MailRecipientType.TO }

    @get:Transient
    val ccEmailAddresses: List<MailRecipientAddressEntity>
        get() = recipientEmailAddress.filter { it.type == MailRecipientType.CC }

    @get:Transient
    val bccEmailAddresses: List<MailRecipientAddressEntity>
        get() = recipientEmailAddress.filter { it.type == MailRecipientType.BCC }
}
