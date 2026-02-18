package com.yourssu.scouter.common.implement.domain.mail

data class MailInlineImageReference(
    val fileId: Long? = null,
    val contentId: String,
    val fileName: String,
    val contentType: String,
    val storageKey: String,
)

data class MailAttachmentReference(
    val fileId: Long? = null,
    val fileName: String,
    val contentType: String,
    val storageKey: String,
)
