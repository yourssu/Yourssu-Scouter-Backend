package com.yourssu.scouter.common.implement.support.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.business.domain.mail.MailData
import jakarta.mail.util.ByteArrayDataSource
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Suppress("NonAsciiCharacters")
class GoogleMailSenderTest(

    @Autowired
    private val googleMailSender: GoogleMailSender,
) {

    @Test
    @Disabled
    fun HTML본문_인라인이미지_첨부파일_메일_전송_테스트() {
        val body = """
            <p>안녕하세요,</p>
            <p>이메일 전송 기능 테스트입니다.</p>
            <img src="cid:logo" alt="유어슈 로고">
            <p>감사합니다.<br>- 권예진</p>
        """.trimIndent()

        val mailData = MailData(
            senderEmailAddress = "encho.urssu@gmail.com",
            receiverEmailAddresses = listOf("email1@gmail.com", "email2@naver.com"),
            ccEmailAddresses = listOf("cc1@gmail.com", "cc2@naver.com"),
            bccEmailAddresses = listOf("bcc1@gmail.com", "bcc2@naver.com"),
            mailSubject = "메일 전송 테스트",
            mailBody = body,
            bodyFormat = MailBodyFormat.HTML,
            inlineImages = mapOf(
                "logo" to ByteArrayDataSource(
                    this::class.java.getResourceAsStream("/static/logo.jpg")!!.readAllBytes(),
                    "image/png",
                ),
            ),
            attachments = mapOf(
                "테스트1.pdf" to ByteArrayDataSource(
                    this::class.java.getResourceAsStream("/static/테스트1.pdf")!!.readAllBytes(),
                    "application/pdf",
                ),
                "테스트2.zip" to ByteArrayDataSource(
                    this::class.java.getResourceAsStream("/static/테스트2.zip")!!.readAllBytes(),
                    "application/zip",
                ),
            ),
        )

        googleMailSender.send(
            mailData,
            "Bearer access-token"
        )
    }
}
