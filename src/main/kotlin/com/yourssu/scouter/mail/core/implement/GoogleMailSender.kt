package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.business.MailData
import com.yourssu.scouter.mail.core.business.MailSender
import com.yourssu.scouter.mail.core.implement.MimeMessageBuilder
import com.yourssu.scouter.mail.core.implement.MimeMessageBuilderResolver
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.MimeMessage
import java.util.Properties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GoogleMailSender(
    private val messageBuilderResolver: MimeMessageBuilderResolver,
    private val mailSenderProperties: MailSenderProperties,
) : MailSender {

    private val log = LoggerFactory.getLogger(GoogleMailSender::class.java)

    override fun send(mailData: MailData) {
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
        log.info("SMTP 인증 시도: email={}", mailSenderProperties.email)
        val authenticator = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication =
                PasswordAuthentication(mailSenderProperties.email, mailSenderProperties.appPassword)
        }
        val session = Session.getInstance(properties, authenticator)
        val smtpMailData = mailData.copy(senderEmailAddress = mailSenderProperties.email)
        val message: MimeMessage = messageBuilder.build(smtpMailData, session)
        Transport.send(message)
    }
}
