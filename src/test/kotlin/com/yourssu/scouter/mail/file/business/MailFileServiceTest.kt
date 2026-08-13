package com.yourssu.scouter.mail.file.business

import com.yourssu.scouter.mail.core.implement.MailAttachmentReference
import com.yourssu.scouter.mail.file.implement.MailFileReferenceResolver
import com.yourssu.scouter.mail.file.implement.MailFileStorage
import com.yourssu.scouter.mail.file.implement.MailFileUsage
import com.yourssu.scouter.mail.file.implement.MailFileValidator
import com.yourssu.scouter.mail.file.implement.MailUploadedFile
import com.yourssu.scouter.mail.file.implement.MailUploadedFileRepository
import com.yourssu.scouter.mail.file.implement.MailUploadedFileStatus
import com.yourssu.scouter.mail.file.implement.MailFileAlreadyUsedException
import com.yourssu.scouter.mail.file.implement.MailFileInvalidUsageException
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
        assertThat(result.cid).startsWith("attachment/")
        assertThat(result.cid).doesNotContain("/7/")
        assertThat(result.contentType).isEqualTo("application/pdf")
    }

    @Test
    fun `deleteFile은 used 파일 삭제를 막는다`() {
        val service = createService()
        val usedFile =
            MailUploadedFile(
                id = 5L,
                userId = 7L,
                usage = MailFileUsage.ATTACHMENT,
                fileName = "guide.pdf",
                contentType = "application/pdf",
                storageKey = "mail-files/attachment/7/guide.pdf",
                status = MailUploadedFileStatus.ACTIVE,
                used = true,
            )
        whenever(validator.requireOwnedFile(7L, 5L)).thenReturn(usedFile)
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
        val references =
            listOf(
                MailAttachmentReference(
                    fileName = "guide.pdf",
                    contentType = "application/pdf",
                    storageKey = "mail-files/attachment/7/guide.pdf",
                ),
            )
        whenever(referenceResolver.resolveAttachmentReferences(references)).thenThrow(
            MailFileInvalidUsageException("attachmentReferences.fileId는 필수입니다."),
        )

        assertThatThrownBy { service.resolveAttachmentReferences(references = references) }
            .isInstanceOf(MailFileInvalidUsageException::class.java)
            .hasMessageContaining("fileId는 필수")
    }
}
