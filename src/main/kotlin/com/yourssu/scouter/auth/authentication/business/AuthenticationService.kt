package com.yourssu.scouter.auth.authentication.business

import com.yourssu.scouter.auth.authentication.business.dto.TokenDto

import com.yourssu.scouter.common.support.business.exception.NoSuchUserException
import com.yourssu.scouter.auth.authentication.implement.*
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.auth.user.implement.UserWriter
import com.yourssu.scouter.auth.support.exception.InvalidTokenException
import com.yourssu.scouter.auth.support.exception.InvalidTokenMessages
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuthenticationService(
    private val userReader: UserReader,
    private val userWriter: UserWriter,
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
            ?: throw InvalidTokenException(InvalidTokenMessages.INVALID_TOKEN)
        val privateClaims = PrivateClaims.from(claims)

        val userId: Long = privateClaims.userId
        if (!userReader.existsById(userId)) {
            throw NoSuchUserException("존재하지 않는 유저의 토큰입니다.")
        }
        if (blacklistTokenReader.isBlacklisted(userId, token)) {
            throw InvalidTokenException(InvalidTokenMessages.LOGGED_OUT)
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

    fun refreshToken(requestTime: Instant, refreshToken: String): TokenDto {
        val privateClaims: PrivateClaims = getValidPrivateClaims(TokenType.REFRESH, refreshToken)
        val token: Token = tokenProcessor.generateToken(requestTime, privateClaims.toMap())

        return TokenDto(token.accessToken, token.refreshToken)
    }

    fun withdraw(accessToken: String, refreshToken: String) {
        val privateClaims: PrivateClaims = getValidPrivateClaims(TokenType.ACCESS, accessToken)
        blacklistTokenWriter.write(privateClaims.userId, accessToken, refreshToken)
        userWriter.withdraw(privateClaims.userId)
    }
}
