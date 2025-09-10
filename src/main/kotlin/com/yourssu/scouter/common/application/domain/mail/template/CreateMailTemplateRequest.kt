package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateMailTemplateRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val bodyHtml: String,
    @field:NotNull
    val variables: List<TemplateVariableRequest> = emptyList(),
) {
    data class TemplateVariableRequest(
        val key: String,
        val type: VariableType?,
        val displayName: String,
        val perRecipient: Boolean,
    )

    fun toDomain(createdBy: Long): MailTemplate {
        return MailTemplate(
            title = title,
            bodyHtml = bodyHtml,
            variables = variables.map { TemplateVariable(it.key, it.type, it.displayName, it.perRecipient) },
            createdBy = createdBy,
        )
    }
}
