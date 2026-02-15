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
    override fun update(
        templateId: Long,
        template: MailTemplate,
    ): MailTemplate? {
        val existing = jpaMailTemplateRepository.findById(templateId).orElse(null) ?: return null

        // 1단계: 기존 변수 제거 및 flush
        // 변수는 (template_id, variable_key) 유니크 제약이 있어, 기존 row DELETE가 DB에 반영되기 전에
        // 동일 key를 INSERT 하면 Duplicate Key가 발생할 수 있음.
        // -> orphanRemoval 삭제를 먼저 flush로 확정한 뒤, 새 변수들을 추가한다.
        existing.variables.clear()
        existing.inlineImageReferences.clear()
        existing.attachmentReferences.clear()
        jpaMailTemplateRepository.flush()

        // 2단계: 기존 엔티티 필드 업데이트 (새 인스턴스 생성 대신 dirty checking 활용)
        existing.title = template.title
        existing.bodyHtml = template.bodyHtml
        existing.updatedAt = java.time.Instant.now()

        // 3단계: 새 변수 추가
        existing.variables.addAll(TemplateVariableEntityFactory.fromList(template.variables, existing))
        existing.inlineImageReferences.addAll(
            MailTemplateInlineImageEntityFactory.fromList(template.inlineImageReferences, existing),
        )
        existing.attachmentReferences.addAll(
            MailTemplateAttachmentEntityFactory.fromList(template.attachmentReferences, existing),
        )

        // save() 호출 불필요 - 트랜잭션 종료 시 dirty checking으로 자동 반영
        // 단, 반환값을 위해 명시적으로 toDomain() 호출
        return existing.toDomain()
    }

    override fun deleteById(templateId: Long): Boolean {
        val exists = jpaMailTemplateRepository.existsById(templateId)
        if (!exists) return false
        jpaMailTemplateRepository.deleteById(templateId)
        return true
    }
}

private fun MailTemplateEntity.toDomain(): MailTemplate =
    MailTemplate(
        id = id,
        title = title,
        bodyHtml = bodyHtml,
        variables = variables.map { it.toDomain() },
        inlineImageReferences = inlineImageReferences.map { it.toDomain() },
        attachmentReferences = attachmentReferences.map { it.toDomain() },
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
