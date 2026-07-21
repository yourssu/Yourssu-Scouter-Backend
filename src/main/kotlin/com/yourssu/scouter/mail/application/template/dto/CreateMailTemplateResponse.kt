package com.yourssu.scouter.mail.application.template.dto

import com.yourssu.scouter.mail.implement.template.MailTemplate

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
