package com.yourssu.scouter.common.implement.support.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class S3MailFileStorageKeyResolutionTest {
    private fun createStorage(prefix: String): S3MailFileStorage {
        return S3MailFileStorage(
            MailS3StorageProperties(
                bucket = "test-bucket",
                region = "ap-northeast-2",
                keyPrefix = prefix,
            ),
        )
    }

    @Test
    fun `getPublicUrl은 상대 key에 prefix를 1회만 붙인다`() {
        val storage = createStorage("dev/mail-files")

        val url = storage.getPublicUrl("attachment/2/sample.png")

        assertThat(url)
            .isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/dev/mail-files/attachment/2/sample.png")
        storage.close()
    }

    @Test
    fun `getPublicUrl은 이미 prefix가 포함된 key를 중복 prefix하지 않는다`() {
        val storage = createStorage("dev/mail-files")

        val url = storage.getPublicUrl("dev/mail-files/attachment/2/sample.png")

        assertThat(url)
            .isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/dev/mail-files/attachment/2/sample.png")
        storage.close()
    }

    @Test
    fun `getPublicUrl은 선행 슬래시가 있어도 정상 key로 정규화한다`() {
        val storage = createStorage("dev/mail-files")

        val url = storage.getPublicUrl("/dev/mail-files/attachment/2/sample.png")

        assertThat(url)
            .isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/dev/mail-files/attachment/2/sample.png")
        storage.close()
    }
}
