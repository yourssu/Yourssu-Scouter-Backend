package com.yourssu.scouter.recruiting.applicant.application

import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantWebhookUnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class ApplicantWebhookSecretInterceptorTest {

    private val request: HttpServletRequest = mock(HttpServletRequest::class.java)
    private val response: HttpServletResponse = mock(HttpServletResponse::class.java)
    private val handler = Any()

    @Test
    fun `설정된 시크릿이 비어있으면 요청을 거부한다`() {
        val interceptor = ApplicantWebhookSecretInterceptor(ApplicantWebhookProperties(secret = ""))
        whenever(request.getHeader(ApplicantWebhookSecretInterceptor.SECRET_HEADER)).thenReturn("any-secret")

        assertThatThrownBy { interceptor.preHandle(request, response, handler) }
            .isInstanceOf(ApplicantWebhookUnauthorizedException::class.java)
    }

    @Test
    fun `헤더가 없으면 요청을 거부한다`() {
        val interceptor = ApplicantWebhookSecretInterceptor(ApplicantWebhookProperties(secret = "correct-secret"))
        whenever(request.getHeader(ApplicantWebhookSecretInterceptor.SECRET_HEADER)).thenReturn(null)

        assertThatThrownBy { interceptor.preHandle(request, response, handler) }
            .isInstanceOf(ApplicantWebhookUnauthorizedException::class.java)
    }

    @Test
    fun `헤더 값이 설정된 시크릿과 다르면 요청을 거부한다`() {
        val interceptor = ApplicantWebhookSecretInterceptor(ApplicantWebhookProperties(secret = "correct-secret"))
        whenever(request.getHeader(ApplicantWebhookSecretInterceptor.SECRET_HEADER)).thenReturn("wrong-secret")

        assertThatThrownBy { interceptor.preHandle(request, response, handler) }
            .isInstanceOf(ApplicantWebhookUnauthorizedException::class.java)
    }

    @Test
    fun `헤더 값이 설정된 시크릿과 일치하면 통과시킨다`() {
        val interceptor = ApplicantWebhookSecretInterceptor(ApplicantWebhookProperties(secret = "correct-secret"))
        whenever(request.getHeader(ApplicantWebhookSecretInterceptor.SECRET_HEADER)).thenReturn("correct-secret")

        val result = interceptor.preHandle(request, response, handler)

        assertThat(result).isTrue()
    }
}
