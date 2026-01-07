package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import jakarta.persistence.*
import java.time.LocalDateTime
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

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
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    var updatedAt: LocalDateTime,

    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val variables: MutableList<TemplateVariableEntity> = mutableListOf(),
)

object MailTemplateEntityFactory {
    fun from(template: MailTemplate): MailTemplateEntity {
        val entity = MailTemplateEntity(
            id = template.id,
            title = template.title,
            bodyHtml = template.bodyHtml,
            createdBy = template.createdBy,
            createdAt = template.createdAt ?: LocalDateTime.now(),
            updatedAt = template.updatedAt ?: LocalDateTime.now(),
        )
        entity.variables.addAll(TemplateVariableEntityFactory.fromList(template.variables, entity))
        return entity
    }
}
