package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailFilePresignCommand
import com.yourssu.scouter.common.implement.domain.mail.MailFilePresignResult
import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailInlineImageReference
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFile
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileRepository
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class MailFileService(
    private val mailFileStorage: MailFileStorage,
    private val mailUploadedFileRepository: MailUploadedFileRepository,
) {
    private val presignDuration: Duration = Duration.ofMinutes(10)

    fun createPresignedPutUrl(command: MailFilePresignCommand): MailFilePresignResult {
        val category =
            when (command.usage) {
                MailFileUsage.INLINE -> "inline"
                MailFileUsage.ATTACHMENT -> "attachment"
            }
        val key = "$category/${command.userId}/${UUID.randomUUID()}-${sanitize(command.fileName)}"
        val storageKey = mailFileStorage.resolveStorageKey(key)
        val putUrl =
            mailFileStorage.createPresignedPutUrl(
                key = key,
                contentType = command.contentType,
                expireDuration = presignDuration,
            )

        return MailFilePresignResult(
            s3Key = storageKey,
            putUrl = putUrl,
            expiresAt = Instant.now().plus(presignDuration),
            contentType = command.contentType,
        )
    }

    fun confirmUploads(
        userId: Long,
        files: List<MailUploadedFile>,
    ): List<MailUploadedFile> {
        validateOwnership(userId, files)
        return mailUploadedFileRepository.saveAll(files)
    }

    fun readActiveFiles(
        userId: Long,
        usage: MailFileUsage?,
    ): List<MailUploadedFile> {
        val files = mailUploadedFileRepository.findAllActiveByUserId(userId)
        if (usage == null) {
            return files
        }
        return files.filter { it.usage == usage }
    }

    @Transactional
    fun deleteFile(
        userId: Long,
        fileId: Long,
    ) {
        val file = requireFile(userId, fileId)
        require(!file.used) { "이미 사용된 파일은 삭제할 수 없습니다." }
        mailUploadedFileRepository.save(file.copy(status = MailUploadedFileStatus.DELETED))
    }

    @Transactional
    fun resolveInlineReferences(
        userId: Long,
        references: List<MailInlineImageReference>,
    ): List<MailInlineImageReference> {
        val resolved =
            references.map { reference ->
                resolveInlineReference(userId, reference)
            }
        markUsed(userId, references.mapNotNull { it.fileId })
        return resolved
    }

    @Transactional
    fun resolveAttachmentReferences(
        userId: Long,
        references: List<MailAttachmentReference>,
    ): List<MailAttachmentReference> {
        val resolved =
            references.map { reference ->
                resolveAttachmentReference(userId, reference)
            }
        markUsed(userId, references.mapNotNull { it.fileId })
        return resolved
    }

    private fun resolveInlineReference(
        userId: Long,
        reference: MailInlineImageReference,
    ): MailInlineImageReference {
        val fileId = reference.fileId ?: throw IllegalArgumentException("inlineImageReferences.fileId는 필수입니다.")
        val file = requireFile(userId, fileId)
        require(file.usage == MailFileUsage.INLINE) { "인라인 이미지가 아닌 파일입니다. fileId=$fileId" }
        return reference.copy(
            fileName = file.fileName,
            contentType = file.contentType,
            storageKey = file.storageKey,
        )
    }

    private fun resolveAttachmentReference(
        userId: Long,
        reference: MailAttachmentReference,
    ): MailAttachmentReference {
        val fileId = reference.fileId ?: throw IllegalArgumentException("attachmentReferences.fileId는 필수입니다.")
        val file = requireFile(userId, fileId)
        require(file.usage == MailFileUsage.ATTACHMENT) { "첨부파일이 아닌 파일입니다. fileId=$fileId" }
        return reference.copy(
            fileName = file.fileName,
            contentType = file.contentType,
            storageKey = file.storageKey,
        )
    }

    private fun markUsed(
        userId: Long,
        fileIds: List<Long>,
    ) {
        if (fileIds.isEmpty()) {
            return
        }
        val files = fileIds.distinct().map { requireFile(userId, it) }
        files.forEach { file ->
            if (!file.used) {
                mailUploadedFileRepository.save(file.copy(used = true))
            }
        }
    }

    private fun validateOwnership(
        userId: Long,
        files: List<MailUploadedFile>,
    ) {
        files.forEach { file ->
            require(file.userId == userId) { "파일 소유자가 일치하지 않습니다." }
            require(file.storageKey.isNotBlank()) { "storageKey는 비어 있을 수 없습니다." }
            require(file.contentType.isNotBlank()) { "contentType은 비어 있을 수 없습니다." }
            require(file.fileName.isNotBlank()) { "fileName은 비어 있을 수 없습니다." }
        }
    }

    private fun requireFile(
        userId: Long,
        fileId: Long,
    ): MailUploadedFile {
        val file =
            mailUploadedFileRepository.findById(fileId)
                ?: throw IllegalArgumentException("파일을 찾을 수 없습니다. fileId=$fileId")
        require(file.userId == userId) { "파일 접근 권한이 없습니다. fileId=$fileId" }
        require(file.status == MailUploadedFileStatus.ACTIVE) { "삭제된 파일입니다. fileId=$fileId" }
        return file
    }

    private fun sanitize(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
