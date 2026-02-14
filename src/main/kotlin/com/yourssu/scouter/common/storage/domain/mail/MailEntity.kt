package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
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
    fun addReceiverEmailAddresses(receiverEmailAddresses: List<String>) {
        val receiverEmailAddressEntities =
            MailRecipientAddressEntity.from(
                mailAddresses = receiverEmailAddresses,
                type = MailRecipientType.TO,
                mailEntity = this,
            )
        this.recipientEmailAddress.addAll(receiverEmailAddressEntities)
    }

    fun addCcEmailAddresses(ccEmailAddresses: List<String>) {
        val ccEmailAddressEntities =
            MailRecipientAddressEntity.from(
                mailAddresses = ccEmailAddresses,
                type = MailRecipientType.CC,
                mailEntity = this,
            )
        this.recipientEmailAddress.addAll(ccEmailAddressEntities)
    }

    fun addBccEmailAddresses(bccEmailAddresses: List<String>) {
        val bccEmailAddressEntities =
            MailRecipientAddressEntity.from(
                mailAddresses = bccEmailAddresses,
                type = MailRecipientType.BCC,
                mailEntity = this,
            )
        this.recipientEmailAddress.addAll(bccEmailAddressEntities)
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
