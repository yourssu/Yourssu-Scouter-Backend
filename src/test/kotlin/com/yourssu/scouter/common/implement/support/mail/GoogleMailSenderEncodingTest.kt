package com.yourssu.scouter.common.implement.support.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilderResolver
import com.yourssu.scouter.common.implement.domain.mail.builder.MimeMessageCommonHeaderApplier
import com.yourssu.scouter.common.implement.domain.mail.builder.MultipartHtmlMimeMessageBuilder
import com.yourssu.scouter.common.implement.domain.mail.builder.PlainTextOnlyMimeMessageBuilder
import com.yourssu.scouter.common.implement.domain.mail.builder.SimpleHtmlMimeMessageBuilder
import com.yourssu.scouter.common.implement.support.google.GoogleMailClient
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import jakarta.mail.util.ByteArrayDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.Properties

@Suppress("NonAsciiCharacters")
class GoogleMailSenderEncodingTest {

    private val googleMailClient: GoogleMailClient = mock()

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

    private val googleMailSender =
        GoogleMailSender(
            googleMailClient,
            builderResolver,
        )

    @Test
    fun `한글 제목의 HTML 메일이 Base64 인코딩과 디코딩 후에도 제목이 깨지지 않는다`() {
        // given
        val mailData =
            MailData(
                senderEmailAddress = "sender@example.com",
                receiverEmailAddresses = listOf("to@example.com"),
                mailSubject = "정시에 보내져야함",
                mailBody = "<p>본문</p>",
                bodyFormat = MailBodyFormat.HTML,
            )

        // when
        googleMailSender.send(mailData, "Bearer dummy-token")

        // then
        val encodedCaptor = argumentCaptor<String>()
        val tokenCaptor = argumentCaptor<String>()

        verify(googleMailClient).sendEmail(encodedCaptor.capture(), tokenCaptor.capture())
        assertThat(tokenCaptor.firstValue).isEqualTo("Bearer dummy-token")

        val decodedBytes = Base64.getUrlDecoder().decode(encodedCaptor.firstValue)

        val session = Session.getInstance(Properties(), null)
        val parsedMessage = MimeMessage(session, ByteArrayInputStream(decodedBytes))

        assertThat(parsedMessage.subject).isEqualTo(mailData.mailSubject)
    }

    @Test
    fun `한글 제목의 HTML_첨부_메일이 Base64 인코딩과 디코딩 후에도 제목이 깨지지 않는다`() {
        // given
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

        // when
        googleMailSender.send(mailData, "Bearer dummy-token")

        // then
        val encodedCaptor = argumentCaptor<String>()

        verify(googleMailClient).sendEmail(encodedCaptor.capture(), any())

        val decodedBytes = Base64.getUrlDecoder().decode(encodedCaptor.firstValue)

        val session = Session.getInstance(Properties(), null)
        val parsedMessage = MimeMessage(session, ByteArrayInputStream(decodedBytes))

        assertThat(parsedMessage.subject).isEqualTo(mailData.mailSubject)
    }
}

