package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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

    @Transactional
    override fun update(templateId: Long, template: MailTemplate): MailTemplate? {
        val existing = jpaMailTemplateRepository.findById(templateId).orElse(null) ?: return null

        // 전체 교체: 제목/본문/변수/updatedAt
        // 변수는 (template_id, variable_key) 유니크 제약이 있어, 기존 row DELETE가 DB에 반영되기 전에
        // 동일 key를 INSERT 하면 Duplicate Key가 발생할 수 있음.
        // -> orphanRemoval 삭제를 먼저 flush로 확정한 뒤, 새 변수들을 추가한다.
        existing.variables.clear()
        jpaMailTemplateRepository.flush()

        val updated = MailTemplateEntity(
            id = existing.id,
            title = template.title,
            bodyHtml = template.bodyHtml,
            createdBy = existing.createdBy,
            createdAt = existing.createdAt,
            updatedAt = java.time.LocalDateTime.now(),
        )
        updated.variables.addAll(TemplateVariableEntityFactory.fromList(template.variables, updated))

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
