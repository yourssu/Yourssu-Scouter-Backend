package com.yourssu.scouter.common.implement.support.security.token

import com.yourssu.scouter.common.implement.support.exception.InvalidTokenException
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

        return TOKEN_PREFIX + Jwts.builder()
            .issuedAt(issueDate)
            .expiration(Date(issueDate.time + expiredHours * 60 * 60 * 1000L))
            .claims(privateClaims)
            .signWith(Keys.hmacShaKeyFor(key.toByteArray(StandardCharsets.UTF_8)))
            .compact()
    }

    private fun convertToDate(targetTime: LocalDateTime): Date {
        val zonedDateTIme: ZonedDateTime = targetTime.atZone(ZoneId.of("Asia/Seoul"))

        return Date.from(zonedDateTIme.toInstant())
    }

    override fun decode(tokenType: TokenType, token: String): Claims? {
        validateBearerToken(token)

        return parseToClaims(tokenType, token)
    }

    private fun validateBearerToken(token: String) {
        if (!token.startsWith(TOKEN_PREFIX)) {
            throw InvalidTokenException("Bearer 타입이 아닙니다.")
        }
    }

    private fun parseToClaims(tokenType: TokenType, token: String): Claims? {
        val key: String = jwtProperties.findTokenKey(tokenType)

        return try {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(key.toByteArray(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(findPureToken(token))
                .payload
        } catch (_: JwtException) {
            null
        }
    }

    private fun findPureToken(token: String): String {
        return token.substring(TOKEN_PREFIX.length)
    }
}
