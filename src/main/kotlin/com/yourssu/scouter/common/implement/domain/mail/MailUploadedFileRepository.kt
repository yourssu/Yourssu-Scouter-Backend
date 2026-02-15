package com.yourssu.scouter.common.implement.domain.mail

interface MailUploadedFileRepository {
    fun save(file: MailUploadedFile): MailUploadedFile

    fun saveAll(files: List<MailUploadedFile>): List<MailUploadedFile>

    fun findById(id: Long): MailUploadedFile?

    fun findAllByIdIn(ids: List<Long>): List<MailUploadedFile>

    fun findAllActiveByUserId(userId: Long): List<MailUploadedFile>

    fun findAllActiveByUserIdAndUsage(userId: Long, usage: MailFileUsage): List<MailUploadedFile>
}
