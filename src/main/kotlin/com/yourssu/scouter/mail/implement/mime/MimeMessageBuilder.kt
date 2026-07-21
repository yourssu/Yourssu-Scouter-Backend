package com.yourssu.scouter.mail.implement.mime

import com.yourssu.scouter.mail.business.MailData
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage

interface MimeMessageBuilder {
    fun build(mailData: MailData, session: Session): MimeMessage
}
