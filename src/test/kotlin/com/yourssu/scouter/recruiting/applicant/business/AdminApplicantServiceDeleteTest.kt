package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.storage.UserEntity
import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.implement.Term
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantAnswerEntity
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantAvailableTimeEntity
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.comment.implement.CommentCategory
import com.yourssu.scouter.recruiting.comment.storage.DocumentCommentEntity
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import com.yourssu.scouter.recruiting.evaluation.storage.DocumentEvaluationEntity
import com.yourssu.scouter.recruiting.evaluation.storage.DocumentEvaluationItemEntity
import com.yourssu.scouter.recruiting.evaluation.storage.FinalEvaluationEntity
import com.yourssu.scouter.recruiting.evaluation.storage.InterviewEvaluationEntity
import com.yourssu.scouter.recruiting.evaluation.storage.InterviewEvaluationItemEntity
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.storage.AssignedQuestionEntity
import com.yourssu.scouter.recruiting.interviewQuestion.storage.AssignedQuestionRequirementEntity
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.rubric.storage.DocumentSectionEntity
import com.yourssu.scouter.recruiting.rubric.storage.InterviewRubricEntity
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleLocationType
import com.yourssu.scouter.recruiting.schedule.storage.ScheduleEntity
import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantNotFoundException
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.Year

