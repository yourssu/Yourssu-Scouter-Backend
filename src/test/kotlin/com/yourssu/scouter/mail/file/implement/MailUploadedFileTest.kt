package com.yourssu.scouter.mail.file.implement

import com.yourssu.scouter.mail.file.implement.MailFileAccessDeniedException
import com.yourssu.scouter.mail.file.implement.MailFileInvalidUsageException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class MailUploadedFileTest {
    private fun uploadedFile(
        userId: Long = 7L,
        usage: MailFileUsage = MailFileUsage.ATTACHMENT,
        used: Boolean = false,
    ): MailUploadedFile {
        return MailUploadedFile(
            id = 5L,
            userId = userId,
            usage = usage,
            fileName = "guide.pdf",
            contentType = "application/pdf",
            storageKey = "mail-files/attachment/7/guide.pdf",
            used = used,
        )
    }

    @Test
    fun `validateOwnership은 소유자가 일치하면 예외가 발생하지 않는다`() {
        val file = uploadedFile(userId = 7L)

        assertThatCode { file.validateOwnership(7L) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateOwnership은 소유자가 다르면 예외가 발생한다`() {
        val file = uploadedFile(userId = 7L)

        assertThatThrownBy { file.validateOwnership(8L) }
            .isInstanceOf(MailFileAccessDeniedException::class.java)
            .hasMessageContaining("파일 접근 권한")
            .hasMessageContaining("fileId=5")
    }

    @Test
    fun `validateUsage는 사용 용도가 일치하면 예외가 발생하지 않는다`() {
        val file = uploadedFile(usage = MailFileUsage.INLINE)

        assertThatCode { file.validateUsage(MailFileUsage.INLINE) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateUsage는 사용 용도가 다르면 예외가 발생한다`() {
        val file = uploadedFile(usage = MailFileUsage.ATTACHMENT)

        assertThatThrownBy { file.validateUsage(MailFileUsage.INLINE) }
            .isInstanceOf(MailFileInvalidUsageException::class.java)
            .hasMessageContaining("인라인 이미지가 아닌 파일")
            .hasMessageContaining("fileId=5")
    }

    @Test
    fun `markAsUsed는 used가 true인 새 객체를 반환한다`() {
        val file = uploadedFile(used = false)

        val result = file.markAsUsed()

        assertThat(result).isNotSameAs(file)
        assertThat(result.used).isTrue()
        assertThat(result.id).isEqualTo(file.id)
        assertThat(result.userId).isEqualTo(file.userId)
        assertThat(result.usage).isEqualTo(file.usage)
        assertThat(result.fileName).isEqualTo(file.fileName)
        assertThat(result.contentType).isEqualTo(file.contentType)
        assertThat(result.storageKey).isEqualTo(file.storageKey)
    }
}
