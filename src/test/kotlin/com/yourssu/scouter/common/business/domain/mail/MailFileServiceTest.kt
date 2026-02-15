package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailFilePresignCommand
import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailInlineImageReference
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFile
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileRepository
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class MailFileServiceTest {
    private val storage = mock<MailFileStorage>()
    private val repository = mock<MailUploadedFileRepository>()

    @Test
    fun `createPresignedPutUrl은 사용 용도에 맞는 key와 put url을 생성한다`() {
        whenever(storage.createPresignedPutUrl(any(), any(), any())).thenReturn("https://example.com/put")
        whenever(storage.resolveStorageKey(any())).thenAnswer { "dev/mail-files/${it.arguments[0] as String}" }
        val service = MailFileService(storage, repository)

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
    fun `resolveInlineReferences는 fileId로 파일을 조회해 storageKey를 채운다`() {
        val service = MailFileService(storage, repository)
        whenever(repository.findById(1L)).thenReturn(
            MailUploadedFile(
                id = 1L,
                userId = 7L,
                usage = MailFileUsage.INLINE,
                fileName = "logo.png",
                contentType = "image/png",
                storageKey = "mail-files/inline/7/logo.png",
                status = MailUploadedFileStatus.ACTIVE,
            ),
        )
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as MailUploadedFile }

        val resolved =
            service.resolveInlineReferences(
                userId = 7L,
                references =
                    listOf(
                        MailInlineImageReference(
                            fileId = 1L,
                            contentId = "cid_logo",
                            fileName = "",
                            contentType = "",
                            storageKey = "",
                        ),
                    ),
            )

        assertThat(resolved[0].storageKey).isEqualTo("mail-files/inline/7/logo.png")
        verify(repository).save(argThat { id == 1L && used })
    }

    @Test
    fun `deleteFile은 used 파일 삭제를 막는다`() {
        val service = MailFileService(storage, repository)
        whenever(repository.findById(5L)).thenReturn(
            MailUploadedFile(
                id = 5L,
                userId = 7L,
                usage = MailFileUsage.ATTACHMENT,
                fileName = "guide.pdf",
                contentType = "application/pdf",
                storageKey = "mail-files/attachment/7/guide.pdf",
                status = MailUploadedFileStatus.ACTIVE,
                used = true,
            ),
        )

        assertThatThrownBy { service.deleteFile(7L, 5L) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이미 사용된 파일")
    }

    @Test
    fun `resolveAttachmentReferences는 fileId가 없으면 예외가 발생한다`() {
        val service = MailFileService(storage, repository)

        assertThatThrownBy {
            service.resolveAttachmentReferences(
                userId = 7L,
                references =
                    listOf(
                        MailAttachmentReference(
                            fileName = "guide.pdf",
                            contentType = "application/pdf",
                            storageKey = "mail-files/attachment/7/guide.pdf",
                        ),
                    ),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("fileId는 필수")
    }
}
