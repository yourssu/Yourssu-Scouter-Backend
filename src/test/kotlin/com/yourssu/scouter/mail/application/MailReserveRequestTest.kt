package com.yourssu.scouter.mail.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailReserveRequestTest {
    @Test
    fun `toCommand는 기본 본문 형식을 변환한다`() {
        val request = createRequest()

        val command = request.toCommand(userId = 1L)

        assertThat(command.senderUserId).isEqualTo(1L)
        assertThat(command.templateId).isEqualTo(42L)
        assertThat(command.reservationTime).isEqualTo(Instant.parse("2026-02-14T00:00:00Z"))
        assertThat(command.sharedBindings).containsEntry("company", "yourssu")
        assertThat(command.ccEmailAddresses).containsExactly("cc@example.com")
        assertThat(command.bccEmailAddresses).containsExactly("bcc@example.com")
        assertThat(command.recipients).hasSize(1)
    }

    @Test
    fun `toCommand는 파일 참조 정보를 도메인 참조로 변환한다`() {
        val request =
            MailReserveRequest(
                templateId = 42L,
                reservationTime = Instant.parse("2026-02-14T00:00:00Z"),
                recipients =
                    listOf(
                        MailReserveRequest.RecipientRequest(
                            email = "receiver@example.com",
                            applicantId = 102L,
                            bindings = mapOf("name" to "지원자"),
                        ),
                    ),
            )

        val command = request.toCommand(1L)

        assertThat(command.recipients).hasSize(1)
        assertThat(command.recipients[0].applicantId).isEqualTo(102L)
        assertThat(command.recipients[0].bindings).containsEntry("name", "지원자")
    }

    private fun createRequest(): MailReserveRequest {
        return MailReserveRequest(
            templateId = 42L,
            reservationTime = Instant.parse("2026-02-14T00:00:00Z"),
            recipients =
                listOf(
                    MailReserveRequest.RecipientRequest(
                        email = "receiver@example.com",
                        applicantId = 101L,
                    ),
                ),
            sharedBindings = mapOf("company" to "yourssu"),
            ccEmailAddresses = listOf("cc@example.com"),
            bccEmailAddresses = listOf("bcc@example.com"),
        )
    }
}
