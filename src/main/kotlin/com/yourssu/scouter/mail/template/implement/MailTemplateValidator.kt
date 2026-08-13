package com.yourssu.scouter.mail.template.implement
import com.yourssu.scouter.mail.template.support.MailPlaceholderParser

import com.yourssu.scouter.mail.template.implement.RecipientAttributeResolver
import com.yourssu.scouter.mail.support.exception.InvalidTemplateException
import org.springframework.stereotype.Component

@Component
class MailTemplateValidator(
    private val recipientAttributeResolver: RecipientAttributeResolver,
) {

    fun validate(template: MailTemplate) {
        validateVariableKeys(template.variables)
        validateNoDuplicateKeys(template.variables)
        validatePlaceholderConsistency(template)
        validateApplicantBoundKeys(template.variables)
        validateUserInputTypes(template.variables)
    }

    private fun validateVariableKeys(variables: List<TemplateVariable>) {
        variables.forEach { v ->
            if (!MailPlaceholderParser.PLACEHOLDER_REGEX.containsMatchIn("{{${v.key}}}")) {
                throw InvalidTemplateException(
                    "변수 키 형식이 올바르지 않습니다: ${v.key} (예: var-550e8400-e29b-41d4-a716-446655440000)"
                )
            }
        }
    }

    private fun validateNoDuplicateKeys(variables: List<TemplateVariable>) {
        val duplicates = variables.groupBy { it.key }.filter { it.value.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw InvalidTemplateException("중복된 변수 키: $duplicates")
        }
    }

    private fun validatePlaceholderConsistency(template: MailTemplate) {
        val placeholderKeys = MailPlaceholderParser.extractKeys(template.subject.raw) +
            MailPlaceholderParser.extractKeys(template.bodyHtml)
        val variableKeys = template.variables.map { it.key }.toSet()

        val undeclared = placeholderKeys - variableKeys
        if (undeclared.isNotEmpty()) {
            throw InvalidTemplateException("선언되지 않은 플레이스홀더: $undeclared")
        }

        val unused = variableKeys - placeholderKeys
        if (unused.isNotEmpty()) {
            throw InvalidTemplateException("subject 또는 body에 사용되지 않는 변수: $unused")
        }
    }

    private fun validateApplicantBoundKeys(variables: List<TemplateVariable>) {
        val available = recipientAttributeResolver.availableKeys()
        variables.filterIsInstance<TemplateVariable.ApplicantBound>()
            .forEach { v ->
                if (v.attributeKey !in available) {
                    throw InvalidTemplateException(
                        "유효하지 않은 attributeKey: ${v.attributeKey} (가능한 값: $available)"
                    )
                }
            }
    }

    private fun validateUserInputTypes(variables: List<TemplateVariable>) {
        variables.filterIsInstance<TemplateVariable.UserInput>()
            .forEach { v ->
                if (!v.type.isUserInputType()) {
                    throw InvalidTemplateException("UserInput 변수에 AUTO_FILL 타입 사용 불가: ${v.type}")
                }
            }
    }
}
