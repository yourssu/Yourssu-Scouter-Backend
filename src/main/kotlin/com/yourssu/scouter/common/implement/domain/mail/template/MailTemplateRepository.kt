package com.yourssu.scouter.common.implement.domain.mail.template

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MailTemplateRepository {
    fun save(template: MailTemplate): MailTemplate
    fun findAll(pageable: Pageable): Page<MailTemplate>
    fun findById(templateId: Long): MailTemplate?
    fun update(templateId: Long, template: MailTemplate): MailTemplate?
}
