package com.yourssu.scouter.common.implement.domain.mail.template

object MailTemplateUpdater {
    fun updateValidated(existingId: Long, createdBy: Long, current: MailTemplate, replacement: MailTemplate): MailTemplate {
        val merged = MailTemplate(
            id = existingId,
            title = replacement.title,
            bodyHtml = replacement.bodyHtml,
            variables = replacement.variables,
            createdBy = createdBy,
            createdAt = current.createdAt,
            updatedAt = java.time.Instant.now(),
        )

        MailTemplateValidator.validate(merged)
        return merged
    }
}
