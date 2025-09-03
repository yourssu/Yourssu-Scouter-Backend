package com.yourssu.scouter.common.implement.domain.mail.template

interface MailTemplateRepository {
    fun save(template: MailTemplate): MailTemplate
}
