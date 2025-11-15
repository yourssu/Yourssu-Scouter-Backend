package com.yourssu.scouter.common.implement.support.security.oauth2

import com.yourssu.scouter.common.implement.support.exception.CustomException
import feign.FeignException
import feign.Request
import feign.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.nio.charset.StandardCharsets

class GoogleOAuth2HandlerTest {

    private val props = GoogleOAuth2Properties(
        clientId = "id",
        clientSecret = "secret",
        redirectPath = "/oauth/callback/google",
        allowedRedirectUris = listOf("http://localhost:5173/oauth/callback/google"),
        scope = listOf("openid")
    )
    private val tokenClient: GoogleOAuth2TokenClient = mock()
    private val userClient: GoogleUserApiClient = mock()
    private val handler = GoogleOAuth2Handler(props, tokenClient, userClient)

    private fun feignException(status: Int, url: String): FeignException {
        val req = Request.create(Request.HttpMethod.POST, url, mapOf(), null, StandardCharsets.UTF_8, null)
        val res = Response.builder().request(req).status(status).reason("err").build()
        return FeignException.errorStatus("fetchToken", res)
    }

    @Test
    fun `token exchange 401 maps to 401`() {
        whenever(tokenClient.fetchToken(any())).thenThrow(feignException(401, "https://oauth2.googleapis.com/token"))

        val ex = assertThrows<CustomException> {
            handler.fetchOAuth2User("code", "http://localhost:5173", null)
        }
        assertThat(ex.status).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(ex.errorCode).isEqualTo("OAuth-Token-Exchange-Fail")
    }

    @Test
    fun `token exchange non-401 maps to 400`() {
        whenever(tokenClient.fetchToken(any())).thenThrow(feignException(400, "https://oauth2.googleapis.com/token"))

        val ex = assertThrows<CustomException> {
            handler.fetchOAuth2User("code", "http://localhost:5173", null)
        }
        assertThat(ex.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(ex.errorCode).isEqualTo("OAuth-Token-Exchange-Fail")
    }
}
