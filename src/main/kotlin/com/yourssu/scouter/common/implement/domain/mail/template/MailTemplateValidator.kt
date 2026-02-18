package com.yourssu.scouter.common.implement.domain.mail.template

import com.yourssu.scouter.common.implement.support.exception.InvalidTemplateException

object MailTemplateValidator {

    // UUID 형식: var-{UUID} (8-4-4-4-12, 하이픈 포함)
    private val uuidPattern = Regex("^var-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", RegexOption.IGNORE_CASE)

    fun validate(template: MailTemplate) {
        validateVariables(template.variables)
        validateNoDuplicateKeys(template.variables)
        validateBodyConsistency(template.bodyHtml, template.variables)
    }

    private fun validateVariables(variables: List<TemplateVariable>) {
        variables.forEach { v ->
            // 모든 키는 var-로 시작해야 함
            if (!v.key.startsWith("var-")) {
                throw InvalidTemplateException("Key '${'$'}{v.key}' must start with 'var-'")
            }

            // UUID 형식 검증: var-{UUID}
            if (!uuidPattern.matches(v.key)) {
                throw InvalidTemplateException(
                    "Key '${'$'}{v.key}' must follow the format 'var-{UUID}' (e.g., 'var-550e8400-e29b-41d4-a716-446655440000')"
                )
            }
        }
    }

    private fun validateNoDuplicateKeys(variables: List<TemplateVariable>) {
        val dup = variables.groupBy { it.key }.filter { it.value.size > 1 }.keys
        if (dup.isNotEmpty()) {
            throw InvalidTemplateException("Duplicate variable keys: ${'$'}dup")
        }
    }

    private fun extractKeysFromBody(bodyHtml: String): Set<String> {
        val regex = "\\{\\{(.*?)\\}}".toRegex()
        return regex.findAll(bodyHtml).map { it.groupValues[1].trim() }.toSet()
    }

    private fun validateBodyConsistency(bodyHtml: String, variables: List<TemplateVariable>) {
        val bodyKeys = extractKeysFromBody(bodyHtml)
        val variableKeys = variables.map { it.key }.toSet()

        val missingInVars = bodyKeys - variableKeys
        if (missingInVars.isNotEmpty()) {
            throw InvalidTemplateException("Variables missing for placeholders: ${'$'}missingInVars")
        }
    }
}
