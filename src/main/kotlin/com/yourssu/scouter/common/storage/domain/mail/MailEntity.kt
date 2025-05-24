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
