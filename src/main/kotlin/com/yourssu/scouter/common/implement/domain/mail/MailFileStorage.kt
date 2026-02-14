package com.yourssu.scouter.common.implement.domain.mail

interface MailFileStorage {
    fun upload(key: String, bytes: ByteArray, contentType: String): String

    fun download(key: String): ByteArray
}
