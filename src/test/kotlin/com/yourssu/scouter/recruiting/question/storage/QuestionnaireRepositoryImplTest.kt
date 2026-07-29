package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.user.storage.UserEntity
import com.yourssu.scouter.common.division.storage.DivisionEntity
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.common.semester.implement.Term
import com.yourssu.scouter.common.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionCategory
import com.yourssu.scouter.recruiting.question.implement.QuestionGroup
import com.yourssu.scouter.recruiting.question.implement.Questionnaire
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestion
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
@Import(QuestionnaireRepositoryImpl::class, QuestionnaireQuestionRepositoryImpl::class, FixedQuestionRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class QuestionnaireRepositoryImplTest {

    @Autowired
    lateinit var questionnaireRepositoryImpl: QuestionnaireRepositoryImpl

    @Autowired
    lateinit var questionnaireQuestionRepositoryImpl: QuestionnaireQuestionRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var applicantId: Long = 0
    private var interviewerUserId: Long = 0
    private var fixedQuestionId: Long = 0

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
        val fixedQuestion = entityManager.persist(
            FixedQuestionEntity(category = FixedQuestionCategory.CULTURE_FIT, content = "협업 경험을 말씀해주세요.", sortOrder = 1),
        )
        applicantId = applicant.id!!
        interviewerUserId = interviewer.id!!
        fixedQuestionId = fixedQuestion.id!!

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `질문지가 없으면 null을 반환한다`() {
        val result = questionnaireRepositoryImpl.findByApplicantId(applicantId)

        assertThat(result).isNull()
    }

    @Test
    fun `질문지를 처음 저장하면 조회할 수 있다`() {
        questionnaireRepositoryImpl.upsert(Questionnaire(applicantId, interviewerUserId))

        val found = questionnaireRepositoryImpl.findByApplicantId(applicantId)

        assertThat(found).isNotNull
        assertThat(found!!.assignedInterviewerUserId).isEqualTo(interviewerUserId)
    }

    @Test
    fun `질문 목록을 전체 치환하면 이전 목록은 사라지고 새 목록만 남는다`() {
        questionnaireRepositoryImpl.upsert(Questionnaire(applicantId, interviewerUserId))
        questionnaireQuestionRepositoryImpl.replaceAll(
            applicantId,
            listOf(QuestionnaireQuestion(questionnaireId = applicantId, group = QuestionGroup.CULTURE_FIT, sourceQuestionId = fixedQuestionId, content = null, sortOrder = 0)),
        )

        val replaced = questionnaireQuestionRepositoryImpl.replaceAll(
            applicantId,
            listOf(QuestionnaireQuestion(questionnaireId = applicantId, group = QuestionGroup.PERSONAL, sourceQuestionId = null, content = "자유 문항", sortOrder = 0)),
        )

        val found = questionnaireQuestionRepositoryImpl.findAllByQuestionnaireId(applicantId)
        assertThat(replaced).hasSize(1)
        assertThat(found).hasSize(1)
        assertThat(found[0].group).isEqualTo(QuestionGroup.PERSONAL)
        assertThat(found[0].content).isEqualTo("자유 문항")
    }
}
