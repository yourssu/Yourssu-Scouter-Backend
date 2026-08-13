package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.business.MailData
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage

interface MimeMessageBuilder {
    fun build(mailData: MailData, session: Session): MimeMessage
}
