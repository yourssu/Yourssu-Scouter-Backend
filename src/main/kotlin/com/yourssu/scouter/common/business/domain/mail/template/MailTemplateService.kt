package com.yourssu.scouter.common.business.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplateRepository
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import org.springframework.stereotype.Service

@Service
class MailTemplateService(
    private val mailTemplateRepository: MailTemplateRepository,
) {
    fun createTemplate(template: MailTemplate): MailTemplate {
        return mailTemplateRepository.save(template)
    }
}
