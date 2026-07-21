package com.yourssu.scouter.mail.support.implement.mail

import com.yourssu.scouter.mail.business.MailBodyFormat
import com.yourssu.scouter.mail.business.MailData
import com.yourssu.scouter.mail.implement.mime.MimeMessageBuilderResolver
import com.yourssu.scouter.mail.implement.mime.builder.MimeMessageCommonHeaderApplier
import com.yourssu.scouter.mail.implement.mime.builder.MultipartHtmlMimeMessageBuilder
import com.yourssu.scouter.mail.implement.mime.builder.PlainTextOnlyMimeMessageBuilder
import com.yourssu.scouter.mail.implement.mime.builder.SimpleHtmlMimeMessageBuilder
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import jakarta.mail.util.ByteArrayDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Properties

@Suppress("NonAsciiCharacters")
class GoogleMailSenderEncodingTest {

    private val helper = MimeMessageCommonHeaderApplier()
    private val plainTextBuilder = PlainTextOnlyMimeMessageBuilder(helper)
    private val simpleHtmlBuilder = SimpleHtmlMimeMessageBuilder(helper)
    private val multipartHtmlBuilder = MultipartHtmlMimeMessageBuilder(helper)
    private val builderResolver =
        MimeMessageBuilderResolver(
            plainTextBuilder,
            simpleHtmlBuilder,
            multipartHtmlBuilder,
        )

    private fun buildMessage(mailData: MailData): MimeMessage {
        val session = Session.getInstance(Properties(), null)
        return builderResolver.resolve(mailData).build(mailData, session)
    }

    @Test
    fun `한글 제목의 HTML 메일이 MimeMessage로 빌드된 후에도 제목이 깨지지 않는다`() {
        val mailData =
            MailData(
                senderEmailAddress = "sender@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                mailSubject = "정시에 보내져야함",
                mailBody = "<p>본문</p>",
                bodyFormat = MailBodyFormat.HTML,
            )

        val message = buildMessage(mailData)

        assertThat(message.subject).isEqualTo(mailData.mailSubject)
    }

    @Test
    fun `한글 제목의 HTML_첨부_메일이 MimeMessage로 빌드된 후에도 제목이 깨지지 않는다`() {
        val mailData =
            MailData(
                senderEmailAddress = "sender@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                mailSubject = "정시에 보내져야함",
                mailBody = "<p>본문</p>",
                bodyFormat = MailBodyFormat.HTML,
                attachments =
                    mapOf(
                        "image.png" to ByteArrayDataSource("dummy".toByteArray(), "image/png"),
                    ),
            )

        val message = buildMessage(mailData)

        assertThat(message.subject).isEqualTo(mailData.mailSubject)
    }
}
