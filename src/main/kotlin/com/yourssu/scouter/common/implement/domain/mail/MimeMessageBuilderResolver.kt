package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.builder.MultipartHtmlMimeMessageBuilder
import com.yourssu.scouter.common.implement.domain.mail.builder.PlainTextOnlyMimeMessageBuilder
import com.yourssu.scouter.common.implement.domain.mail.builder.SimpleHtmlMimeMessageBuilder
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

        if (mailData.inlineImages.isEmpty() && mailData.attachments.isEmpty()) {
            return simpleHtmlBuilder
        }

        return multipartHtmlBuilder
    }
}
