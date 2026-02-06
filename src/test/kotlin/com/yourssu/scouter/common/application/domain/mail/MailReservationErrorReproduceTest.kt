package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.common.implement.domain.user.TokenInfo
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import com.yourssu.scouter.common.implement.domain.user.UserRepository
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "token.jwt.access-key=test-access-key-test-access-key-test-32bytes!",
    "token.jwt.refresh-key=test-refresh-key-test-refresh-key-test-32bytes!",
])
@Transactional
class MailReservationErrorReproduceTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val tokenProcessor: TokenProcessor,
    @Autowired private val userRepository: UserRepository,
) {

    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        val user = userRepository.save(
            User(
                userInfo = UserInfo(
                    name = "에러재현유저",
                    email = "error-test@example.com",
                    profileImageUrl = "https://example.com/profile.jpg",
                    oauthId = "oauth-error-test-${System.nanoTime()}",
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

        accessToken = tokenProcessor.encode(
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
    fun `파일 없이 JSON으로 전송하면 200 OK가 반환된다`() {
        mockMvc.perform(
            post("/api/mails/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer $accessToken")
        )
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun `multipart로 파일 첨부하여 전송하면 200 OK가 반환된다`() {
        val requestPart = MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.toByteArray()
        )

        mockMvc.perform(
            multipart("/api/mails/reservation")
                .file(requestPart)
                .header("Authorization", "Bearer $accessToken")
        )
            .andDo(print())
            .andExpect(status().isOk)
    }

    @Test
    fun `request 파트의 content-type이 없으면 415가 반환된다`() {
        val requestPart = MockMultipartFile(
            "request",
            "",
            null,
            requestJson.toByteArray()
        )

        mockMvc.perform(
            multipart("/api/mails/reservation")
                .file(requestPart)
                .header("Authorization", "Bearer $accessToken")
        )
            .andDo(print())
            .andExpect(status().isUnsupportedMediaType)
    }
}
