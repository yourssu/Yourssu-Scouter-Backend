package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailReserveRequestTest {
    @Test
    fun `toCommand는 기본 본문 형식을 변환한다`() {
        val request = createRequest()

        val command = request.toCommand(userId = 1L)

        assertThat(command.bodyFormat).isEqualTo(MailBodyFormat.HTML)
    }

    @Test
    fun `toCommand는 파일 참조 정보를 도메인 참조로 변환한다`() {
        val request =
            MailReserveRequest(
                receiverEmailAddresses = listOf("receiver@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "<p>본문</p>",
                bodyFormat = "HTML",
                reservationTime = Instant.parse("2026-02-14T00:00:00Z"),
                attachmentReferences =
                    listOf(
                        MailReserveRequest.AttachmentReferenceRequest(
                            fileId = 102L,
                        ),
                    ),
            )

        val command = request.toCommand(1L)

        assertThat(command.attachmentReferences).hasSize(1)
        assertThat(command.attachmentReferences[0].fileId).isEqualTo(102L)
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
