package com.yourssu.scouter.common.implement.domain.mail

interface MailUploadedFileRepository {
    fun save(file: MailUploadedFile): MailUploadedFile

    fun saveAll(files: List<MailUploadedFile>): List<MailUploadedFile>

    fun findById(id: Long): MailUploadedFile?

    fun findAllActiveByUserId(userId: Long): List<MailUploadedFile>
}
