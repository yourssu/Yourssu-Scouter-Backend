package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.business.MailBodyFormat
import com.yourssu.scouter.mail.core.business.MailData
import com.yourssu.scouter.mail.core.implement.MultipartHtmlMimeMessageBuilder
import com.yourssu.scouter.mail.core.implement.PlainTextOnlyMimeMessageBuilder
import com.yourssu.scouter.mail.core.implement.SimpleHtmlMimeMessageBuilder
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
