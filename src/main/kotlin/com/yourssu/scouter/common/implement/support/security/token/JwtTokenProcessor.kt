package com.yourssu.scouter.common.implement.support.security.token

import com.yourssu.scouter.common.implement.domain.authentication.Token
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.common.implement.support.exception.InvalidTokenException
import com.yourssu.scouter.common.implement.support.exception.InvalidTokenMessages
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import org.springframework.stereotype.Component

@Component
class JwtTokenProcessor(
    private val jwtProperties: JwtProperties,
) : TokenProcessor {

    companion object {
        const val TOKEN_PREFIX: String = "Bearer "
    }

    override fun encode(
        issueTime: LocalDateTime,
        tokenType: TokenType,
        privateClaims: Map<String, Any>
    ): String {
        val issueDate: Date = convertToDate(issueTime)
        val key: String = jwtProperties.findTokenKey(tokenType)
        val expiredHours: Long = jwtProperties.findExpiredHours(tokenType)
        val claimsWithTokenType: Map<String, Any> = buildMap {
            putAll(privateClaims)
            put("tokenType", tokenType.name)
        }

        return Jwts.builder()
            .issuedAt(issueDate)
            .expiration(Date(issueDate.time + expiredHours * 60 * 60 * 1000L))
            .claims(claimsWithTokenType)
            .signWith(Keys.hmacShaKeyFor(key.toByteArray(StandardCharsets.UTF_8)))
            .compact()
    }

    private fun convertToDate(targetTime: LocalDateTime): Date {
        val zonedDateTIme: ZonedDateTime = targetTime.atZone(ZoneId.of("Asia/Seoul"))

        return Date.from(zonedDateTIme.toInstant())
    }

    override fun decode(tokenType: TokenType, token: String): Claims? {
        val pureToken = findActualToken(token)

        // 1) 요청된 타입의 키로 먼저 검증
        val requestedKey: String = jwtProperties.findTokenKey(tokenType)
        parseWithKey(requestedKey, pureToken)?.let { claims ->
            val actualType: String? = claims["tokenType"] as? String
            if (actualType != null && actualType != tokenType.name) {
                val message = if (TokenType.REFRESH == tokenType) InvalidTokenMessages.NOT_REFRESH_TOKEN else InvalidTokenMessages.NOT_ACCESS_TOKEN
                throw InvalidTokenException(message)
            }
            return claims
        }

        // 2) 다른 타입의 키로 검증 성공하면 타입 불일치로 간주
        val otherType: TokenType = if (TokenType.ACCESS == tokenType) TokenType.REFRESH else TokenType.ACCESS
        val otherKey: String = jwtProperties.findTokenKey(otherType)
        parseWithKey(otherKey, pureToken)?.let {
            val message = if (TokenType.REFRESH == tokenType) InvalidTokenMessages.NOT_REFRESH_TOKEN else InvalidTokenMessages.NOT_ACCESS_TOKEN
            throw InvalidTokenException(message)
        }

        // 3) 어떤 키로도 파싱되지 않으면 유효하지 않은 토큰으로 간주
        return null
    }

    private fun parseWithKey(key: String, pureToken: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(key.toByteArray(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(pureToken)
                .payload
        } catch (_: JwtException) {
            null
        }
    }

    private fun findActualToken(token: String): String {
        val trimmed = token.trim()
        return if (trimmed.startsWith(TOKEN_PREFIX)) trimmed.substring(TOKEN_PREFIX.length) else trimmed
    }

    override fun generateToken(issueTime: LocalDateTime, privateClaims: Map<String, Any>): Token {
        val accessToken: String = encode(
            issueTime = issueTime,
            tokenType = TokenType.ACCESS,
            privateClaims = privateClaims,
        )
        val refreshToken: String = encode(
            issueTime = issueTime,
            tokenType = TokenType.REFRESH,
            privateClaims = privateClaims,
        )

        return Token(accessToken, refreshToken)
    }
}
