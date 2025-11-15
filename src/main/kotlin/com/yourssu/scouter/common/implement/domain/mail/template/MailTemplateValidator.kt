package com.yourssu.scouter.common.implement.domain.mail.template

import com.yourssu.scouter.common.implement.support.exception.InvalidTemplateException

object MailTemplateValidator {

    private val fixedKeys: Set<String> = setOf("applicant", "partName")

    fun validate(template: MailTemplate) {
        validateVariables(template.variables)
        validateNoDuplicateKeys(template.variables)
        validateBodyConsistency(template.bodyHtml, template.variables)
    }

    private fun validateVariables(variables: List<TemplateVariable>) {
        variables.forEach { v ->
            val isFixed = fixedKeys.contains(v.key)
            val isVar = v.key.startsWith("var-")
            if (isFixed && v.type != null) {
                throw InvalidTemplateException("Fixed key '${'$'}{v.key}' must not have a type")
            }
            if (isVar && v.type == null) {
                throw InvalidTemplateException("Variable key '${'$'}{v.key}' must have a type")
            }
            if (!isFixed && !isVar) {
                throw InvalidTemplateException("Key '${'$'}{v.key}' must be one of fixed keys ${'$'}fixedKeys or start with 'var-'")
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
