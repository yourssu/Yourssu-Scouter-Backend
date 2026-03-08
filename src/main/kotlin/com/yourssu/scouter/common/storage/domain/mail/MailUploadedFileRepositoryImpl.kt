package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFile
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileRepository
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import org.springframework.stereotype.Repository

@Repository
class MailUploadedFileRepositoryImpl(
    private val jpaMailUploadedFileRepository: JpaMailUploadedFileRepository,
) : MailUploadedFileRepository {
    override fun save(file: MailUploadedFile): MailUploadedFile {
        return jpaMailUploadedFileRepository.save(MailUploadedFileEntity.from(file)).toDomain()
    }

    override fun saveAll(files: List<MailUploadedFile>): List<MailUploadedFile> {
        return jpaMailUploadedFileRepository.saveAll(files.map { MailUploadedFileEntity.from(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): MailUploadedFile? {
        return jpaMailUploadedFileRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findAllByIdIn(ids: List<Long>): List<MailUploadedFile> {
        return jpaMailUploadedFileRepository.findAllByIdIn(ids).map { it.toDomain() }
    }

    override fun findAllActiveByUserId(userId: Long): List<MailUploadedFile> {
        return jpaMailUploadedFileRepository.findAllByUserIdAndStatus(userId, MailUploadedFileStatus.ACTIVE).map { it.toDomain() }
    }

    override fun findAllActiveByUserIdAndUsage(
        userId: Long,
        usage: MailFileUsage,
    ): List<MailUploadedFile> {
        return jpaMailUploadedFileRepository.findAllByUserIdAndStatusAndUsage(userId, MailUploadedFileStatus.ACTIVE, usage)
            .map { it.toDomain() }
    }

    override fun findActiveByStorageKey(storageKey: String): MailUploadedFile? {
        return jpaMailUploadedFileRepository.findByStorageKeyAndStatus(storageKey, MailUploadedFileStatus.ACTIVE)?.toDomain()
    }
}
