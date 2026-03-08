package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.implement.support.exception.MailFileInvalidUsageException
import org.springframework.stereotype.Component

@Component
class MailFileReferenceResolver(
    private val mailFileValidator: MailFileValidator,
    private val mailUploadedFileRepository: MailUploadedFileRepository,
) {
    fun resolveAttachmentReferences(
        references: List<MailAttachmentReference>,
    ): List<MailAttachmentReference> {
        val resolved = references.map { resolveAttachmentReference(it) }
        markUsed(references.mapNotNull { it.fileId })
        return resolved
    }

    private fun resolveAttachmentReference(
        reference: MailAttachmentReference,
    ): MailAttachmentReference {
        val fileId =
            reference.fileId
                ?: throw MailFileInvalidUsageException("attachmentReferences.fileId는 필수입니다.")
        val file = mailFileValidator.requireFile(fileId)
        mailFileValidator.validateUsage(file, MailFileUsage.ATTACHMENT)
        return reference.copy(
            fileName = file.fileName,
            contentType = file.contentType,
            storageKey = file.storageKey,
        )
    }

    private fun markUsed(
        fileIds: List<Long>,
    ) {
        if (fileIds.isEmpty()) return
        fileIds.distinct()
            .map { mailFileValidator.requireFile(it) }
            .filter { !it.used }
            .forEach { mailUploadedFileRepository.save(it.copy(used = true)) }
    }
}
