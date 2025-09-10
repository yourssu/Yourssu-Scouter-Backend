package com.yourssu.scouter.common.implement.domain.mail.template

object MailTemplateFactory {

    fun createValidated(template: MailTemplate): MailTemplate {
        MailTemplateValidator.validate(template)
        return template
    }
}
