package com.yourssu.scouter.common.implement.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailInlineImageReference
import java.time.Instant

class MailTemplate(
    val id: Long? = null,
    val title: String,
    val bodyHtml: String,
    val variables: List<TemplateVariable> = emptyList(),
    val inlineImageReferences: List<MailInlineImageReference> = emptyList(),
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
    val createdBy: Long,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