/**
 * 지원자 삭제가 외래 키에 막히지 않고, 지원자를 참조하던 행을 하나도 남기지 않는지 확인한다.
 *
 * local(H2) 스키마는 엔티티 매핑으로 만들어져 document_evaluation, document_comment, assigned_question 에는 외래 키가 없다.
 * 그래서 "예외가 안 났다"는 증거가 되지 않는다. 테이블별 잔여 행 수를 직접 센다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Suppress("NonAsciiCharacters")
class AdminApplicantServiceDeleteTest {

    @Autowired
    lateinit var adminApplicantService: AdminApplicantService

    @Autowired
    lateinit var em: EntityManager

    private lateinit var part: PartEntity
    private lateinit var semester: SemesterEntity
    private lateinit var evaluator: UserEntity
    private lateinit var rubric: InterviewRubricEntity
    private lateinit var rubricItem: InterviewEvaluationItemEntity
    private var sectionId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = em.persistAndReturn(DivisionEntity(null, "검증디비전", 99))
        part = em.persistAndReturn(PartEntity(null, division, "검증파트", 99))
        // 학기는 SemesterInitializer 가 이미 넣어 둔 행을 쓴다. 같은 (year, term) 을 또 넣으면
        // InterviewRubricRepositoryImpl.save 의 findByYearAndTerm 이 두 건을 집어 터진다.
        semester = em.createQuery("SELECT s FROM SemesterEntity s WHERE s.year = :year AND s.term = :term", SemesterEntity::class.java)
            .setParameter("year", Year.of(2026))
            .setParameter("term", Term.FALL)
            .singleResult
        evaluator = em.persistAndReturn(
            UserEntity(
                id = null,
                name = "평가자",
                email = "evaluator@example.com",
                profileImageUrl = "",
                authType = AuthType.OAUTH2,
                oauthId = "oauth-evaluator",
                oauth2Type = OAuth2Type.GOOGLE,
                tokenPrefix = "Bearer",
                accessToken = "dummy",
                refreshToken = "dummy",
                accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
            ),
        )

        rubric = em.persistAndReturn(
            InterviewRubricEntity(
                semester = semester,
                deadline = LocalDateTime.of(2026, 10, 1, 0, 0),
                part = part,
                isLocked = true,
            ),
        )
        rubricItem = em.persistAndReturn(
            InterviewEvaluationItemEntity(
                interviewRubric = rubric,
                interviewRequirementId = null,
                keyword = "협업",
                rubricType = RubricGroupType.TEAM,
                maxScore = 20,
            ),
        )
        sectionId = em.persistAndReturn(DocumentSectionEntity(null, part, "지원 동기", 60, "지원 동기")).id!!

        em.flush()
        em.clear()
    }

    @Test
    fun `지원자를 지우면 그 지원자를 참조하던 행이 모두 사라진다`() {
        // given
        val applicantId = seedApplicantWithEverything("delete-me@example.com")

        assertThat(countFor("ApplicantAnswerEntity e WHERE e.applicant.id", applicantId)).isEqualTo(1)
        assertThat(countFor("ApplicantAvailableTimeEntity e WHERE e.applicant.id", applicantId)).isEqualTo(1)
        assertThat(countFor("InterviewEvaluationEntity e WHERE e.applicantId", applicantId)).isEqualTo(1)
        assertThat(countFor("FinalEvaluationEntity e WHERE e.applicant.id", applicantId)).isEqualTo(1)
        assertThat(countFor("DocumentEvaluationEntity e WHERE e.applicantId", applicantId)).isEqualTo(1)
        assertThat(countFor("DocumentCommentEntity e WHERE e.applicantId", applicantId)).isEqualTo(2)
        assertThat(countFor("ScheduleEntity e WHERE e.applicant.id", applicantId)).isEqualTo(1)
        assertThat(countFor("AssignedQuestionEntity e WHERE e.applicantId", applicantId)).isEqualTo(1)

        // when
        adminApplicantService.deleteWithRelatedData(applicantId)
        em.flush()
        em.clear()

        // then
        assertThat(countFor("ApplicantEntity e WHERE e.id", applicantId)).isZero
        assertThat(countFor("ApplicantAnswerEntity e WHERE e.applicant.id", applicantId)).isZero
        assertThat(countFor("ApplicantAvailableTimeEntity e WHERE e.applicant.id", applicantId)).isZero
        assertThat(countFor("InterviewEvaluationEntity e WHERE e.applicantId", applicantId)).isZero
        assertThat(countFor("FinalEvaluationEntity e WHERE e.applicant.id", applicantId)).isZero
        assertThat(countFor("DocumentEvaluationEntity e WHERE e.applicantId", applicantId)).isZero
        assertThat(countFor("DocumentCommentEntity e WHERE e.applicantId", applicantId)).isZero
        assertThat(countFor("ScheduleEntity e WHERE e.applicant.id", applicantId)).isZero
        assertThat(countFor("AssignedQuestionEntity e WHERE e.applicantId", applicantId)).isZero

        // 서류 평가 문항 점수와 배정 질문 요구조건 매핑은 부모가 사라지면 고아가 된다. 둘 다 0 이어야 한다.
        assertThat(count("SELECT COUNT(e) FROM DocumentEvaluationItemEntity e")).isZero
        assertThat(count("SELECT COUNT(e) FROM AssignedQuestionRequirementEntity e")).isZero
    }

    @Test
    fun `마지막 평가가 사라진 면접 루브릭은 잠금이 풀린다`() {
        // given
        val applicantId = seedApplicantWithEverything("unlock@example.com")
        assertThat(em.find(InterviewRubricEntity::class.java, rubric.id).isLocked).isTrue

        // when
        adminApplicantService.deleteWithRelatedData(applicantId)
        em.flush()
        em.clear()

        // then
        assertThat(em.find(InterviewRubricEntity::class.java, rubric.id).isLocked).isFalse
    }

    @Test
    fun `같은 루브릭에 다른 지원자의 평가가 남아 있으면 잠금은 유지된다`() {
        // given
        val deleted = seedApplicantWithEverything("deleted@example.com")
        val kept = seedApplicantWithEverything("kept@example.com")

        // when
        adminApplicantService.deleteWithRelatedData(deleted)
        em.flush()
        em.clear()

        // then
        assertThat(em.find(InterviewRubricEntity::class.java, rubric.id).isLocked).isTrue
        assertThat(countFor("InterviewEvaluationEntity e WHERE e.applicantId", kept)).isEqualTo(1)
    }

    @Test
    fun `참조 행이 하나도 없는 지원자도 지울 수 있다`() {
        // given
        val applicantId = em.persistAndReturn(applicantEntity("bare@example.com")).id!!
        em.flush()
        em.clear()

        // when, then
        assertThatCode {
            adminApplicantService.deleteWithRelatedData(applicantId)
            em.flush()
        }.doesNotThrowAnyException()
        assertThat(countFor("ApplicantEntity e WHERE e.id", applicantId)).isZero
    }

    @Test
    fun `없는 지원자를 지우려 하면 실패한다`() {
        assertThatThrownBy { adminApplicantService.deleteWithRelatedData(-1L) }
            .isInstanceOf(ApplicantNotFoundException::class.java)
    }

    private fun seedApplicantWithEverything(email: String): Long {
        val applicant = em.persistAndReturn(applicantEntity(email))
        val applicantId = applicant.id!!

        em.persist(ApplicantAvailableTimeEntity(null, applicant, Instant.parse("2026-10-05T01:00:00Z")))
        em.persist(ApplicantAnswerEntity(null, applicant, "지원 동기", "하고 싶어서", sectionId))

        em.persist(InterviewEvaluationEntity(applicantId = applicantId, interviewEvaluationItem = rubricItem, userId = evaluator.id!!, score = 15))
        em.persist(
            FinalEvaluationEntity(
                user = evaluator,
                interviewRubric = rubric,
                applicant = applicant,
                overallComment = "총평",
                interviewResult = InterviewResult.PENDING,
                score = 15,
                submittedAt = LocalDateTime.of(2026, 10, 2, 12, 0),
            ),
        )

        val documentEvaluation = em.persistAndReturn(
            DocumentEvaluationEntity(
                id = null,
                applicantId = applicantId,
                evaluatorUserId = evaluator.id!!,
                overallComment = "서류 총평",
                result = DocumentResult.PENDING,
                submittedAt = Instant.now(),
            ),
        )
        em.persist(DocumentEvaluationItemEntity(null, documentEvaluation, sectionId, 50, "메모"))

        // 답글이 부모를 ON DELETE CASCADE 로 참조한다. 부모와 답글을 함께 넣어 삭제 순서를 검사한다.
        val rootComment = em.persistAndReturn(
            DocumentCommentEntity(
                id = null,
                applicantId = applicantId,
                sectionId = sectionId,
                authorUserId = evaluator.id!!,
                content = "코멘트",
                parentCommentId = null,
                category = CommentCategory.DOCUMENT,
            ),
        )
        em.flush()
        em.persist(
            DocumentCommentEntity(
                id = null,
                applicantId = applicantId,
                sectionId = sectionId,
                authorUserId = evaluator.id!!,
                content = "답글",
                parentCommentId = rootComment.id,
                category = CommentCategory.DOCUMENT,
            ),
        )

        em.persist(
            ScheduleEntity(
                part = part,
                applicant = applicant,
                startTime = Instant.parse("2026-10-05T01:00:00Z"),
                endTime = Instant.parse("2026-10-05T01:30:00Z"),
                locationType = ScheduleLocationType.CLUB_ROOM,
            ),
        )

        val assignedQuestion = em.persistAndReturn(
            AssignedQuestionEntity(
                id = null,
                assignedMemberId = null,
                applicantId = applicantId,
                category = AssignedQuestionCategory.PERSONAL,
                sourceQuestionId = null,
                content = "질문",
                sortOrder = 1,
                isSelected = true,
            ),
        )
        em.flush()
        em.persist(AssignedQuestionRequirementEntity(assignedQuestion.id!!, 1L))

        em.flush()
        em.clear()

        return applicantId
    }

    private fun applicantEntity(email: String) = ApplicantEntity(
        id = null,
        name = "검증지원자",
        email = email,
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

    private fun countFor(fromAndWhere: String, applicantId: Long): Long =
        em.createQuery("SELECT COUNT(e) FROM $fromAndWhere = :id", java.lang.Long::class.java)
            .setParameter("id", applicantId)
            .singleResult
            .toLong()

    private fun count(jpql: String): Long =
        em.createQuery(jpql, java.lang.Long::class.java).singleResult.toLong()

    private fun <T : Any> EntityManager.persistAndReturn(entity: T): T {
        persist(entity)
        return entity
    }
}
