package com.yourssu.scouter.mail.implement.message

data class MailAttachmentReference(
    val fileId: Long? = null,
    val fileName: String,
    val contentType: String,
    val storageKey: String,
)
