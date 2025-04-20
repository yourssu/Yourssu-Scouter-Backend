package com.yourssu.scouter.common.implement.domain.mail.builder

import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilder
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
