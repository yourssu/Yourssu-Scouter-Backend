package com.yourssu.scouter.mail.core.storage

import com.yourssu.scouter.mail.core.business.MailBodyFormat
import com.yourssu.scouter.mail.core.implement.MailReservation
import com.yourssu.scouter.mail.core.implement.MailReservationStatus
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
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "mail")
class MailEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    // 예약자 users.id (nullable: 레거시 데이터 backfill)
    @Column
    val reservedByUserId: Long?,
    @Column(nullable = false)
    val mailSubject: String,
    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "LONGTEXT")
    val mailBody: String,
    @Enumerated(EnumType.STRING)
    val bodyFormat: MailBodyFormat,
    @Column(nullable = true)
    val groupId: Long? = null,
    @Column(nullable = false)
    val reservationTime: Instant,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    @Column
    val claimedAt: Instant? = null,
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "mail", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val recipientEmailAddress: MutableList<MailRecipientAddressEntity> = mutableListOf(),
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "mail", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val attachments: MutableList<MailAttachmentEntity> = mutableListOf(),
) {
    fun addReceiverEmailAddresses(receiverEmailAddress: String) {
        recipientEmailAddress.addAll(
            MailRecipientAddressEntity.from(
                mailAddresses = listOf(receiverEmailAddress),
                type = MailRecipientType.TO,
                mailEntity = this,
            ),
        )
    }

    fun addCcEmailAddresses(ccEmailAddresses: List<String>) {
        recipientEmailAddress.addAll(
            MailRecipientAddressEntity.from(
                mailAddresses = ccEmailAddresses,
                type = MailRecipientType.CC,
                mailEntity = this,
            ),
        )
    }

    fun addBccEmailAddresses(bccEmailAddresses: List<String>) {
        recipientEmailAddress.addAll(
            MailRecipientAddressEntity.from(
                mailAddresses = bccEmailAddresses,
                type = MailRecipientType.BCC,
                mailEntity = this,
            ),
        )
    }

    @get:Transient
    val receiverEmailAddress: MailRecipientAddressEntity?
        get() = recipientEmailAddress.firstOrNull { it.type == MailRecipientType.TO }

    @get:Transient
    val ccEmailAddresses: List<MailRecipientAddressEntity>
        get() = recipientEmailAddress.filter { it.type == MailRecipientType.CC }

    @get:Transient
    val bccEmailAddresses: List<MailRecipientAddressEntity>
        get() = recipientEmailAddress.filter { it.type == MailRecipientType.BCC }

    fun toDomain(): MailReservation =
        MailReservation(
            id = id,
            reservedByUserId = reservedByUserId,
            receiverEmailAddress = receiverEmailAddress?.emailAddress ?: "",
            ccEmailAddresses = ccEmailAddresses.map { it.emailAddress },
            bccEmailAddresses = bccEmailAddresses.map { it.emailAddress },
            mailSubject = mailSubject,
            mailBody = mailBody,
            bodyFormat = bodyFormat,
            attachmentReferences = attachments.map(MailAttachmentEntity::toDomain),
            groupId = groupId,
            reservationTime = reservationTime,
            status = status,
            claimedAt = claimedAt,
        )
}
