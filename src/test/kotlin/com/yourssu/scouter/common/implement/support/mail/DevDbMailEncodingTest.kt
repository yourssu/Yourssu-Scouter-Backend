package com.yourssu.scouter.common.implement.support.mail

import com.yourssu.scouter.common.business.domain.mail.MailData
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import com.yourssu.scouter.common.implement.domain.mail.MimeMessageBuilderResolver
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Test
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

    @Test
    fun `dev DB mailId 197과 199의 MimeMessage Subject 인코딩 비교`() {
        val ids = listOf(197L, 199L)
        ids.forEach { id ->
            val mail = mailRepository.findById(id)
                ?: error("mailId=$id 를 찾을 수 없습니다.")

            println("=== mailId=$id DB 문자열 subject=[${mail.mailSubject}] ===")

            val mailData = MailData.from(mail)

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

            // JavaMail이 디코딩해서 보여주는 Subject
            println("MimeMessage.getSubject() for mailId=$id -> [${message.subject}]")

            // Raw MIME 헤더에서 Subject 라인만 출력
            val baos = ByteArrayOutputStream()
            message.writeTo(baos)
            val raw = baos.toString(Charsets.ISO_8859_1) // MIME 원문은 ISO-8859-1로 보는 게 관례
            val subjectHeader = raw.lineSequence().firstOrNull { it.startsWith("Subject:") }
            println("Raw Subject header for mailId=$id -> $subjectHeader")

            // Gmail에 보내는 것과 동일한 Base64 URL 인코딩 문자열도 참고용
            val encoded = Base64.getUrlEncoder().encodeToString(baos.toByteArray())
            println("Encoded length for mailId=$id = ${encoded.length}")
            println()
        }
    }
}
