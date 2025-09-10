package com.yourssu.scouter.common.implement.domain.mail.template

data class TemplateVariable(
    val key: String,
    val type: VariableType?,
    val displayName: String,
    val perRecipient: Boolean,
)
