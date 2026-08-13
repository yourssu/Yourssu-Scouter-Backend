package com.yourssu.scouter.mail.template.implement
import com.yourssu.scouter.mail.support.utils.MailPlaceholderParser

@JvmInline
value class RenderableText(val raw: String) {

    fun extractKeys(): Set<String> = MailPlaceholderParser.extractKeys(raw)

    fun substitute(values: Map<String, String>): String = MailPlaceholderParser.substitute(raw, values)
}
