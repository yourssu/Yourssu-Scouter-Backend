package com.yourssu.scouter.common.application.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest

class CommonExceptionHandlerTest {

    private class TestableCommonExceptionHandler : CommonExceptionHandler() {
        fun callHandleExceptionInternal(
            ex: Exception,
            headers: HttpHeaders,
            status: HttpStatus,
            request: WebRequest
        ) = super.handleExceptionInternal(ex, null, headers, status, request)
    }

    private val handler = TestableCommonExceptionHandler()

    @Test
    fun `unhandled exception preserves original status code`() {
        val req = ServletWebRequest(MockHttpServletRequest())
        val res = handler.callHandleExceptionInternal(
            RuntimeException("boom"),
            HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR,
            req
        )
        val body = res?.body as ExceptionResponse
        assertThat(res.statusCode.value()).isEqualTo(500)
        assertThat(body.status).isEqualTo(500)
        assertThat(body.errorCode).isEqualTo("Internal Server Error")
    }

    @Test
    fun `handleExceptionInternal preserves 400 status for bad request`() {
        val req = ServletWebRequest(MockHttpServletRequest())
        val res = handler.callHandleExceptionInternal(
            RuntimeException("missing part"),
            HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            req
        )
        val body = res?.body as ExceptionResponse
        assertThat(res.statusCode.value()).isEqualTo(400)
        assertThat(body.status).isEqualTo(400)
        assertThat(body.errorCode).isEqualTo("Bad Request")
    }

    @Test
    fun `custom exception preserves status and errorCode`() {
        val ex = CustomException("reconsent", "GOOGLE_OAUTH_RECONSENT_REQUIRED", HttpStatus.FORBIDDEN)
        val request = MockHttpServletRequest().apply {
            method = "GET"
            requestURI = "/members/include-from-applicants"
            addHeader(HttpHeaders.AUTHORIZATION, "Bearer abcdef")
        }
        val res = handler.handleCustomException(ex, request)
        val body = res.body!!
        assertThat(res.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(body.status).isEqualTo(403)
        assertThat(body.errorCode).isEqualTo("GOOGLE_OAUTH_RECONSENT_REQUIRED")
        assertThat(body.message).contains("reconsent")
    }
}
