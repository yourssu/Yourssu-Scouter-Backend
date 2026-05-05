package com.yourssu.scouter.common.business.domain.mail.template

import com.yourssu.scouter.common.business.domain.mail.MailFileService
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateValidator
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MailTemplateService(
    private val mailTemplateRepository: MailTemplateRepository,
    private val mailTemplateValidator: MailTemplateValidator,
    private val mailFileService: MailFileService,
) {
    fun createTemplate(template: MailTemplate): MailTemplate {
        val resolved = resolveReferences(template)
        resolved.validateAttachmentReferences()
        mailTemplateValidator.validate(resolved)
        return mailTemplateRepository.save(resolved)
    }

    fun readTemplates(pageable: Pageable): Page<MailTemplate> {
        return mailTemplateRepository.findAll(pageable)
    }

    fun readTemplate(templateId: Long): MailTemplate? {
        return mailTemplateRepository.findById(templateId)
    }

    fun updateTemplate(
        templateId: Long,
        template: MailTemplate,
    ): MailTemplate? {
        val resolved = resolveReferences(template)
        resolved.validateAttachmentReferences()
        mailTemplateValidator.validate(resolved)
        return mailTemplateRepository.update(templateId, resolved)
    }

    fun deleteTemplate(templateId: Long): Boolean {
        return mailTemplateRepository.deleteById(templateId)
    }

    private fun resolveReferences(template: MailTemplate): MailTemplate {
        return MailTemplate(
            id = template.id,
            title = template.title,
            subject = template.subject,
            bodyHtml = template.bodyHtml,
            variables = template.variables,
            attachmentReferences =
                mailFileService.resolveAttachmentReferences(
                    template.attachmentReferences,
                ),
            createdBy = template.createdBy,
            createdAt = template.createdAt,
            updatedAt = template.updatedAt,
        )
    }
}
