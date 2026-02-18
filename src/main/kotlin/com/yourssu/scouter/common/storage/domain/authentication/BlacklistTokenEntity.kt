package com.yourssu.scouter.common.storage.domain.authentication

import com.yourssu.scouter.common.implement.domain.authentication.BlacklistToken
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "blacklist_token")
class BlacklistTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tokenType: TokenType,

    @Column(nullable = false)
    val token: String,
) {

    companion object {
        fun from(blacklistToken: BlacklistToken) = BlacklistTokenEntity(
            id = blacklistToken.id,
            userId = blacklistToken.userId,
            tokenType = blacklistToken.tokenType,
            token = blacklistToken.token,
        )
    }

    fun toDomain() = BlacklistToken(
        id = id,
        userId = userId,
        tokenType = tokenType,
        token = token,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlacklistTokenEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
