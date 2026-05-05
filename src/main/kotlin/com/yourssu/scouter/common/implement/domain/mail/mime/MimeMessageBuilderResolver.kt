package com.yourssu.scouter.common.implement.domain.mail.mime

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.mime.builder.MultipartHtmlMimeMessageBuilder
import com.yourssu.scouter.common.implement.domain.mail.mime.builder.PlainTextOnlyMimeMessageBuilder
import com.yourssu.scouter.common.implement.domain.mail.mime.builder.SimpleHtmlMimeMessageBuilder
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
