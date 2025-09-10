package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MailTemplateRepositoryImpl(
    private val jpaMailTemplateRepository: JpaMailTemplateRepository,
) : MailTemplateRepository {

    override fun save(template: MailTemplate): MailTemplate {
        val entity = MailTemplateEntityFactory.from(template)
        val saved = jpaMailTemplateRepository.save(entity)
        return saved.toDomain()
    }

    override fun findAll(pageable: Pageable): Page<MailTemplate> {
        return jpaMailTemplateRepository.findAllBy(pageable).map { it.toDomain() }
    }

    override fun findById(templateId: Long): MailTemplate? {
        return jpaMailTemplateRepository.findById(templateId).orElse(null)?.toDomain()
    }

    override fun update(templateId: Long, template: MailTemplate): MailTemplate? {
        val existing = jpaMailTemplateRepository.findById(templateId).orElse(null) ?: return null

        // 전체 교체: 제목/본문/변수/updatedAt
        existing.variables.clear()
        existing.variables.addAll(TemplateVariableEntityFactory.fromList(template.variables, existing))

        val updated = MailTemplateEntity(
            id = existing.id,
            title = template.title,
            bodyHtml = template.bodyHtml,
            createdBy = existing.createdBy,
            createdAt = existing.createdAt,
            updatedAt = java.time.LocalDateTime.now(),
            variables = existing.variables,
        )

        val saved = jpaMailTemplateRepository.save(updated)
        return saved.toDomain()
    }

    override fun deleteById(templateId: Long): Boolean {
        val exists = jpaMailTemplateRepository.existsById(templateId)
        if (!exists) return false
        jpaMailTemplateRepository.deleteById(templateId)
        return true
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
