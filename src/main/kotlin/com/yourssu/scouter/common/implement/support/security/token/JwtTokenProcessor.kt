package com.yourssu.scouter.common.implement.support.security.token

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
}
