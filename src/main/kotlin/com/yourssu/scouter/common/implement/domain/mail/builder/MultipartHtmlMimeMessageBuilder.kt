package com.yourssu.scouter.common.implement.domain.mail.builder

import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilder
import jakarta.activation.DataHandler
import jakarta.mail.Multipart
import jakarta.mail.Session
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage

import jakarta.mail.internet.MimeMultipart
import jakarta.mail.util.ByteArrayDataSource
import org.springframework.stereotype.Component

@Component
class MultipartHtmlMimeMessageBuilder(
    private val helper: MimeMessageCommonHeaderApplier,
) : MimeMessageBuilder {

    override fun build(mailData: MailData, session: Session): MimeMessage {
        val mimeMessage = MimeMessage(session)
        helper.applyHeader(mimeMessage, mailData)
        mimeMessage.setContent(buildMultipart(mailData))

        return mimeMessage
    }
    private fun buildMultipart(mailData: MailData): Multipart {
        return MimeMultipart("related").apply {
            addBodyPart(buildHtmlPart(mailData.mailBody))
            mailData.inlineImages.forEach { addBodyPart(buildInlineImagePart(it.key, it.value)) }
            mailData.attachments.forEach { addBodyPart(buildAttachmentPart(it.key, it.value)) }
        }
    }

    private fun buildHtmlPart(body: String): MimeBodyPart {
        return MimeBodyPart().apply {
            setContent(body, "text/html; charset=utf-8")
        }
    }

    private fun buildInlineImagePart(contentId: String, dataSource: ByteArrayDataSource): MimeBodyPart {
        return MimeBodyPart().apply {
            setHeader("Content-ID", "<$contentId>")
            disposition = MimeBodyPart.INLINE
            dataHandler = DataHandler(dataSource)
        }
    }

    private fun buildAttachmentPart(name: String, dataSource: ByteArrayDataSource): MimeBodyPart {
        return MimeBodyPart().apply {
            fileName = name
            dataHandler = DataHandler(dataSource)
        }
    }
}
