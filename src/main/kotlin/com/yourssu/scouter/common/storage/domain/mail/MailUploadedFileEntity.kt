package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFile
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import com.yourssu.scouter.common.storage.domain.basetime.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "mail_uploaded_file",
    indexes = [
        Index(name = "idx_mail_uploaded_file_user_status", columnList = "userId, status"),
        Index(name = "idx_mail_uploaded_file_user_status_usage", columnList = "userId, status, `usage`"),
    ],
)
class MailUploadedFileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name ="`usage`" , nullable = false)
    val usage: MailFileUsage,
    @Column(nullable = false)
    val fileName: String,
    @Column(nullable = false)
    val contentType: String,
    @Column(nullable = false)
    val storageKey: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: MailUploadedFileStatus = MailUploadedFileStatus.ACTIVE,
    @Column(nullable = false)
    val used: Boolean = false,
) : BaseTimeEntity() {
    fun toDomain(): MailUploadedFile {
        return MailUploadedFile(
            id = id,
            userId = userId,
            usage = usage,
            fileName = fileName,
            contentType = contentType,
            storageKey = storageKey,
            status = status,
            used = used,
            createdAt = createdTime,
            updatedAt = updatedTime,
        )
    }

    companion object {
        fun from(file: MailUploadedFile): MailUploadedFileEntity {
            return MailUploadedFileEntity(
                id = file.id,
                userId = file.userId,
                usage = file.usage,
                fileName = file.fileName,
                contentType = file.contentType,
                storageKey = file.storageKey,
                status = file.status,
                used = file.used,
            )
        }
    }
}
