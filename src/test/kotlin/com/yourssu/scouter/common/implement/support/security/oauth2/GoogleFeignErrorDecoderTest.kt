package com.yourssu.scouter.common.implement.support.security.oauth2

import com.yourssu.scouter.common.implement.support.exception.CustomException
import feign.Request
import feign.Response
import feign.codec.ErrorDecoder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class GoogleFeignErrorDecoderTest {

    private val decoder: ErrorDecoder = GoogleFeignErrorDecoder()

    @Test
    fun `403 from googleapis maps to re-consent CustomException`() {
        val request = Request.create(
            Request.HttpMethod.GET,
            "https://forms.googleapis.com/v1/forms/abc",
            mapOf(),
            null,
            StandardCharsets.UTF_8,
            null
        )
        val response = Response.builder()
            .status(403)
            .request(request)
            .reason("Forbidden")
            .build()

        val ex = decoder.decode("forms#get", response) as CustomException
        assertThat(ex.status.value()).isEqualTo(403)
        assertThat(ex.errorCode).isEqualTo("GOOGLE_OAUTH_RECONSENT_REQUIRED")
        assertThat(ex.message).contains("권한")
    }
}
