package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailFilePresignResult
import com.yourssu.scouter.common.implement.domain.mail.MailFileReferenceResolver
import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailFileValidator
import com.yourssu.scouter.common.implement.domain.mail.MailStorageKeyGenerator
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFile
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileRepository
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import com.yourssu.scouter.common.implement.support.exception.MailFileNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class MailFileService(
    private val mailFileStorage: MailFileStorage,
    private val mailUploadedFileRepository: MailUploadedFileRepository,
    private val mailFileValidator: MailFileValidator,
    private val mailFileReferenceResolver: MailFileReferenceResolver,
) {
    private val presignDuration: Duration = Duration.ofMinutes(10)

    fun createPresignedPutUrl(command: MailFilePresignCommand): MailFilePresignResult {
        val key = MailStorageKeyGenerator.generate(command.usage, command.userId, command.fileName)
        val putUrl =
            mailFileStorage.createPresignedPutUrl(
                key = key,
                contentType = command.contentType,
                expireDuration = presignDuration,
            )

        return MailFilePresignResult(
            cid = key,
            putUrl = putUrl,
            expiresAt = Instant.now().plus(presignDuration),
            contentType = command.contentType,
        )
    }

    fun confirmUploads(
        userId: Long,
        files: List<MailUploadedFile>,
    ): List<MailUploadedFile> {
        mailFileValidator.validateOwnership(userId, files)
        return mailUploadedFileRepository.saveAll(files)
    }

    fun readActiveFiles(
        userId: Long,
        usage: MailFileUsage?,
    ): List<MailUploadedFile> {
        val files = mailUploadedFileRepository.findAllActiveByUserId(userId)
        if (usage == null) return files
        return files.filter { it.usage == usage }
    }

    @Transactional
    fun deleteFile(
        userId: Long,
        fileId: Long,
    ) {
        val file = mailFileValidator.requireFile(userId, fileId)
        mailFileValidator.validateNotUsed(file)
        mailUploadedFileRepository.save(file.copy(status = MailUploadedFileStatus.DELETED))
    }

    fun createPresignedGetUrl(storageKey: String): String {
        mailUploadedFileRepository.findActiveByStorageKey(storageKey)
            ?: throw MailFileNotFoundException("파일을 찾을 수 없습니다. storageKey=$storageKey")
        return mailFileStorage.createPresignedGetUrl(
            key = storageKey,
            expireDuration = presignDuration,
        )
    }

    @Transactional
    fun resolveAttachmentReferences(
        userId: Long,
        references: List<MailAttachmentReference>,
    ): List<MailAttachmentReference> {
        return mailFileReferenceResolver.resolveAttachmentReferences(userId, references)
    }
}
