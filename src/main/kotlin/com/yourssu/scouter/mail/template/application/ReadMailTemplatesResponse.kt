package com.yourssu.scouter.mail.template.application

import com.yourssu.scouter.mail.template.implement.MailTemplate

data class ReadMailTemplateSummaryResponse(
    val id: Long,
    val title: String,
    val updatedAt: java.time.Instant,
) {
    companion object {
        fun from(template: MailTemplate): ReadMailTemplateSummaryResponse = ReadMailTemplateSummaryResponse(
            id = template.id!!,
            title = template.title,
            updatedAt = template.updatedAt!!,
        )
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
