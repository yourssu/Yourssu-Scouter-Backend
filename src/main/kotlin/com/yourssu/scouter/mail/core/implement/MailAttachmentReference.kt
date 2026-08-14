package com.yourssu.scouter.mail.core.implement

data class MailAttachmentReference(
    val fileId: Long? = null,
    val fileName: String,
    val contentType: String,
    val storageKey: String,
)
