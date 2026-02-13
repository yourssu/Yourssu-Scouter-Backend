package com.yourssu.scouter.common.implement.support.storage

import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Component
class S3MailFileStorage(
    private val properties: MailS3StorageProperties,
) : MailFileStorage {

    private val client: S3Client = S3Client.builder()
        .region(Region.of(properties.region))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build()

    override fun upload(key: String, bytes: ByteArray, contentType: String): String {
        val resolvedKey = resolveKey(key)
        val request = PutObjectRequest.builder()
            .bucket(properties.bucket)
            .key(resolvedKey)
            .contentType(contentType)
            .build()
        client.putObject(request, RequestBody.fromBytes(bytes))

        return resolvedKey
    }

    override fun download(key: String): ByteArray {
        val request = GetObjectRequest.builder()
            .bucket(properties.bucket)
            .key(key)
            .build()

        return client.getObjectAsBytes(request).asByteArray()
    }

    private fun resolveKey(key: String): String {
        if (properties.keyPrefix.isBlank()) {
            return key
        }

        return "${properties.keyPrefix.trimEnd('/')}/$key"
    }
}
