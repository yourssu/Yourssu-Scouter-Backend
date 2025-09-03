package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import org.springframework.stereotype.Repository

@Repository
class MailTemplateRepositoryImpl(
    private val jpaMailTemplateRepository: JpaMailTemplateRepository,
) : MailTemplateRepository {

    override fun save(template: MailTemplate): MailTemplate {
        val entity = MailTemplateEntity(
            id = template.id,
            title = template.title,
            bodyHtml = template.bodyHtml,
            createdBy = template.createdBy,
            createdAt = template.createdAt ?: java.time.LocalDateTime.now(),
            updatedAt = template.updatedAt ?: java.time.LocalDateTime.now(),
        )

        val variables = template.variables.map {
            TemplateVariableEntity(
                template = entity,
                variableKey = it.key,
                variableType = it.type,
                displayName = it.displayName,
                perRecipient = it.perRecipient,
            )
        }
        entity.variables.addAll(variables)

        val saved = jpaMailTemplateRepository.save(entity)
        return saved.toDomain()
    }
}

private fun MailTemplateEntity.toDomain(): MailTemplate = MailTemplate(
    id = id,
    title = title,
    bodyHtml = bodyHtml,
    variables = variables.map { it.toDomain() },
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
