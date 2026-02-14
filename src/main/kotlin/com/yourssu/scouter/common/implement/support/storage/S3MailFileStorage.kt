package com.yourssu.scouter.common.implement.support.storage

import com.yourssu.scouter.common.implement.domain.mail.MailFileStorage
import jakarta.annotation.PreDestroy
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
    private val client: S3Client =
        S3Client.builder()
            .region(Region.of(properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()

    override fun upload(
        key: String,
        bytes: ByteArray,
        contentType: String,
    ): String {
        return try {
            val resolvedKey = resolveKey(key)
            val request =
                PutObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(resolvedKey)
                    .contentType(contentType)
                    .build()
            client.putObject(request, RequestBody.fromBytes(bytes))

            resolvedKey
        } catch (e: Exception) {
            throw IllegalStateException("S3 파일 업로드에 실패했습니다: $key", e)
        }
    }

    override fun download(key: String): ByteArray {
        return try {
            val request =
                GetObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(key)
                    .build()

            client.getObjectAsBytes(request).asByteArray()
        } catch (e: Exception) {
            throw IllegalStateException("S3 파일 다운로드에 실패했습니다: $key", e)
        }
    }

    private fun resolveKey(key: String): String {
        if (properties.keyPrefix.isBlank()) {
            return key
        }

        return "${properties.keyPrefix.trimEnd('/')}/$key"
    }

    @PreDestroy
    fun close() {
        client.close()
    }
}
