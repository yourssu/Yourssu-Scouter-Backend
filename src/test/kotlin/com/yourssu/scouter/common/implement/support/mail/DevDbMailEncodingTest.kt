package com.yourssu.scouter.common.implement.support.mail

import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilderResolver
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import java.util.Properties

@SpringBootTest
@ActiveProfiles("local", "local-dev-db")
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@Suppress("NonAsciiCharacters")
class DevDbMailEncodingTest(
    @Autowired private val mailRepository: MailRepository,
    @Autowired private val mimeMessageBuilderResolver: MimeMessageBuilderResolver,
) {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerDevDbProperties(registry: DynamicPropertyRegistry) {
            val envPath = Paths.get(".env.local")
            if (!Files.exists(envPath)) {
                return
            }
            val lines = Files.readAllLines(envPath)
            val map =
                lines
                    .asSequence()
                    .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
                    .mapNotNull { line ->
                        val idx = line.indexOf('=')
                        if (idx <= 0) {
                            null
                        } else {
                            val key = line.substring(0, idx).trim()
                            val value = line.substring(idx + 1).trim()
                            key to value
                        }
                    }.toMap()

            fun registerIfPresent(key: String) {
                val value = map[key] ?: return
                registry.add(key) { value }
            }

            registerIfPresent("DB_URL")
            registerIfPresent("DB_USERNAME")
            registerIfPresent("DB_PASSWORD")
            registerIfPresent("MAIL_STORAGE_S3_BUCKET")
            registerIfPresent("MAIL_STORAGE_S3_REGION")
            registerIfPresent("MAIL_STORAGE_S3_KEY_PREFIX")
        }
    }

    /**
     * dev DB에 존재하는 샘플 메일(197, 199)로 MimeMessage 제목 인코딩이 UTF-8로 정상인지 검증.
     * 해당 mailId가 DB에 없으면(CI 등) 테스트는 스킵된다.
     */
    @Test
    fun `dev DB 샘플 mailId로 MimeMessage Subject UTF-8 인코딩 회귀 검증`() {
        val ids = listOf(197L, 199L) // dev DB 전용 샘플 ID
        ids.forEach { id ->
            val mail = mailRepository.findById(id)
            assumeTrue(mail != null, "mailId=$id not found; this test requires dev DB with sample mails 197, 199")
            val m = mail!!

            val mailData = MailData.from(m)

            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.mime.charset", "UTF-8")
                put("mail.mime.allowutf8", "true")
            }
            val session = Session.getInstance(props, null)
            val builder = mimeMessageBuilderResolver.resolve(mailData)
            val message: MimeMessage = builder.build(mailData, session)

            // 회귀 검증: JavaMail 디코딩 결과가 DB 제목과 동일해야 함
            assertThat(message.subject).isEqualTo(m.mailSubject)

            val baos = ByteArrayOutputStream()
            message.writeTo(baos)
            val raw = baos.toString(Charsets.ISO_8859_1)
            val subjectHeader = raw.lineSequence().firstOrNull { it.startsWith("Subject:") }

            // 회귀 검증: Raw Subject가 RFC 2047 UTF-8 B 인코딩 형태여야 함 (mojibake 방지)
            assertThat(subjectHeader).isNotNull().contains("=?UTF-8?B?")
        }
    }
}
