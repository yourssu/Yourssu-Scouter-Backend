package com.yourssu.scouter.auth.authorization.storage

import com.yourssu.scouter.auth.authorization.implement.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(UserRoleRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class UserRoleRepositoryImplTest {

    @Autowired
    lateinit var userRoleRepositoryImpl: UserRoleRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `role을 저장하면 해당 role을 가진 것으로 조회된다`() {
        // given
        val userId = 1L
        entityManager.persist(UserRoleEntity(userId = userId, role = Role.SCOUTER_ADMIN))
        entityManager.flush()
        entityManager.clear()

        // when
        val result = userRoleRepositoryImpl.existsByUserIdAndRole(userId, Role.SCOUTER_ADMIN)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `role이 저장되어 있지 않으면 조회되지 않는다`() {
        // when
        val result = userRoleRepositoryImpl.existsByUserIdAndRole(999L, Role.SCOUTER_ADMIN)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `유저의 모든 role을 조회한다`() {
        // given
        val userId = 2L
        entityManager.persist(UserRoleEntity(userId = userId, role = Role.SCOUTER_ADMIN))
        entityManager.flush()
        entityManager.clear()

        // when
        val result = userRoleRepositoryImpl.findAllByUserId(userId)

        // then
        assertThat(result).hasSize(1)
        assertThat(result.first().userId).isEqualTo(userId)
        assertThat(result.first().role).isEqualTo(Role.SCOUTER_ADMIN)
    }
}
