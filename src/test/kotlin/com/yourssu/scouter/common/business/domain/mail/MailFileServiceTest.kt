package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailFileReferenceResolver
import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailFileValidator
import com.yourssu.scouter.common.implement.domain.mail.MailInlineImageReference
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFile
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileRepository
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import com.yourssu.scouter.common.implement.support.exception.MailFileAlreadyUsedException
import com.yourssu.scouter.common.implement.support.exception.MailFileInvalidUsageException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class MailFileServiceTest {
    private val storage = mock<MailFileStorage>()
    private val repository = mock<MailUploadedFileRepository>()
    private val validator = mock<MailFileValidator>()
    private val referenceResolver = mock<MailFileReferenceResolver>()

    private fun createService() = MailFileService(storage, repository, validator, referenceResolver)

    @Test
    fun `createPresignedPutUrl은 사용 용도에 맞는 key와 put url을 생성한다`() {
        whenever(storage.createPresignedPutUrl(any(), any(), any())).thenReturn("https://example.com/put")
        whenever(storage.resolveStorageKey(any())).thenAnswer { "dev/mail-files/${it.arguments[0] as String}" }
        val service = createService()

        val result =
            service.createPresignedPutUrl(
                MailFilePresignCommand(
                    userId = 7L,
                    fileName = "guide.pdf",
                    contentType = "application/pdf",
                    usage = MailFileUsage.ATTACHMENT,
                ),
            )

        assertThat(result.putUrl).isEqualTo("https://example.com/put")
        assertThat(result.s3Key).contains("dev/mail-files/attachment/7/")
        assertThat(result.contentType).isEqualTo("application/pdf")
    }

    @Test
    fun `resolveInlineReferences는 referenceResolver에 위임한다`() {
        val service = createService()
        val inputReferences = listOf(
            MailInlineImageReference(
                fileId = 1L,
                contentId = "cid_logo",
                fileName = "",
                contentType = "",
                storageKey = "",
            ),
        )
        val resolvedReferences = listOf(
            MailInlineImageReference(
                fileId = 1L,
                contentId = "cid_logo",
                fileName = "logo.png",
                contentType = "image/png",
                storageKey = "mail-files/inline/7/logo.png",
            ),
        )
        whenever(referenceResolver.resolveInlineReferences(7L, inputReferences)).thenReturn(resolvedReferences)

        val resolved = service.resolveInlineReferences(userId = 7L, references = inputReferences)

        assertThat(resolved[0].storageKey).isEqualTo("mail-files/inline/7/logo.png")
    }

    @Test
    fun `deleteFile은 used 파일 삭제를 막는다`() {
        val service = createService()
        val usedFile = MailUploadedFile(
            id = 5L,
            userId = 7L,
            usage = MailFileUsage.ATTACHMENT,
            fileName = "guide.pdf",
            contentType = "application/pdf",
            storageKey = "mail-files/attachment/7/guide.pdf",
            status = MailUploadedFileStatus.ACTIVE,
            used = true,
        )
        whenever(validator.requireFile(7L, 5L)).thenReturn(usedFile)
        whenever(validator.validateNotUsed(usedFile)).thenThrow(
            MailFileAlreadyUsedException("이미 사용된 파일은 삭제할 수 없습니다."),
        )

        assertThatThrownBy { service.deleteFile(7L, 5L) }
            .isInstanceOf(MailFileAlreadyUsedException::class.java)
            .hasMessageContaining("이미 사용된 파일")
    }

    @Test
    fun `resolveAttachmentReferences는 fileId가 없으면 예외가 발생한다`() {
        val service = createService()
        val references = listOf(
            MailAttachmentReference(
                fileName = "guide.pdf",
                contentType = "application/pdf",
                storageKey = "mail-files/attachment/7/guide.pdf",
            ),
        )
        whenever(referenceResolver.resolveAttachmentReferences(7L, references)).thenThrow(
            MailFileInvalidUsageException("attachmentReferences.fileId는 필수입니다."),
        )

        assertThatThrownBy { service.resolveAttachmentReferences(userId = 7L, references = references) }
            .isInstanceOf(MailFileInvalidUsageException::class.java)
            .hasMessageContaining("fileId는 필수")
    }
}
