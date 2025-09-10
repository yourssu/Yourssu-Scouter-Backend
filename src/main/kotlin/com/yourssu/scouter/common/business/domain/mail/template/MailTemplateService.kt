package com.yourssu.scouter.common.business.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateValidator
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MailTemplateService(
    private val mailTemplateRepository: MailTemplateRepository,
) {
    fun createTemplate(template: MailTemplate): MailTemplate {
        MailTemplateValidator.validate(template)
        return mailTemplateRepository.save(template)
    }

    fun readTemplates(pageable: Pageable): Page<MailTemplate> {
        return mailTemplateRepository.findAll(pageable)
    }

    fun readTemplate(templateId: Long): MailTemplate? {
        return mailTemplateRepository.findById(templateId)
    }

    fun updateTemplate(templateId: Long, template: MailTemplate): MailTemplate? {
        MailTemplateValidator.validate(template)
        return mailTemplateRepository.update(templateId, template)
    }

    fun deleteTemplate(templateId: Long): Boolean {
        return mailTemplateRepository.deleteById(templateId)
    }
}
