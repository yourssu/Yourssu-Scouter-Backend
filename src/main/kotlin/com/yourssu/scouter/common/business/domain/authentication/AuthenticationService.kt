package com.yourssu.scouter.common.business.domain.authentication

import com.yourssu.scouter.common.business.support.exception.NoSuchUserException
import com.yourssu.scouter.common.implement.domain.authentication.BlacklistTokenReader
import com.yourssu.scouter.common.implement.domain.authentication.BlacklistTokenWriter
import com.yourssu.scouter.common.implement.domain.authentication.PrivateClaims
import com.yourssu.scouter.common.implement.domain.authentication.Token
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.support.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userReader: UserReader,
    private val tokenProcessor: TokenProcessor,
    private val blacklistTokenWriter: BlacklistTokenWriter,
    private val blacklistTokenReader: BlacklistTokenReader,
) {

    fun logout(accessToken: String, refreshToken: String) {
        val privateClaims: PrivateClaims = getValidPrivateClaims(TokenType.ACCESS, accessToken)

        blacklistTokenWriter.write(privateClaims.userId, accessToken, refreshToken)
    }

    fun getValidPrivateClaims(tokenType: TokenType, token: String): PrivateClaims {
        val claims: Claims = tokenProcessor.decode(tokenType, token)
            ?: throw InvalidTokenException("유효한 토큰이 아닙니다.")
        val privateClaims = PrivateClaims.from(claims)

        val userId: Long = privateClaims.userId
        if (!userReader.existsById(userId)) {
            throw NoSuchUserException("존재하지 않는 유저의 토큰입니다.")
        }
        if (blacklistTokenReader.isBlacklisted(userId, token)) {
            throw InvalidTokenException("로그아웃되었습니다.")
        }

        return privateClaims
    }

    fun isValidToken(tokenType: TokenType, targetToken: String): Boolean {
        val claims: Claims = tokenProcessor.decode(tokenType, targetToken)
            ?: return false

        val userId = PrivateClaims.from(claims).userId

        return userReader.existsById(userId) &&
                !blacklistTokenReader.isBlacklisted(userId, targetToken)
    }

    fun refreshToken(requestTime: LocalDateTime, refreshToken: String): TokenDto {
        val privateClaims: PrivateClaims = getValidPrivateClaims(TokenType.REFRESH, refreshToken)
        val token: Token = tokenProcessor.generateToken(requestTime, privateClaims.toMap())

        return TokenDto(token.accessToken, token.refreshToken)
    }
}
