package com.yourssu.scouter.mail.support.utils

object MailPlaceholderParser {

    val PLACEHOLDER_REGEX = Regex(
        """\{\{(var-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})}}""",
        RegexOption.IGNORE_CASE,
    )

    fun extractKeys(text: String): Set<String> =
        PLACEHOLDER_REGEX.findAll(text).map { it.groupValues[1] }.toSet()

    fun substitute(text: String, values: Map<String, String>): String =
        PLACEHOLDER_REGEX.replace(text) { match ->
            values[match.groupValues[1]] ?: match.value
        }
}
