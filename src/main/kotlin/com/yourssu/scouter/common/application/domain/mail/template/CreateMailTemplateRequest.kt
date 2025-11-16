package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateMailTemplateRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    @field:Size(max = 100_000, message = "메일 템플릿 본문은 최대 100000자까지 입력 가능합니다")
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
