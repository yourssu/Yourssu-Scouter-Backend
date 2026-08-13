package com.yourssu.scouter.mail.file.business

import com.yourssu.scouter.mail.core.implement.MailAttachmentReference
import com.yourssu.scouter.mail.file.implement.MailFileDownloadResult
import com.yourssu.scouter.mail.file.implement.MailFilePresignResult
import com.yourssu.scouter.mail.file.implement.MailFileReferenceResolver
import com.yourssu.scouter.mail.file.implement.MailFileStorage
import com.yourssu.scouter.mail.file.implement.MailFileUsage
import com.yourssu.scouter.mail.file.implement.MailFileValidator
import com.yourssu.scouter.mail.support.utils.MailStorageKeyGenerator
import com.yourssu.scouter.mail.file.implement.MailUploadedFile
import com.yourssu.scouter.mail.file.implement.MailUploadedFileRepository
import com.yourssu.scouter.mail.file.implement.MailUploadedFileStatus
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
        val key = MailStorageKeyGenerator.generate(command.usage, command.fileName)
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
        val file = mailFileValidator.requireOwnedFile(userId, fileId)
        file.validateNotUsed()
        mailUploadedFileRepository.save(file.copy(status = MailUploadedFileStatus.DELETED))
    }

    fun createPresignedGetUrl(storageKey: String): MailFileDownloadResult {
        val url =
            mailFileStorage.createPresignedGetUrl(
                key = storageKey,
                expireDuration = presignDuration,
            )
        return MailFileDownloadResult(
            getUrl = url,
            expiresAt = Instant.now().plus(presignDuration),
        )
    }

    fun getPublicUrl(
        cid: String,
        fileUsage: MailFileUsage,
    ) = mailFileStorage.getPublicUrl(fileUsage.name.lowercase() + '/' + cid)

    fun downloadAttachments(references: List<MailAttachmentReference>): Map<String, jakarta.mail.util.ByteArrayDataSource> {
        return references.associate { ref ->
            ref.fileName to
                jakarta.mail.util.ByteArrayDataSource(
                    mailFileStorage.download(ref.storageKey),
                    ref.contentType,
                )
        }
    }

    @Transactional
    fun resolveAttachmentReferences(references: List<MailAttachmentReference>): List<MailAttachmentReference> {
        return mailFileReferenceResolver.resolveAttachmentReferences(references)
    }
}
