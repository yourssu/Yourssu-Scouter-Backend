package com.yourssu.scouter.common.implement.domain.mail.template

@JvmInline
value class RenderableText(val raw: String) {

    fun extractKeys(): Set<String> = MailPlaceholderParser.extractKeys(raw)

    fun substitute(values: Map<String, String>): String = MailPlaceholderParser.substitute(raw, values)
}
