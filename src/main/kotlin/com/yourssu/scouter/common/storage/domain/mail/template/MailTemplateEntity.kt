package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "mail_template")
class MailTemplateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var title: String,
    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    var bodyHtml: String,
    @Column(nullable = false)
    val createdBy: Long,
    @Column(nullable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    var updatedAt: Instant,
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val variables: MutableList<TemplateVariableEntity> = mutableListOf(),
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val inlineImageReferences: MutableList<MailTemplateInlineImageEntity> = mutableListOf(),
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val attachmentReferences: MutableList<MailTemplateAttachmentEntity> = mutableListOf(),
)

object MailTemplateEntityFactory {
    fun from(template: MailTemplate): MailTemplateEntity {
        val entity =
            MailTemplateEntity(
                id = template.id,
                title = template.title,
                bodyHtml = template.bodyHtml,
                createdBy = template.createdBy,
                createdAt = template.createdAt ?: Instant.now(),
                updatedAt = template.updatedAt ?: Instant.now(),
            )
        entity.variables.addAll(TemplateVariableEntity.fromList(template.variables, entity))
        entity.inlineImageReferences.addAll(
            MailTemplateInlineImageEntity.fromList(template.inlineImageReferences, entity),
        )
        entity.attachmentReferences.addAll(
            MailTemplateAttachmentEntity.fromList(template.attachmentReferences, entity),
        )
        return entity
    }
}
