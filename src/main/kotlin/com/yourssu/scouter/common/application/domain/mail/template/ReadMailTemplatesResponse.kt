package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate

data class ReadMailTemplateSummaryResponse(
    val id: Long,
    val title: String,
    val updatedAt: java.time.LocalDateTime,
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
