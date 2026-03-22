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
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class S3MailFileStorage(
    private val properties: MailS3StorageProperties,
) : MailFileStorage {
    private val client: S3Client =
        S3Client.builder()
            .region(Region.of(properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()

    private val presigner: S3Presigner =
        S3Presigner.builder()
            .region(Region.of(properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()

    override fun upload(
        key: String,
        bytes: ByteArray,
        contentType: String,
    ): String {
        val resolvedKey = resolveKey(key)
        return try {
            val request =
                PutObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(resolvedKey)
                    .contentType(contentType)
                    .build()
            client.putObject(request, RequestBody.fromBytes(bytes))

            resolvedKey
        } catch (e: Exception) {
            throw IllegalStateException(
                "S3 파일 업로드에 실패했습니다. bucket=${properties.bucket}, key=$key, resolvedKey=$resolvedKey",
                e,
            )
        }
    }

    override fun download(key: String): ByteArray {
        val resolvedKey = resolveKey(key)
        return try {
            val request =
                GetObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(resolvedKey)
                    .build()

            client.getObjectAsBytes(request).asByteArray()
        } catch (e: Exception) {
            throw IllegalStateException(
                "S3 파일 다운로드에 실패했습니다. bucket=${properties.bucket}, key=$key, resolvedKey=$resolvedKey",
                e,
            )
        }
    }

    override fun createPresignedPutUrl(
        key: String,
        contentType: String,
        expireDuration: Duration,
    ): String {
        val resolvedKey = resolveKey(key)
        return try {
            val putObjectRequest =
                PutObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(resolvedKey)
                    .contentType(contentType)
                    .build()
            val presignRequest =
                PutObjectPresignRequest.builder()
                    .signatureDuration(expireDuration)
                    .putObjectRequest(putObjectRequest)
                    .build()

            presigner.presignPutObject(presignRequest).url().toString()
        } catch (e: Exception) {
            throw IllegalStateException(
                "S3 업로드용 presigned URL 생성에 실패했습니다. bucket=${properties.bucket}, key=$key, resolvedKey=$resolvedKey",
                e,
            )
        }
    }

    override fun createPresignedGetUrl(
        key: String,
        expireDuration: Duration,
    ): String {
        val resolvedKey = resolveKey(key)
        return try {
            val getObjectRequest =
                GetObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(resolvedKey)
                    .build()
            val presignRequest =
                GetObjectPresignRequest.builder()
                    .signatureDuration(expireDuration)
                    .getObjectRequest(getObjectRequest)
                    .build()

            presigner.presignGetObject(presignRequest).url().toString()
        } catch (e: Exception) {
            throw IllegalStateException(
                "S3 다운로드용 presigned URL 생성에 실패했습니다. bucket=${properties.bucket}, key=$key, resolvedKey=$resolvedKey",
                e,
            )
        }
    }

    override fun getPublicUrl(key: String): String {
        val resolvedKey = resolveKey(key)
        return "https://${properties.bucket}.s3.${properties.region}.amazonaws.com/$resolvedKey"
    }

    private fun resolveKey(key: String): String {
        val normalizedKey = key.trim().removePrefix("/")
        if (properties.keyPrefix.isBlank()) {
            return normalizedKey
        }

        val normalizedPrefix = properties.keyPrefix.trim().trim('/')
        if (normalizedKey == normalizedPrefix || normalizedKey.startsWith("$normalizedPrefix/")) {
            return normalizedKey
        }

        return "$normalizedPrefix/$normalizedKey"
    }

    @PreDestroy
    fun close() {
        client.close()
        presigner.close()
    }
}
