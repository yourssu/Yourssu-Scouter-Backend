package com.yourssu.scouter.mail.implement.mime

import com.yourssu.scouter.mail.business.MailBodyFormat
import com.yourssu.scouter.mail.business.MailData
import com.yourssu.scouter.mail.implement.mime.builder.MultipartHtmlMimeMessageBuilder
import com.yourssu.scouter.mail.implement.mime.builder.PlainTextOnlyMimeMessageBuilder
import com.yourssu.scouter.mail.implement.mime.builder.SimpleHtmlMimeMessageBuilder
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
