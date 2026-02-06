package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailWriter
import com.yourssu.scouter.common.implement.domain.user.TokenInfo
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import com.yourssu.scouter.common.implement.domain.user.UserRepository
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MailReservationIntegrationTest(
    @Autowired private val mailWriter: MailWriter,
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val tokenProcessor: TokenProcessor,
    @Autowired private val userRepository: UserRepository,
) {

    private fun createUserAndToken(): String {
        val user = userRepository.save(
            User(
                userInfo = UserInfo(
                    name = "테스트유저",
                    email = "integration-${System.nanoTime()}@example.com",
                    profileImageUrl = "https://example.com/profile.jpg",
                    oauthId = "oauth-integration-${System.nanoTime()}",
                    oauth2Type = OAuth2Type.GOOGLE,
                ),
                tokenInfo = TokenInfo(
                    tokenPrefix = "Bearer",
                    accessToken = "test-access-token",
                    refreshToken = "test-refresh-token",
                    accessTokenExpirationDateTime = LocalDateTime.now().plusHours(1),
                ),
            )
        )
        return tokenProcessor.encode(
            issueTime = LocalDateTime.now(),
            tokenType = TokenType.ACCESS,
            privateClaims = mapOf("userId" to user.id!!)
        )
    }

    private val requestJson = """
        {
            "receiverEmailAddresses": ["user@example.com"],
            "ccEmailAddresses": [],
            "bccEmailAddresses": [],
            "mailSubject": "test",
            "mailBody": "<p>Web FE,김병두</p><p>개별변수:ㅇㅇ</p>",
            "bodyFormat": "HTML",
            "reservationTime": "2026-02-06T02:16:57.946Z"
        }
    """.trimIndent()

    @Test
    fun `메일 예약 저장이 정상 동작한다`() {
        val mail = Mail(
            senderEmailAddress = "sender@example.com",
            receiverEmailAddresses = listOf("user@example.com"),
            ccEmailAddresses = emptyList(),
            bccEmailAddresses = emptyList(),
            mailSubject = "test",
            mailBody = "<p>Web FE,김병두</p><p>개별변수:ㅇㅇ</p>",
            bodyFormat = MailBodyFormat.HTML,
            inlineImages = emptyMap(),
            attachments = emptyMap(),
        )

        assertDoesNotThrow {
            mailWriter.reserve(mail, LocalDateTime.of(2026, 2, 6, 2, 16, 57))
        }
    }

    @Test
    fun `JSON으로 메일 예약 API 호출 시 200을 반환한다`() {
        val token = createUserAndToken()

        mockMvc.perform(
            post("/api/mails/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `multipart로 메일 예약 API 호출 시 200을 반환한다`() {
        val token = createUserAndToken()

        val requestPart = MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.toByteArray()
        )

        mockMvc.perform(
            multipart("/api/mails/reservation")
                .file(requestPart)
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
    }
}
