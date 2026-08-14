package com.yourssu.scouter.mail.file.implement

import java.time.Duration

interface MailFileStorage {
    fun upload(
        key: String,
        bytes: ByteArray,
        contentType: String,
    ): String

    fun download(key: String): ByteArray

    fun createPresignedPutUrl(
        key: String,
        contentType: String,
        expireDuration: Duration,
    ): String

    fun createPresignedGetUrl(
        key: String,
        expireDuration: Duration,
    ): String

    fun getPublicUrl(key: String): String
}
