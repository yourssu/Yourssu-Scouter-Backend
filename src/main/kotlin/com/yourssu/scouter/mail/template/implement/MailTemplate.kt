package com.yourssu.scouter.mail.template.implement
import com.yourssu.scouter.mail.support.utils.MailPlaceholderParser

import com.yourssu.scouter.mail.core.business.MailBodyFormat
import com.yourssu.scouter.mail.core.implement.Mail
import com.yourssu.scouter.mail.core.implement.MailAttachmentReference
import com.yourssu.scouter.mail.support.exception.InvalidMailRenderingException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MailTemplate(
    val id: Long? = null,
    val title: String,
    val subject: RenderableText,
    val bodyHtml: String,
    val variables: List<TemplateVariable> = emptyList(),
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
    val createdBy: Long,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    fun createMail(mailRenderingContext: MailRenderContext): Mail {
        MailRenderer.validate(this, mailRenderingContext)
        val renderedMail = MailRenderer.render(this, mailRenderingContext)

        return Mail(
            receiverEmailAddress = mailRenderingContext.recipientEmail,
            ccEmailAddresses = mailRenderingContext.ccEmails,
            bccEmailAddresses = mailRenderingContext.bccEmails,
            mailSubject = renderedMail.subject,
            mailBody = renderedMail.bodyHtml,
            bodyFormat = MailBodyFormat.HTML,
            attachmentReferences = attachmentReferences,
        )
    }

    fun createMailList(mailRenderingContextList: List<MailRenderContext>): List<Mail> {
        return mailRenderingContextList.map(::createMail)
    }

    fun validateAttachmentReferences() {
        attachmentReferences.forEach { ref ->
            require(ref.storageKey.isNotBlank()) { "첨부파일 storageKey는 비어 있을 수 없습니다." }
            require(ref.fileName.isNotBlank()) { "첨부파일 fileName은 비어 있을 수 없습니다." }
            require(ref.contentType.isNotBlank()) { "첨부파일 contentType은 비어 있을 수 없습니다." }
        }
    }
}

private object MailRenderer {
    fun validate(template: MailTemplate, context: MailRenderContext) {
        template.variables.forEach { variable ->
            val recipientValue = context.recipientBindings[variable.key]
            val sharedValue = context.sharedBindings[variable.key]
            val isError = when (variable) {
                is TemplateVariable.UserInput if variable.perRecipient -> recipientValue == null
                is TemplateVariable.UserInput -> sharedValue == null
                else -> recipientValue != null || sharedValue != null
            }
            if (isError) throw InvalidMailRenderingException()
        }
    }

    private val dateDisplayFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneId.of("Asia/Seoul"))

    fun render(template: MailTemplate, context: MailRenderContext): RenderedMail {
        val values = template.variables.associate { variable ->
            val value = when (variable) {
                is TemplateVariable.UserInput if variable.perRecipient -> context.recipientBindings[variable.key]
                is TemplateVariable.UserInput -> context.sharedBindings[variable.key]
                is TemplateVariable.PartName -> context.recipientAttributes["applicant.part.name"]
                is TemplateVariable.ApplicantBound -> context.recipientAttributes[variable.attributeKey]
            } ?: error("Unresolved variable ${variable.key} for recipient ${context.recipientEmail}")
            val displayValue =
                if (variable is TemplateVariable.UserInput && variable.type == VariableType.DATE) {
                    formatDateValue(value)
                } else {
                    value
                }
            variable.key to displayValue
        }
        val renderedSubject = template.subject.substitute(values)
        val renderedBody = MailPlaceholderParser.substitute(template.bodyHtml, values)

        return RenderedMail(
            recipientEmail = context.recipientEmail,
            subject = renderedSubject,
            bodyHtml = renderedBody,
        )
    }

    private fun formatDateValue(raw: String): String {
        return try {
            dateDisplayFormat.format(Instant.parse(raw))
        } catch (e: DateTimeParseException) {
            raw
        }
    }

    data class RenderedMail(
        val recipientEmail: String,
        val subject: String,
        val bodyHtml: String,
    )
}
