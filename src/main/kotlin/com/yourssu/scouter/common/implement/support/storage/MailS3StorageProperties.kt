package com.yourssu.scouter.common.implement.support.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mail.storage.s3")
data class MailS3StorageProperties(
    val bucket: String = "",
    val region: String = "",
    val keyPrefix: String = "mail-files"
)
