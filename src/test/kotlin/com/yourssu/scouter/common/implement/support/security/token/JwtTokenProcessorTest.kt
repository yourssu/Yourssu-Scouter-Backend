package com.yourssu.scouter.common.implement.support.security.token

import com.yourssu.scouter.common.implement.support.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import java.time.LocalDateTime
import java.time.ZoneId
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class JwtTokenProcessorTest {

    private val jwtProperties = JwtProperties(
        "ThisIsLocalAccessKeyThisIsLocalAccessKey",
        "ThisIsLocalRefreshKeyThisIsLocalRefreshKey",
        1L,
        336L,
    )

    @Test
    fun `토큰을 인코딩한다`() {
        // given
        val tokenProcessor = JwtTokenProcessor(jwtProperties)
        val privateClaims = mapOf("userId" to 1L)

        // when
        val actual = tokenProcessor.encode(LocalDateTime.now(), TokenType.ACCESS, privateClaims)

        // then
        assertThat(actual).contains("Bearer ")
    }

    @Test
    fun `토큰의 타입이 유효하지 않다면 예외가 발생한다`() {
        // given
        val tokenProcessor = JwtTokenProcessor(jwtProperties)
        val invalidTypeToken = "Basic12 abcde"

        // when & then
        Assertions.assertThatThrownBy { tokenProcessor.decode(TokenType.ACCESS, invalidTypeToken) }
            .isInstanceOf(InvalidTokenException::class.java)
            .hasMessage("Bearer 타입이 아닙니다.")
    }

    @Test
    fun `토큰이 유효하지 않다면 null을 반환한다`() {
        // given
        val tokenProcessor = JwtTokenProcessor(jwtProperties)
        val invalidToken = "Bearer abcde"

        // when
        val actual: Claims? = tokenProcessor.decode(TokenType.ACCESS, invalidToken)

        // then
        assertThat(actual).isNull()
    }

    @Test
    fun `유효한 토큰이면 디코딩한 값을 반환한다`() {
        // given
        val tokenProcessor = JwtTokenProcessor(jwtProperties)
        val keyName = "userId"
        val userId = 1L

        val privateClaims = mapOf(keyName to userId)
        val validToken = tokenProcessor.encode(
            LocalDateTime.now(ZoneId.of("Asia/Seoul")),
            TokenType.ACCESS,
            privateClaims
        )

        // when
        val actual: Claims? = tokenProcessor.decode(TokenType.ACCESS, validToken)

        // then
        assertThat((actual!![keyName] as Number).toLong()).isEqualTo(userId)
    }
}
