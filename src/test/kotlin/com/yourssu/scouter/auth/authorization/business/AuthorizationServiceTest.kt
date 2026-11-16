package com.yourssu.scouter.auth.authorization.business

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.authorization.implement.Role
import com.yourssu.scouter.auth.support.exception.LastRoleHolderException
import com.yourssu.scouter.auth.support.exception.UserNotFoundException
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.storage.UserEntity
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * user_role 에는 (user_id, role) 유니크 제약이 있다(V25). 그래서 부여는 멱등해야 한다.
 * 테스트 프로필은 Flyway 를 끄고 엔티티로 스키마를 만들기 때문에 H2 에는 그 제약이 없다.
 * "예외가 안 났다"로는 증명이 안 되므로 행 수를 직접 센다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Suppress("NonAsciiCharacters")
class AuthorizationServiceTest {

    @Autowired
    lateinit var authorizationService: AuthorizationService

    @Autowired
    lateinit var em: EntityManager

    private var firstUserId: Long = 0
    private var secondUserId: Long = 0

    @BeforeEach
    fun setUp() {
        firstUserId = persistUser("권한첫번째", "role-first@example.com", "oauth-role-1")
        secondUserId = persistUser("권한두번째", "role-second@example.com", "oauth-role-2")
        em.flush()
        em.clear()
    }

    @Test
    fun `권한이 없던 사용자에게 부여하면 true 를 돌려준다`() {
        val granted = authorizationService.grant("role-first@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        assertThat(granted).isTrue
        assertThat(authorizationService.hasRole(firstUserId, Role.SCOUTER_ADMIN)).isTrue
        assertThat(roleRowCount(firstUserId)).isEqualTo(1)
    }

    @Test
    fun `이미 가진 사용자에게 다시 부여해도 터지지 않고 행도 늘지 않는다`() {
        authorizationService.grant("role-first@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        val grantedAgain = authorizationService.grant("role-first@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        assertThat(grantedAgain).isFalse
        assertThat(roleRowCount(firstUserId)).isEqualTo(1)
    }

    @Test
    fun `가입 이력이 없는 이메일에는 부여할 수 없다`() {
        assertThatThrownBy { authorizationService.grant("nobody@example.com", Role.SCOUTER_ADMIN) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `두 명 이상일 때는 회수된다`() {
        authorizationService.grant("role-first@example.com", Role.SCOUTER_ADMIN)
        authorizationService.grant("role-second@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        val revoked = authorizationService.revoke("role-first@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        assertThat(revoked).isTrue
        assertThat(authorizationService.hasRole(firstUserId, Role.SCOUTER_ADMIN)).isFalse
        assertThat(authorizationService.hasRole(secondUserId, Role.SCOUTER_ADMIN)).isTrue
    }

    @Test
    fun `마지막 한 명은 회수할 수 없다`() {
        authorizationService.grant("role-first@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        assertThatThrownBy { authorizationService.revoke("role-first@example.com", Role.SCOUTER_ADMIN) }
            .isInstanceOf(LastRoleHolderException::class.java)
            .hasMessageContaining("마지막")

        assertThat(authorizationService.hasRole(firstUserId, Role.SCOUTER_ADMIN)).isTrue
    }

    @Test
    fun `권한이 없는 사용자를 회수하면 false 를 돌려준다`() {
        authorizationService.grant("role-first@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        val revoked = authorizationService.revoke("role-second@example.com", Role.SCOUTER_ADMIN)

        assertThat(revoked).isFalse
        assertThat(authorizationService.hasRole(firstUserId, Role.SCOUTER_ADMIN)).isTrue
    }

    @Test
    fun `보유자 목록은 이름과 이메일을 이메일 순으로 돌려준다`() {
        authorizationService.grant("role-second@example.com", Role.SCOUTER_ADMIN)
        authorizationService.grant("role-first@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        val holders = authorizationService.readHolders(Role.SCOUTER_ADMIN)

        assertThat(holders.map { it.email })
            .containsExactly("role-first@example.com", "role-second@example.com")
        assertThat(holders.map { it.name }).containsExactly("권한첫번째", "권한두번째")
    }

    @Test
    fun `아무도 없으면 빈 목록이다`() {
        assertThat(authorizationService.readHolders(Role.SCOUTER_ADMIN)).isEmpty()
    }

    private fun roleRowCount(userId: Long): Long =
        em.createQuery(
            "SELECT COUNT(r) FROM UserRoleEntity r WHERE r.userId = :id AND r.role = :role",
            java.lang.Long::class.java,
        ).setParameter("id", userId).setParameter("role", Role.SCOUTER_ADMIN).singleResult.toLong()

    private fun persistUser(name: String, email: String, oauthId: String): Long {
        val user = UserEntity(
            id = null,
            name = name,
            email = email,
            profileImageUrl = "",
            authType = AuthType.OAUTH2,
            oauthId = oauthId,
            oauth2Type = OAuth2Type.GOOGLE,
            tokenPrefix = "Bearer",
            accessToken = "dummy",
            refreshToken = "dummy",
            accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
        )
        em.persist(user)
        em.flush()

        return user.id!!
    }
}
