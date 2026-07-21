package com.yourssu.scouter.mail.storage.template

import com.yourssu.scouter.mail.implement.template.MailTemplate
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
    @Column(nullable = false)
    var subject: String,
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
    val attachmentReferences: MutableList<MailTemplateAttachmentEntity> = mutableListOf(),
)

object MailTemplateEntityFactory {
    fun from(template: MailTemplate): MailTemplateEntity {
        val entity =
            MailTemplateEntity(
                id = template.id,
                title = template.title,
                subject = template.subject.raw,
                bodyHtml = template.bodyHtml,
                createdBy = template.createdBy,
                createdAt = template.createdAt ?: Instant.now(),
                updatedAt = template.updatedAt ?: Instant.now(),
            )
        entity.variables.addAll(TemplateVariableEntity.fromList(template.variables, entity))
        entity.attachmentReferences.addAll(
            MailTemplateAttachmentEntity.fromList(template.attachmentReferences, entity),
        )
        return entity
    }
}
