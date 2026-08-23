package com.yourssu.scouter.auth.user.storage

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.implement.TokenInfo
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val profileImageUrl: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val authType: AuthType,

    @Column(unique = true)
    val oauthId: String? = null,

    @Enumerated(EnumType.STRING)
    val oauth2Type: OAuth2Type? = null,

    val password: String? = null,

    val tokenPrefix: String? = null,

    @Column(length = 511)
    val accessToken: String? = null,

    val refreshToken: String? = null,

    val accessTokenExpirationDateTime: Instant? = null,
) {

    companion object {
        fun from(user: User) = UserEntity(
            id = user.id,
            name = user.userInfo.name,
            email = user.userInfo.email,
            profileImageUrl = user.userInfo.profileImageUrl,
            authType = user.userInfo.authType,
            oauthId = user.userInfo.oauthId,
            oauth2Type = user.userInfo.oauth2Type,
            password = user.userInfo.password,
            tokenPrefix = user.tokenInfo?.tokenPrefix,
            accessToken = user.tokenInfo?.accessToken,
            refreshToken = user.tokenInfo?.refreshToken,
            accessTokenExpirationDateTime = user.tokenInfo?.accessTokenExpirationDateTime,
        )
    }

    fun toDomain() = User(
        id = id,
        userInfo = UserInfo(
            name = name,
            email = email,
            profileImageUrl = profileImageUrl,
            authType = authType,
            oauthId = oauthId,
            oauth2Type = oauth2Type,
            password = password,
        ),
        tokenInfo = run {
            val prefix = tokenPrefix
            val access = accessToken
            val refresh = refreshToken
            val expiration = accessTokenExpirationDateTime
            if (prefix != null && access != null && refresh != null && expiration != null) {
                TokenInfo(
                    tokenPrefix = prefix,
                    accessToken = access,
                    refreshToken = refresh,
                    accessTokenExpirationDateTime = expiration,
                )
            } else {
                null
            }
        },
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
