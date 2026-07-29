package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.user.storage.UserEntity
import com.yourssu.scouter.common.division.storage.DivisionEntity
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.common.semester.implement.Term
import com.yourssu.scouter.common.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemo
import com.yourssu.scouter.recruiting.question.implement.QuestionGroup
import com.yourssu.scouter.recruiting.question.storage.QuestionnaireEntity
import com.yourssu.scouter.recruiting.question.storage.QuestionnaireQuestionEntity
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
@Import(InterviewMemoRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class InterviewMemoRepositoryImplTest {

    @Autowired
    lateinit var interviewMemoRepositoryImpl: InterviewMemoRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var questionId1: Long = 0
    private var questionId2: Long = 0

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
        entityManager.persist(
            QuestionnaireEntity(applicantId = applicant.id!!, assignedInterviewerUserId = interviewer.id!!),
        )
        val question1 = entityManager.persist(
            QuestionnaireQuestionEntity(
                questionnaireId = applicant.id!!,
                group = QuestionGroup.PERSONAL,
                sourceQuestionId = null,
                content = "질문1",
                sortOrder = 0,
            ),
        )
        val question2 = entityManager.persist(
            QuestionnaireQuestionEntity(
                questionnaireId = applicant.id!!,
                group = QuestionGroup.PERSONAL,
                sourceQuestionId = null,
                content = "질문2",
                sortOrder = 1,
            ),
        )
        questionId1 = question1.id!!
        questionId2 = question2.id!!

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `메모가 없으면 빈 목록을 반환한다`() {
        val result = interviewMemoRepositoryImpl.findAllByQuestionnaireQuestionIdIn(listOf(questionId1, questionId2))

        assertThat(result).isEmpty()
    }

    @Test
    fun `메모 목록을 전체 치환하면 이전 목록은 사라지고 새 목록만 남는다`() {
        interviewMemoRepositoryImpl.replaceAll(
            listOf(questionId1, questionId2),
            listOf(InterviewMemo(questionnaireQuestionId = questionId1, memo = "이전 메모")),
        )

        val replaced = interviewMemoRepositoryImpl.replaceAll(
            listOf(questionId1, questionId2),
            listOf(
                InterviewMemo(questionnaireQuestionId = questionId1, memo = "메모1"),
                InterviewMemo(questionnaireQuestionId = questionId2, memo = "메모2"),
            ),
        )

        val found = interviewMemoRepositoryImpl.findAllByQuestionnaireQuestionIdIn(listOf(questionId1, questionId2))
        assertThat(replaced).hasSize(2)
        assertThat(found).hasSize(2)
        assertThat(found.map { it.memo }).containsExactlyInAnyOrder("메모1", "메모2")
    }
}
