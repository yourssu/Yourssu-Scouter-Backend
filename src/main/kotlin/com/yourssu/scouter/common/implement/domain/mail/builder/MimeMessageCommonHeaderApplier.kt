package com.yourssu.scouter.common.implement.domain.mail.builder

import com.yourssu.scouter.common.business.domain.mail.MailData
import jakarta.mail.Address
import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.springframework.stereotype.Component

@Component
class MimeMessageCommonHeaderApplier {

    fun applyHeader(message: MimeMessage, mailData: MailData) {
        message.setFrom(InternetAddress(mailData.senderEmailAddress))
        message.setRecipients(Message.RecipientType.TO, mailData.receiverEmailAddresses.toInternetAddresses())
        message.setRecipients(Message.RecipientType.CC, mailData.ccEmailAddresses.toInternetAddresses())
        message.setRecipients(Message.RecipientType.BCC, mailData.bccEmailAddresses.toInternetAddresses())
        message.setSubject(mailData.mailSubject, "UTF-8")
    }

    private fun List<String>.toInternetAddresses(): Array<Address> {
        return this.map { InternetAddress(it) }.toTypedArray()
    }
}
