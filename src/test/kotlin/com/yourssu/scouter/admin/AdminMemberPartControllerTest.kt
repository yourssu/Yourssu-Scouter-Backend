package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.department.storage.DepartmentEntity
import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.member.core.implement.MemberRole
import com.yourssu.scouter.member.core.implement.MemberState
import com.yourssu.scouter.member.core.storage.ActiveMemberEntity
import com.yourssu.scouter.member.core.storage.MemberEntity
import com.yourssu.scouter.member.core.storage.MemberPartEntity
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
import java.time.LocalDate

/**
 * 이 화면이 꼭 보여야 하는 대상은 파트가 0개인 멤버다.
 * 그런데 MemberService.readAllActiveByFilters 의 members.sorted() 는 Member.compareTo -> parts.first() 를 타서
 * 그런 멤버가 섞이면 멤버가 2명 이상일 때 터진다. 그래서 이 테스트는 멤버를 반드시 둘 이상 심는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Suppress("NonAsciiCharacters")
class AdminMemberPartControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var em: EntityManager

    private lateinit var part: PartEntity
    private var withPartId: Long = 0
    private var withoutPartId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = DivisionEntity(null, "화면디비전", 95)
        em.persist(division)
        part = PartEntity(null, division, "화면파트", 95)
        em.persist(part)

        withPartId = persistMember("파트있음", "with-part@example.com", "010-9100-0001", "20260201", "withpart")
        em.persist(MemberPartEntity(null, em.find(MemberEntity::class.java, withPartId), part))
        withoutPartId = persistMember("파트없음", "without-part@example.com", "010-9100-0002", "20260202", "withoutpart")

        em.flush()
        em.clear()
    }

    @Test
    fun `파트가 없는 멤버가 섞여 있어도 목록이 그려진다`() {
        val body = mockMvc.perform(
            get("/admin/recruiting/member-parts")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true),
        ).andReturn().response.also { assertThat(it.status).isEqualTo(200) }.contentAsString

        assertThat(body).contains("with-part@example.com")
        assertThat(body).contains("without-part@example.com")
        assertThat(body).contains("배정된 파트 없음")
        assertThat(body).contains("""data-member-id="$withoutPartId"""")
        assertThat(body).contains("""data-member-id="$withPartId"""")
    }

    @Test
    fun `파트 필터는 그 파트를 가진 멤버만 남긴다`() {
        val body = mockMvc.perform(
            get("/admin/recruiting/member-parts")
                .param("partId", part.id!!.toString())
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true),
        ).andReturn().response.also { assertThat(it.status).isEqualTo(200) }.contentAsString

        assertThat(body).contains("with-part@example.com")
        assertThat(body).doesNotContain("without-part@example.com")
    }

    @Test
    fun `세션이 없으면 로그인 화면으로 돌려보낸다`() {
        val response = mockMvc.perform(get("/admin/recruiting/member-parts")).andReturn().response

        assertThat(response.status).isEqualTo(302)
        assertThat(response.redirectedUrl).isEqualTo("/admin/recruiting")
    }

    @Test
    fun `배정은 화면에 반영되고 마지막 파트 해제는 거부된다`() {
        val assign = mockMvc.perform(
            post("/admin/recruiting/member-parts/assign")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("memberId", withoutPartId.toString())
                .param("partId", part.id!!.toString()),
        ).andReturn().response
        assertThat(assign.status).isEqualTo(200)

        em.flush()
        em.clear()

        val afterAssign = mockMvc.perform(
            get("/admin/recruiting/member-parts")
                .param("partId", part.id!!.toString())
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true),
        ).andReturn().response.contentAsString
        assertThat(afterAssign).contains("without-part@example.com")

        val unassign = mockMvc.perform(
            post("/admin/recruiting/member-parts/unassign")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("memberId", withoutPartId.toString())
                .param("partId", part.id!!.toString()),
        ).andReturn().response
        assertThat(unassign.status).isEqualTo(400)
        assertThat(unassign.contentAsString).contains("마지막 파트")
    }

    @Test
    fun `허용되지 않는 상태 값은 오류를 띄우고 액티브로 되돌린다`() {
        val body = mockMvc.perform(
            get("/admin/recruiting/member-parts")
                .param("state", "NOT_A_STATE")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true),
        ).andReturn().response.also { assertThat(it.status).isEqualTo(200) }.contentAsString

        assertThat(body).contains("허용되지 않는 상태 값입니다")
        assertThat(body).contains("with-part@example.com")
    }

    private fun persistMember(name: String, email: String, phone: String, studentId: String, nickname: String): Long {
        val department = em.createQuery("SELECT d FROM DepartmentEntity d", DepartmentEntity::class.java)
            .setMaxResults(1)
            .singleResult
        val member = MemberEntity(
            id = null,
            name = name,
            email = email,
            phoneNumber = phone,
            birthDate = LocalDate.of(2000, 1, 1),
            department = department,
            studentId = studentId,
            role = MemberRole.MEMBER,
            nicknameEnglish = nickname,
            nicknameKorean = name,
            state = MemberState.ACTIVE,
            joinDate = LocalDate.of(2024, 3, 1),
            note = "",
            stateUpdatedTime = Instant.now(),
        )
        em.persist(member)
        em.persist(ActiveMemberEntity(null, member, false, 3, false))
        em.flush()

        return member.id!!
    }
}
