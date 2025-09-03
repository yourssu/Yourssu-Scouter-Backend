package com.yourssu.scouter.common.implement.domain.mail.template

import java.time.LocalDateTime

class MailTemplate(
    val id: Long? = null,
    val title: String,
    val bodyHtml: String,
    val variables: List<TemplateVariable> = emptyList(),
    val createdBy: Long,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
