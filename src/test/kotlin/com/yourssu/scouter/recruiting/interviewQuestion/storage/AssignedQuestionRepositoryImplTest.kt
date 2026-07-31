package com.yourssu.scouter.recruiting.interviewQuestion.storage

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.user.storage.UserEntity
import com.yourssu.scouter.common.division.storage.DivisionEntity
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.common.semester.implement.Term
import com.yourssu.scouter.common.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.Year

@DataJpaTest
@Import(AssignedQuestionRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class AssignedQuestionRepositoryImplTest {

    @Autowired
    lateinit var assignedQuestionRepositoryImpl: AssignedQuestionRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var applicantId: Long = 0
    private var interviewerUserId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        val part = entityManager.persist(PartEntity(null, division, "PM", 1))
        val semester = entityManager.persist(SemesterEntity(null, Year.of(2025), Term.FALL))
        val applicant = entityManager.persist(
            ApplicantEntity(
                id = null,
                name = "지원자",
                email = "applicant@example.com",
                phoneNumber = "010-0000-0000",
                age = "22",
                department = "컴퓨터학부",
                studentId = "202401002",
                part = part,
                state = ApplicantState.UNDER_REVIEW,
                applicationDateTime = Instant.parse("2025-11-13T00:00:00Z"),
                applicationSemester = semester,
                academicSemester = "3-2",
            ),
        )
        val interviewer = entityManager.persist(
            UserEntity(
                id = null,
                name = "면접관",
                email = "interviewer@example.com",
                profileImageUrl = "",
                oauthId = "oauth-interviewer",
                oauth2Type = OAuth2Type.GOOGLE,
                tokenPrefix = "Bearer",
                accessToken = "dummy",
                refreshToken = "dummy",
                accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
            ),
        )
        applicantId = applicant.id!!
        interviewerUserId = interviewer.id!!

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `지원자별 질문을 전체 치환하고 requirementIds를 함께 저장한다`() {
        assignedQuestionRepositoryImpl.replaceAll(
            applicantId,
            listOf(personalQuestion("이전 질문", 0, listOf(1L))),
        )

        val replaced = assignedQuestionRepositoryImpl.replaceAll(
            applicantId,
            listOf(personalQuestion("새 질문", 0, listOf(11L, 12L))),
        )

        val found = assignedQuestionRepositoryImpl.findAllByApplicantId(applicantId)

        assertThat(replaced).hasSize(1)
        assertThat(found).hasSize(1)
        assertThat(found[0].content).isEqualTo("새 질문")
        assertThat(found[0].assignedInterviewerUserId).isEqualTo(interviewerUserId)
        assertThat(found[0].isSelected).isTrue()
        assertThat(found[0].requirementIds).containsExactly(11L, 12L)
    }

    private fun personalQuestion(content: String, sortOrder: Int, requirementIds: List<Long>): AssignedQuestion {
        return AssignedQuestion(
            assignedInterviewerUserId = interviewerUserId,
            applicantId = applicantId,
            sourceQuestionId = null,
            content = content,
            category = AssignedQuestionCategory.PERSONAL,
            sortOrder = sortOrder,
            isSelected = true,
            requirementIds = requirementIds,
        )
    }
}
