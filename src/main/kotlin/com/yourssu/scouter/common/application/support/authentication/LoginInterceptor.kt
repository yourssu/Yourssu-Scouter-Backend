package com.yourssu.scouter.common.application.support.authentication

import com.yourssu.scouter.common.application.support.exception.LoginRequiredException
import com.yourssu.scouter.common.business.domain.authentication.AuthenticationService
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LoginInterceptor(
    private val authenticationService: AuthenticationService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val accessToken: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (accessToken.isNullOrEmpty()) {
            throw LoginRequiredException("로그인이 필요한 기능입니다.")
        }

        authenticationService.getValidPrivateClaims(TokenType.ACCESS, accessToken)

        return true
    }
}
