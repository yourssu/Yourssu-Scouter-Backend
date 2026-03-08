package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.implement.support.exception.MailFileAccessDeniedException
import com.yourssu.scouter.common.implement.support.exception.MailFileAlreadyUsedException
import com.yourssu.scouter.common.implement.support.exception.MailFileInvalidUsageException
import com.yourssu.scouter.common.implement.support.exception.MailFileNotFoundException
import org.springframework.stereotype.Component

@Component
class MailFileValidator(
    private val mailUploadedFileRepository: MailUploadedFileRepository,
) {
    fun validateOwnership(
        userId: Long,
        files: List<MailUploadedFile>,
    ) {
        files.forEach { file ->
            if (file.userId != userId) {
                throw MailFileAccessDeniedException("파일 소유자가 일치하지 않습니다.")
            }
            if (file.storageKey.isBlank()) {
                throw MailFileInvalidUsageException("storageKey는 비어 있을 수 없습니다.")
            }
            if (file.contentType.isBlank()) {
                throw MailFileInvalidUsageException("contentType은 비어 있을 수 없습니다.")
            }
            if (file.fileName.isBlank()) {
                throw MailFileInvalidUsageException("fileName은 비어 있을 수 없습니다.")
            }
        }
    }

    fun requireFile(fileId: Long): MailUploadedFile {
        val file =
            mailUploadedFileRepository.findById(fileId)
                ?: throw MailFileNotFoundException("파일을 찾을 수 없습니다. fileId=$fileId")
        if (file.status != MailUploadedFileStatus.ACTIVE) {
            throw MailFileNotFoundException("삭제된 파일입니다. fileId=$fileId")
        }
        return file
    }

    fun requireOwnedFile(
        userId: Long,
        fileId: Long,
    ): MailUploadedFile {
        val file = requireFile(fileId)
        if (file.userId != userId) {
            throw MailFileAccessDeniedException("파일 접근 권한이 없습니다. fileId=$fileId")
        }
        return file
    }

    fun validateNotUsed(file: MailUploadedFile) {
        if (file.used) {
            throw MailFileAlreadyUsedException("이미 사용된 파일은 삭제할 수 없습니다.")
        }
    }

    fun validateUsage(
        file: MailUploadedFile,
        expectedUsage: MailFileUsage,
    ) {
        if (file.usage != expectedUsage) {
            val message =
                when (expectedUsage) {
                    MailFileUsage.INLINE -> "인라인 이미지가 아닌 파일입니다. fileId=${file.id}"
                    MailFileUsage.ATTACHMENT -> "첨부파일이 아닌 파일입니다. fileId=${file.id}"
                }
            throw MailFileInvalidUsageException(message)
        }
    }
}
