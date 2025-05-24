package com.yourssu.scouter.common.storage.domain.mail

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "mail_recipient_address")
class MailRecipientAddressEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id")
    val mail: MailEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MailRecipientType,

    @Column(nullable = false)
    val emailAddress: String,
)

enum class MailRecipientType {
    TO, CC, BCC
}
