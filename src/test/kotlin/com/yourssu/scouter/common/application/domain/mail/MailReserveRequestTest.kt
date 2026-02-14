package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailReserveRequestTest {
    @Test
    fun `toCommand는 첨부파일이 없으면 빈 리스트로 변환한다`() {
        val request = createRequest()

        val command =
            request.toCommand(
                userId = 1L,
                request = request,
                inlineImages = null,
                attachments = null,
            )

        assertThat(command.inlineImages).isEmpty()
        assertThat(command.attachments).isEmpty()
        assertThat(command.bodyFormat).isEqualTo(MailBodyFormat.HTML)
    }

    @Test
    fun `toCommand는 첨부파일이 있으면 그대로 전달한다`() {
        val request = createRequest()
        val inlineImage = MockMultipartFile("inlineImages", "banner.png", "image/png", "img".toByteArray())
        val attachment = MockMultipartFile("attachments", "guide.pdf", "application/pdf", "pdf".toByteArray())

        val command =
            request.toCommand(
                userId = 1L,
                request = request,
                inlineImages = listOf(inlineImage),
                attachments = listOf(attachment),
            )

        assertThat(command.inlineImages).containsExactly(inlineImage)
        assertThat(command.attachments).containsExactly(attachment)
    }

    private fun createRequest(): MailReserveRequest {
        return MailReserveRequest(
            receiverEmailAddresses = listOf("receiver@example.com"),
            ccEmailAddresses = emptyList(),
            bccEmailAddresses = emptyList(),
            mailSubject = "제목",
            mailBody = "<p>본문</p>",
            bodyFormat = "HTML",
            reservationTime = Instant.parse("2026-02-14T00:00:00Z"),
        )
    }
}
