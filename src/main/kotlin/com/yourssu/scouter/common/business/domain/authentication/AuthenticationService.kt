package com.yourssu.scouter.common.business.domain.authentication

import com.yourssu.scouter.common.business.support.exception.NoSuchUserException
import com.yourssu.scouter.common.implement.domain.authentication.*
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.domain.user.UserWriter
import com.yourssu.scouter.common.implement.support.exception.InvalidTokenException
import com.yourssu.scouter.common.implement.support.exception.InvalidTokenMessages
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
