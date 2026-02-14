package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailReserveCommandTest {
    @Test
    fun `toMail은 첨부파일과 인라인이미지를 파일명 기준으로 매핑한다`() {
        val inlineImage = MockMultipartFile("inlineImages", "logo.png", "image/png", "png-bytes".toByteArray())
        val attachment = MockMultipartFile("attachments", "notice.pdf", "application/pdf", "pdf-bytes".toByteArray())
        val command =
            createCommand(
                inlineImages = listOf(inlineImage),
                attachments = listOf(attachment),
            )

        val mail = command.toMail("sender@example.com")

        assertThat(mail.inlineImages.keys).containsExactly("logo.png")
        assertThat(mail.attachments.keys).containsExactly("notice.pdf")
        assertThat(mail.inlineImages["logo.png"]?.contentType).contains("image/png")
        assertThat(mail.attachments["notice.pdf"]?.contentType).contains("application/pdf")
    }

    @Test
    fun `toMail은 originalFilename이 없으면 파트 이름을 사용한다`() {
        val attachment = mock<MultipartFile>()
        whenever(attachment.name).thenReturn("attachments")
        whenever(attachment.originalFilename).thenReturn(null)
        whenever(attachment.contentType).thenReturn("application/octet-stream")
        whenever(attachment.inputStream).thenReturn(ByteArrayInputStream("bin".toByteArray()))
        val command = createCommand(attachments = listOf(attachment))

        val mail = command.toMail("sender@example.com")

        assertThat(mail.attachments.keys).containsExactly("attachments")
    }

    private fun createCommand(
        inlineImages: List<MockMultipartFile> = emptyList(),
        attachments: List<MultipartFile> = emptyList(),
    ): MailReserveCommand {
        return MailReserveCommand(
            senderUserId = 1L,
            receiverEmailAddresses = listOf("receiver@example.com"),
            ccEmailAddresses = emptyList(),
            bccEmailAddresses = emptyList(),
            mailSubject = "제목",
            mailBody = "<p>본문</p>",
            bodyFormat = MailBodyFormat.HTML,
            inlineImages = inlineImages,
            attachments = attachments,
            reservationTime = Instant.parse("2026-02-14T00:00:00Z"),
        )
    }
}
