package com.yourssu.scouter.common.application.domain.mail.template

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class CreateMailTemplateRequestFileReferenceTest {
    @Test
    fun `toDomain은 첨부 참조를 매핑한다`() {
        val request =
            CreateMailTemplateRequest(
                title = "OT 안내",
                bodyHtml = "<p>안내 메일입니다</p>",
                variables = emptyList(),
                attachmentReferences =
                    listOf(
                        CreateMailTemplateRequest.AttachmentReferenceRequest(
                            fileId = 102L,
                        ),
                    ),
            )

        val domain = request.toDomain(createdBy = 1L)

        assertThat(domain.attachmentReferences).hasSize(1)
        assertThat(domain.attachmentReferences[0].fileId).isEqualTo(102L)
    }
}
