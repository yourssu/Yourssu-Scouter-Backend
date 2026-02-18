package com.yourssu.scouter.common.application.domain.mail.template

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class CreateMailTemplateRequestFileReferenceTest {
    @Test
    fun `toDomain은 인라인 이미지와 첨부 참조를 매핑한다`() {
        val request =
            CreateMailTemplateRequest(
                title = "OT 안내",
                bodyHtml = "<img src=\"cid:cid_banner\" />",
                variables = emptyList(),
                inlineImageReferences =
                    listOf(
                        CreateMailTemplateRequest.InlineImageReferenceRequest(
                            fileId = 101L,
                            contentId = "cid_banner",
                        ),
                    ),
                attachmentReferences =
                    listOf(
                        CreateMailTemplateRequest.AttachmentReferenceRequest(
                            fileId = 102L,
                        ),
                    ),
            )

        val domain = request.toDomain(createdBy = 1L)

        assertThat(domain.inlineImageReferences).hasSize(1)
        assertThat(domain.inlineImageReferences[0].fileId).isEqualTo(101L)
        assertThat(domain.inlineImageReferences[0].contentId).isEqualTo("cid_banner")
        assertThat(domain.attachmentReferences).hasSize(1)
        assertThat(domain.attachmentReferences[0].fileId).isEqualTo(102L)
    }
}
