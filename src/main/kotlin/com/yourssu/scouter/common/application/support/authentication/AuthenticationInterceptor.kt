package com.yourssu.scouter.common.application.support.authentication

import com.yourssu.scouter.common.business.support.exception.NoSuchUserException
import com.yourssu.scouter.common.implement.domain.authentication.BlacklistTokenReader
import com.yourssu.scouter.common.implement.domain.authentication.PrivateClaims
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.support.exception.InvalidTokenException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val tokenProcessor: TokenProcessor,
    private val userReader: UserReader,
    private val blacklistTokenReader: BlacklistTokenReader,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val accessToken: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (accessToken.isNullOrEmpty()) {
            return true
        }

        val claims = (tokenProcessor.decode(TokenType.ACCESS, accessToken)
            ?: throw InvalidTokenException("유효한 토큰이 아닙니다."))
        val privateClaims = PrivateClaims.from(claims)

        val userId = privateClaims.userId
        if (!userReader.existsById(userId)) {
            throw NoSuchUserException("존재하지 않는 사용자의 토큰입니다.")
        }
        if (blacklistTokenReader.isBlacklisted(userId, accessToken)) {
            throw InvalidTokenException("로그아웃되었습니다.")
        }

        return true
    }
}
