package com.yourssu.scouter.common.implement.support.mail

import com.yourssu.scouter.common.business.domain.mail.MailSender
import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilder
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilderResolver
import com.yourssu.scouter.common.implement.support.google.GoogleMailClient
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import java.io.ByteArrayOutputStream
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GoogleMailSender(
    private val googleMailClient: GoogleMailClient,
    private val messageBuilderResolver: MimeMessageBuilderResolver,
) : MailSender {

    private val log = LoggerFactory.getLogger(GoogleMailSender::class.java)

    override fun send(mailData: MailData, accessToken: String) {
        if (log.isDebugEnabled) {
            log.debug(
                "메일 발송 시도: subject=[{}], subjectBytes={}",
                mailData.mailSubject,
                mailData.mailSubject.toByteArray(Charsets.UTF_8).contentToString(),
            )
        }

        val messageBuilder: MimeMessageBuilder = messageBuilderResolver.resolve(mailData)

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.mime.charset", "UTF-8")
            put("mail.mime.allowutf8", "true")
        }
        val session = Session.getInstance(properties, null)
        val message: MimeMessage = messageBuilder.build(mailData, session)
        val encodedEmail: String = encodeMessage(message)

        googleMailClient.sendEmail(encodedEmail, accessToken)
    }

    private fun encodeMessage(message: MimeMessage): String {
        val outputStream = ByteArrayOutputStream()
        message.writeTo(outputStream)

        val encoder: Base64.Encoder = Base64.getUrlEncoder()

        return encoder.encodeToString(outputStream.toByteArray())
    }
}
