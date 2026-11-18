package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.implement.Term
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantAnswerEntity
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.rubric.storage.DocumentSectionEntity
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
import java.time.Year

/**
 * 상세 화면은 서류 평가 문항(document_section)이 있는 파트에서만 드롭다운을 그린다.
 * 실행 중인 앱으로는 document_section 을 만들 수 없어(#518 과 같은 제약) 여기서 렌더링을 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Suppress("NonAsciiCharacters")
class AdminApplicantControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var em: EntityManager

    private var applicantId: Long = 0
    private var sectionId: Long = 0
    private var otherSectionId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = DivisionEntity(null, "렌더디비전", 98)
        em.persist(division)
        val part = PartEntity(null, division, "렌더파트", 98)
        em.persist(part)
        val semester = em.createQuery(
            "SELECT s FROM SemesterEntity s WHERE s.year = :year AND s.term = :term",
            SemesterEntity::class.java,
        ).setParameter("year", Year.of(2026)).setParameter("term", Term.FALL).singleResult

        val section = DocumentSectionEntity(null, part, "지원 동기를 적어 주세요", 60, "지원 동기")
        em.persist(section)
        val otherSection = DocumentSectionEntity(null, part, "문제 해결 경험을 적어 주세요", 40, "문제 해결")
        em.persist(otherSection)

        val applicant = ApplicantEntity(
            id = null,
            name = "렌더지원자",
            email = "render-519@example.com",
            phoneNumber = "010-0000-0000",
            age = "23",
            department = "컴퓨터학부",
            studentId = "202601001",
            part = part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = Instant.parse("2026-09-01T00:00:00Z"),
            applicationSemester = semester,
            academicSemester = "3-1",
        )
        em.persist(applicant)
        em.flush()

        em.persist(ApplicantAnswerEntity(null, applicant, "지원 동기를 적어 주세요", "백엔드를 하고 싶습니다", section.id))
        em.flush()
        em.clear()

        applicantId = applicant.id!!
        sectionId = section.id!!
        otherSectionId = otherSection.id!!
    }

    @Test
    fun `상세 화면은 지원서 답변과 서류 평가 문항 드롭다운을 그린다`() {
        val body = mockMvc.perform(
            get("/admin/recruiting/applicants")
                .param("filtered", "true")
                .param("applicantId", applicantId.toString())
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true),
        ).andReturn().response.also { assertThat(it.status).isEqualTo(200) }.contentAsString

        assertThat(body).contains("render-519@example.com")
        assertThat(body).contains("백엔드를 하고 싶습니다")
        assertThat(body).contains("문제 해결 경험을 적어 주세요")
        // 답변이 가리키는 문항만 선택된 채로 그려져야 한다. 속성 사이 줄바꿈은 템플릿 들여쓰기 때문이다.
        assertThat(body).containsPattern("""<option value="$sectionId"\s+selected="selected">""")
        assertThat(body).containsPattern("""<option value="$otherSectionId">""")
    }

    @Test
    fun `세션이 없으면 로그인 화면으로 돌려보낸다`() {
        val response = mockMvc.perform(get("/admin/recruiting/applicants")).andReturn().response

        assertThat(response.status).isEqualTo(302)
        assertThat(response.redirectedUrl).isEqualTo("/admin/recruiting")
    }

    @Test
    fun `이름이 비면 추가를 거부한다`() {
        val response = mockMvc.perform(
            post("/admin/recruiting/applicants/create")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("name", "  ")
                .param("email", "blank-name@example.com")
                .param("phoneNumber", "010-1111-2222")
                .param("age", "23")
                .param("departmentId", "1")
                .param("studentId", "202601002")
                .param("partId", "1")
                .param("state", "UNDER_REVIEW")
                .param("applicationDate", "2026-09-01")
                .param("applicationSemesterId", "1")
                .param("academicSemester", "3-1"),
        ).andReturn().response

        assertThat(response.status).isEqualTo(400)
        assertThat(response.contentAsString).contains("이름은 비워 둘 수 없습니다")
    }

    @Test
    fun `전화번호 형식이 틀리면 수정을 거부한다`() {
        val response = mockMvc.perform(
            post("/admin/recruiting/applicants/update")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("applicantId", applicantId.toString())
                .param("phoneNumber", "01012345678"),
        ).andReturn().response

        assertThat(response.status).isEqualTo(400)
        assertThat(response.contentAsString).contains("010-0000-0000 형식")
    }

    @Test
    fun `빈 값만 보낸 수정은 아무것도 바꾸지 않는다`() {
        val response = mockMvc.perform(
            post("/admin/recruiting/applicants/update")
                .sessionAttr(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
                .param("applicantId", applicantId.toString())
                .param("name", "")
                .param("email", "")
                .param("phoneNumber", "")
                .param("academicSemester", ""),
        ).andReturn().response

        assertThat(response.status).isEqualTo(200)

        em.flush()
        em.clear()
        val reloaded = em.find(ApplicantEntity::class.java, applicantId)
        assertThat(reloaded.name).isEqualTo("렌더지원자")
        assertThat(reloaded.email).isEqualTo("render-519@example.com")
        assertThat(reloaded.academicSemester).isEqualTo("3-1")
    }
}
