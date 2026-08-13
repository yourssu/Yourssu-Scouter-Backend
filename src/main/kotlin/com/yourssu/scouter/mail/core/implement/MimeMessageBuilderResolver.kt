package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.business.MailBodyFormat
import com.yourssu.scouter.mail.core.business.MailData
import org.springframework.stereotype.Component

@Component
class MimeMessageBuilderResolver(
    private val plainTextBuilder: PlainTextOnlyMimeMessageBuilder,
    private val simpleHtmlBuilder: SimpleHtmlMimeMessageBuilder,
    private val multipartHtmlBuilder: MultipartHtmlMimeMessageBuilder,
) {
    fun resolve(mailData: MailData): MimeMessageBuilder {
        if (mailData.bodyFormat == MailBodyFormat.PLAIN_TEXT) {
            return plainTextBuilder
        }

        if (mailData.attachments.isEmpty()) {
            return simpleHtmlBuilder
        }

        return multipartHtmlBuilder
    }
}
