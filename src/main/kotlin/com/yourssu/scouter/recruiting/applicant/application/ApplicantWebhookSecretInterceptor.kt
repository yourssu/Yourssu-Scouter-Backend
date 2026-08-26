package com.yourssu.scouter.recruiting.applicant.application

import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantWebhookUnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
@EnableConfigurationProperties(ApplicantWebhookProperties::class)
class ApplicantWebhookSecretInterceptor(
    private val properties: ApplicantWebhookProperties,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val configured = properties.secret.trim()
        if (configured.isEmpty()) {
            throw ApplicantWebhookUnauthorizedException("웹훅 시크릿이 설정되지 않았습니다. scouter.applicant-webhook.secret을 설정해주세요.")
        }

        val provided = request.getHeader(SECRET_HEADER)?.trim()
        if (provided.isNullOrEmpty() || !secureEquals(provided, configured)) {
            throw ApplicantWebhookUnauthorizedException("웹훅 시크릿이 올바르지 않습니다.")
        }

        return true
    }

    private fun secureEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(StandardCharsets.UTF_8), b.toByteArray(StandardCharsets.UTF_8))

    companion object {
        const val SECRET_HEADER = "X-Applicant-Webhook-Secret"
    }
}
