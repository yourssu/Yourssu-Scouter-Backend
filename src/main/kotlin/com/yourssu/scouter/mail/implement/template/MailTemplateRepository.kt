package com.yourssu.scouter.mail.implement.template

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MailTemplateRepository {
    fun save(template: MailTemplate): MailTemplate
    fun findAll(pageable: Pageable): Page<MailTemplate>
    fun findById(templateId: Long): MailTemplate?
    fun update(templateId: Long, template: MailTemplate): MailTemplate?
    fun deleteById(templateId: Long): Boolean
}
