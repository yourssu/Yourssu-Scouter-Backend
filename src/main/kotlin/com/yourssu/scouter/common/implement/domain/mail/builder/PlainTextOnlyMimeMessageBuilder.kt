package com.yourssu.scouter.common.implement.domain.mail.builder

import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilder
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.springframework.stereotype.Component

@Component
class PlainTextOnlyMimeMessageBuilder(
    private val helper: MimeMessageCommonHeaderApplier
) : MimeMessageBuilder {

    override fun build(mailData: MailData, session: Session): MimeMessage {
        val mimeMessage = MimeMessage(session)
        helper.applyHeader(mimeMessage, mailData)
        mimeMessage.setText(mailData.mailBody, "UTF-8")

        return mimeMessage
    }
}
