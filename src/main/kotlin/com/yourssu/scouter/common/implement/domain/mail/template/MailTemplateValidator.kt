package com.yourssu.scouter.common.implement.domain.mail.template

import com.yourssu.scouter.common.implement.support.exception.InvalidTemplateException

object MailTemplateValidator {

    // 숫자 형식: var-{숫자}
    private val numericPattern = Regex("^var-\\d+$")

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

            // 숫자 형식 검증: var-{숫자}
            if (!numericPattern.matches(v.key)) {
                throw InvalidTemplateException(
                    "Key '${'$'}{v.key}' must follow the format 'var-{number}' (e.g., 'var-1762579979965')"
                )
            }

            // 모든 변수는 type이 필수 (null 없음)
            // requiresUserInput과 type의 일관성 검증
            if (v.requiresUserInput && !v.type.isUserInputType()) {
                throw InvalidTemplateException(
                    "User-input variable '${'$'}{v.key}' must have a user-input type (${'$'}{VariableType.USER_INPUT_TYPES.joinToString()}), but got ${'$'}{v.type}"
                )
            }

            if (!v.requiresUserInput && !v.type.isAutoFillType()) {
                throw InvalidTemplateException(
                    "Auto-filled variable '${'$'}{v.key}' must have an auto-fill type (${'$'}{VariableType.AUTO_FILL_TYPES.joinToString()}), but got ${'$'}{v.type}"
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
