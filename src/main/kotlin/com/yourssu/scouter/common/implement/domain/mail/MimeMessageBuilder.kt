package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailData
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage

interface MimeMessageBuilder {
    fun build(mailData: MailData, session: Session): MimeMessage
}
