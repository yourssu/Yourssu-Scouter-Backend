package com.yourssu.scouter.common.storage.domain.user

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.user.TokenInfo
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime


@Entity
@Table(name = "users")
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val email: String,

    @Column(nullable = false)
    val profileImageUrl: String,

    @Column(nullable = false, unique = true)
    val oauthId: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val oauth2Type: OAuth2Type,

    @Column(nullable = false)
    val tokenPrefix: String,

    @Column(nullable = false)
    val accessToken: String,

    @Column(nullable = false)
    val refreshToken: String,

    @Column(nullable = false)
    val accessTokenExpirationDateTime: LocalDateTime,
) {

    companion object {
        fun from(user: User) = UserEntity(
            id = user.id,
            name = user.userInfo.name,
            email = user.userInfo.email,
            profileImageUrl = user.userInfo.profileImageUrl,
            oauthId = user.userInfo.oauthId,
            oauth2Type = user.userInfo.oauth2Type,
            tokenPrefix = user.tokenInfo.tokenPrefix,
            accessToken = user.tokenInfo.accessToken,
            refreshToken = user.tokenInfo.refreshToken,
            accessTokenExpirationDateTime = user.tokenInfo.accessTokenExpirationDateTime,
        )
    }

    fun toDomain() = User(
        id = id,
        userInfo = UserInfo(
            name = name,
            email = email,
            profileImageUrl = profileImageUrl,
            oauthId = oauthId,
            oauth2Type = oauth2Type,
        ),
        tokenInfo = TokenInfo(
            tokenPrefix = tokenPrefix,
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpirationDateTime = accessTokenExpirationDateTime,
        ),
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
