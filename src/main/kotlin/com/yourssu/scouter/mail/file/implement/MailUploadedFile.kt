package com.yourssu.scouter.mail.file.implement

import com.yourssu.scouter.mail.file.implement.MailFileAccessDeniedException
import com.yourssu.scouter.mail.file.implement.MailFileAlreadyUsedException
import com.yourssu.scouter.mail.file.implement.MailFileInvalidUsageException
import java.time.Instant

enum class MailUploadedFileStatus {
    ACTIVE,
    DELETED,
}

data class MailUploadedFile(
    val id: Long? = null,
    val userId: Long,
    val usage: MailFileUsage,
    val fileName: String,
    val contentType: String,
    val storageKey: String,
    val status: MailUploadedFileStatus = MailUploadedFileStatus.ACTIVE,
    val used: Boolean = false,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    fun validateOwnership(userId: Long) {
        if (this.userId != userId) {
            throw MailFileAccessDeniedException("파일 접근 권한이 없습니다. fileId=${this.id}")
        }
    }

    fun validateUsage(expectedUsage: MailFileUsage) {
        if (this.usage != expectedUsage) {
            val message =
                when (expectedUsage) {
                    MailFileUsage.INLINE -> "인라인 이미지가 아닌 파일입니다. fileId=${this.id}"
                    MailFileUsage.ATTACHMENT -> "첨부파일이 아닌 파일입니다. fileId=${this.id}"
                }
            throw MailFileInvalidUsageException(message)
        }
    }

    fun validateNotUsed() {
        if (used) {
            throw MailFileAlreadyUsedException("이미 사용된 파일은 삭제할 수 없습니다.")
        }
    }

    fun markAsUsed(): MailUploadedFile {
        return copy(used = true)
    }
}
