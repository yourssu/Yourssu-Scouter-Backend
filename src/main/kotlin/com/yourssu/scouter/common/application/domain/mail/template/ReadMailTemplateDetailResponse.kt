package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType

data class ReadMailTemplateDetailResponse(
    val id: Long,
    val title: String,
    val bodyHtml: String,
    val variables: List<DetailVariable>,
    val updatedAt: java.time.LocalDateTime,
) {
    data class DetailVariable(
        val key: String,
        val type: VariableType,
        val displayName: String,
        val perRecipient: Boolean,
    ) {
        companion object {
            fun from(variable: TemplateVariable): DetailVariable = DetailVariable(
                key = variable.key,
                type = variable.type,
                displayName = variable.displayName,
                perRecipient = variable.perRecipient,
            )
        }
    }

    companion object {
        fun from(template: MailTemplate): ReadMailTemplateDetailResponse = ReadMailTemplateDetailResponse(
            id = template.id!!,
            title = template.title,
            bodyHtml = template.bodyHtml,
            variables = template.variables.map { DetailVariable.from(it) },
            updatedAt = template.updatedAt!!,
        )
    }
}
