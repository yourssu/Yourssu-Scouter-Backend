package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.MailInlineImageReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "mail_template_inline_image")
class MailTemplateInlineImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    val template: MailTemplateEntity,
    @Column
    val fileId: Long? = null,
    @Column(nullable = false)
    val contentId: String,
    @Column(nullable = false)
    val fileName: String,
    @Column(nullable = false)
    val contentType: String,
    @Column(nullable = false)
    val storageKey: String,
)

fun MailTemplateInlineImageEntity.toDomain(): MailInlineImageReference {
    return MailInlineImageReference(
        fileId = fileId,
        contentId = contentId,
        fileName = fileName,
        contentType = contentType,
        storageKey = storageKey,
    )
}

object MailTemplateInlineImageEntityFactory {
    fun fromList(
        references: List<MailInlineImageReference>,
        template: MailTemplateEntity,
    ): List<MailTemplateInlineImageEntity> {
        return references.map {
            MailTemplateInlineImageEntity(
                template = template,
                fileId = it.fileId,
                contentId = it.contentId,
                fileName = it.fileName,
                contentType = it.contentType,
                storageKey = it.storageKey,
            )
        }
    }
}
