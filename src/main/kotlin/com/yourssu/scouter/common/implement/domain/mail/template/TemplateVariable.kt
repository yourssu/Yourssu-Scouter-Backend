package com.yourssu.scouter.common.implement.domain.mail.template

data class TemplateVariable(
    val key: String,
    val type: VariableType,  // null 없음 - 항상 타입이 있음
    val displayName: String,
    val perRecipient: Boolean,
    val requiresUserInput: Boolean,
)
