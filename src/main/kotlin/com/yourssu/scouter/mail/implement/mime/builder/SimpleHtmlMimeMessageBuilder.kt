package com.yourssu.scouter.mail.implement.mime.builder

import com.yourssu.scouter.mail.business.MailData
import com.yourssu.scouter.mail.implement.mime.MimeMessageBuilder
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.springframework.stereotype.Component

@Component
class SimpleHtmlMimeMessageBuilder(
    private val helper: MimeMessageCommonHeaderApplier,
) : MimeMessageBuilder {

    override fun build(mailData: MailData, session: Session): MimeMessage {
        return MimeMessage(session).apply {
            helper.applyHeader(this, mailData)
            setContent(mailData.mailBody, "text/html; charset=utf-8")
        }
    }
}
