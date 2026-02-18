package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate

data class CreateMailTemplateResponse(
    val id: Long,
    val title: String,
    val updatedAt: java.time.Instant,
) {
    companion object {
        fun from(template: MailTemplate): CreateMailTemplateResponse {
            return CreateMailTemplateResponse(
                id = template.id!!,
                title = template.title,
                updatedAt = template.updatedAt!!,
            )
        }
    }
}
