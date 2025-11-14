package com.yourssu.scouter.common.implement.support.security.oauth2

import com.yourssu.scouter.common.implement.support.exception.CustomException
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.http.HttpStatus

class GoogleFeignErrorDecoder(
    private val defaultDecoder: ErrorDecoder = ErrorDecoder.Default(),
) : ErrorDecoder {

    override fun decode(methodKey: String, response: Response): Exception {
        val status = response.status()
        val url = response.request().url()

        if (status == 403 && isGoogleApi(url)) {
            return CustomException(
                message = "Google API 권한 부족(재동의 필요)",
                errorCode = "GOOGLE_OAUTH_RECONSENT_REQUIRED",
                status = HttpStatus.FORBIDDEN,
            )
        }

        return defaultDecoder.decode(methodKey, response)
    }

    private fun isGoogleApi(url: String): Boolean {
        // forms, drive, gmail 등 구글 API 호출을 포괄
        return url.contains("googleapis.com")
    }
}
