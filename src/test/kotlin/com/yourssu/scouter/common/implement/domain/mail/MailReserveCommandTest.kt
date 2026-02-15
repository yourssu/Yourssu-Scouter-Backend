package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@Suppress("NonAsciiCharacters")
class MailReserveCommandTest {
    @Test
    fun `toMail은 fileId 기반 참조 정보를 그대로 전달한다`() {
        val command =
            MailReserveCommand(
                senderUserId = 1L,
                receiverEmailAddresses = listOf("receiver@example.com"),
                ccEmailAddresses = emptyList(),
                bccEmailAddresses = emptyList(),
                mailSubject = "제목",
                mailBody = "<p>본문</p>",
                bodyFormat = MailBodyFormat.HTML,
                inlineImageReferences =
                    listOf(
                        MailInlineImageReference(
                            fileId = 10L,
                            contentId = "cid_logo",
                            fileName = "logo.png",
                            contentType = "image/png",
                            storageKey = "dev/mail-files/inline/logo.png",
                        ),
                    ),
                attachmentReferences =
                    listOf(
                        MailAttachmentReference(
                            fileId = 11L,
                            fileName = "guide.pdf",
                            contentType = "application/pdf",
                            storageKey = "dev/mail-files/attachment/guide.pdf",
                        ),
                    ),
                reservationTime = Instant.parse("2026-02-14T00:00:00Z"),
            )

        val mail = command.toMail("sender@example.com")

        assertThat(mail.inlineImageReferences).hasSize(1)
        assertThat(mail.inlineImageReferences[0].contentId).isEqualTo("cid_logo")
        assertThat(mail.attachmentReferences).hasSize(1)
        assertThat(mail.attachmentReferences[0].fileName).isEqualTo("guide.pdf")
        assertThat(mail.inlineImages).isEmpty()
        assertThat(mail.attachments).isEmpty()
    }
}
