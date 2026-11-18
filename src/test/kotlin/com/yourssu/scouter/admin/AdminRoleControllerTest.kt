package com.yourssu.scouter.admin

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.authorization.business.AuthorizationService
import com.yourssu.scouter.auth.authorization.implement.Role
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.storage.UserEntity
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Suppress("NonAsciiCharacters")
class AdminRoleControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var authorizationService: AuthorizationService

    @Autowired
    lateinit var em: EntityManager

    @BeforeEach
    fun setUp() {
        persistUser("화면첫번째", "role-page-1@example.com", "oauth-page-1")
        persistUser("화면두번째", "role-page-2@example.com", "oauth-page-2")
        em.flush()
        em.clear()
    }

    @Test
    fun `세션이 없으면 로그인 화면으로 돌려보낸다`() {
        val response = mockMvc.perform(get("/admin/recruiting/roles")).andReturn().response

        assertThat(response.status).isEqualTo(302)
        assertThat(response.redirectedUrl).isEqualTo("/admin/recruiting")
    }

    @Test
    fun `보유자가 없으면 빈 안내를 띄운다`() {
        val body = page()

        assertThat(body).contains("권한을 가진 사용자가 없습니다")
    }

    @Test
    fun `부여하면 목록에 나타난다`() {
        val granted = mockMvc.perform(
            post("/admin/recruiting/roles/grant")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("email", "role-page-1@example.com"),
        ).andReturn().response
        assertThat(granted.status).isEqualTo(200)
        assertThat(granted.contentAsString).contains("\"changed\":true")

        em.flush()
        em.clear()

        val body = page()
        assertThat(body).contains("role-page-1@example.com")
        assertThat(body).contains("화면첫번째")
        assertThat(body).doesNotContain("role-page-2@example.com")
    }

    @Test
    fun `이미 가진 사용자에게 다시 부여하면 changed 가 false 다`() {
        authorizationService.grant("role-page-1@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        val response = mockMvc.perform(
            post("/admin/recruiting/roles/grant")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("email", "role-page-1@example.com"),
        ).andReturn().response

        assertThat(response.status).isEqualTo(200)
        assertThat(response.contentAsString).contains("\"changed\":false")
    }

    @Test
    fun `가입 이력이 없는 이메일은 400 이다`() {
        val response = mockMvc.perform(
            post("/admin/recruiting/roles/grant")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("email", "nobody@example.com"),
        ).andReturn().response

        assertThat(response.status).isEqualTo(400)
        assertThat(response.contentAsString).contains("가입한 사용자가 없습니다")
    }

    @Test
    fun `마지막 한 명 회수는 400 이고 두 명이면 회수된다`() {
        authorizationService.grant("role-page-1@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        val lastOne = mockMvc.perform(
            post("/admin/recruiting/roles/revoke")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("email", "role-page-1@example.com"),
        ).andReturn().response
        assertThat(lastOne.status).isEqualTo(400)
        assertThat(lastOne.contentAsString).contains("마지막")

        authorizationService.grant("role-page-2@example.com", Role.SCOUTER_ADMIN)
        em.flush()
        em.clear()

        val revoked = mockMvc.perform(
            post("/admin/recruiting/roles/revoke")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("email", "role-page-1@example.com"),
        ).andReturn().response
        assertThat(revoked.status).isEqualTo(200)
        assertThat(revoked.contentAsString).contains("\"changed\":true")

        em.flush()
        em.clear()

        val body = page()
        assertThat(body).doesNotContain("role-page-1@example.com")
        assertThat(body).contains("role-page-2@example.com")
    }

    private fun page(): String = mockMvc.perform(
        get("/admin/recruiting/roles")
            .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true),
    ).andReturn().response.also { assertThat(it.status).isEqualTo(200) }.contentAsString

    private fun persistUser(name: String, email: String, oauthId: String) {
        em.persist(
            UserEntity(
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
            ),
        )
    }
}
