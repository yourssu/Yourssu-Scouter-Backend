package com.yourssu.scouter.common.application.support.authentication

import com.yourssu.scouter.common.application.support.exception.LoginRequiredException
import com.yourssu.scouter.common.business.domain.authentication.AuthenticationService
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.slf4j.LoggerFactory

@Component
class LoginInterceptor(
    private val authenticationService: AuthenticationService,
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(LoginInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (isPreflightRequest(request)) {
            return true
        }

        val accessToken: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (accessToken.isNullOrEmpty()) {
            logger.warn("[LoginInterceptor] Missing Authorization | {} {}", request.method, request.requestURI)
        } else if (!accessToken.startsWith("Bearer ")) {
            logger.warn("[LoginInterceptor] Invalid Authorization prefix | {} {} | preview={}", request.method, request.requestURI, accessToken.take(12) + "...")
        }
        if (accessToken.isNullOrEmpty()) {
            throw LoginRequiredException("로그인이 필요한 기능입니다.")
        }

        authenticationService.getValidPrivateClaims(TokenType.ACCESS, accessToken)

        return true
    }

    private fun isPreflightRequest(request: HttpServletRequest): Boolean {
        return request.method.equals(HttpMethod.OPTIONS.toString(), ignoreCase = true)
    }
}
