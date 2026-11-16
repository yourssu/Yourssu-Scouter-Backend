package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.storage.UserEntity
import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.implement.Term
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationItem
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult
import com.yourssu.scouter.recruiting.rubric.storage.DocumentSectionEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.Year

/**
 * 어드민 평가 삭제가 평가자 한 명만 지우는지 확인한다.
 * 서류 평가는 document_section 행을 만드는 사용자 경로가 없어 실행 중인 앱으로는 이 경로에 도달할 수 없다.
 */
@DataJpaTest
@Import(DocumentEvaluationRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class DocumentEvaluationRepositoryImplDeleteByEvaluatorTest {

    @Autowired
    lateinit var documentEvaluationRepositoryImpl: DocumentEvaluationRepositoryImpl

    @Autowired
    lateinit var jpaDocumentEvaluationItemRepository: JpaDocumentEvaluationItemRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    private lateinit var savedApplicant: ApplicantEntity
    private lateinit var evaluatorA: UserEntity
    private lateinit var evaluatorB: UserEntity
    private var sectionId1: Long = 0
    private var sectionId2: Long = 0

    @BeforeEach
    fun setUp() {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        val part = entityManager.persist(PartEntity(null, division, "PM", 1))
        val semester = entityManager.persist(SemesterEntity(null, Year.of(2026), Term.FALL))
        savedApplicant = entityManager.persist(
            ApplicantEntity(
                id = null,
                name = "검증지원자",
                email = "applicant@example.com",
                phoneNumber = "010-0000-0000",
                age = "23",
                department = "컴퓨터학부",
                studentId = "202601001",
                part = part,
                state = ApplicantState.UNDER_REVIEW,
                applicationDateTime = Instant.parse("2026-09-01T00:00:00Z"),
                applicationSemester = semester,
                academicSemester = "3-1",
            ),
        )
        evaluatorA = persistUser("평가자에이", "eval-a@example.com", "oauth-a")
        evaluatorB = persistUser("평가자비", "eval-b@example.com", "oauth-b")

        sectionId1 = entityManager.persist(DocumentSectionEntity(null, part, "지원 동기", 60, "지원 동기")).id!!
        sectionId2 = entityManager.persist(DocumentSectionEntity(null, part, "문제 해결 경험", 40, "문제 해결")).id!!

        flushAndClear()
    }

    @Test
    fun `한 평가자만 지우고 다른 평가자의 평가와 문항 점수는 남긴다`() {
        // given
        documentEvaluationRepositoryImpl.save(evaluationOf(evaluatorA.id!!, 50, 30, "A 총평", Instant.now()))
        documentEvaluationRepositoryImpl.save(evaluationOf(evaluatorB.id!!, 40, 20, "B 총평", Instant.now()))
        flushAndClear()

        // when
        documentEvaluationRepositoryImpl.deleteByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, evaluatorA.id!!)
        flushAndClear()

        // then
        assertThat(
            documentEvaluationRepositoryImpl.findByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, evaluatorA.id!!),
        ).isNull()

        val remaining = documentEvaluationRepositoryImpl.findAllByApplicantId(savedApplicant.id!!)
        assertThat(remaining).hasSize(1)
        assertThat(remaining.single().evaluatorUserId).isEqualTo(evaluatorB.id)
        assertThat(remaining.single().items).hasSize(2)
        assertThat(remaining.single().totalScore()).isEqualTo(60)
        assertThat(remaining.single().overallComment).isEqualTo("B 총평")
    }

    @Test
    fun `삭제한 평가자의 문항 점수 행도 함께 사라진다`() {
        // given
        documentEvaluationRepositoryImpl.save(evaluationOf(evaluatorA.id!!, 50, 30, "A 총평", Instant.now()))
        documentEvaluationRepositoryImpl.save(evaluationOf(evaluatorB.id!!, 40, 20, "B 총평", Instant.now()))
        flushAndClear()

        // when
        documentEvaluationRepositoryImpl.deleteByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, evaluatorA.id!!)
        flushAndClear()

        // then
        assertThat(jpaDocumentEvaluationItemRepository.findAll()).hasSize(2)
        assertThat(documentEvaluationRepositoryImpl.existsBySectionIdIn(listOf(sectionId1, sectionId2))).isTrue
    }

    @Test
    fun `임시저장 상태의 평가도 평가자 단위로 지울 수 있다`() {
        // given: submittedAt 이 null 인 임시저장 행
        documentEvaluationRepositoryImpl.save(evaluationOf(evaluatorA.id!!, 10, 10, "임시저장 총평", null))
        flushAndClear()

        val draft = documentEvaluationRepositoryImpl
            .findByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, evaluatorA.id!!)
        assertThat(draft).isNotNull
        assertThat(draft!!.isSubmitted()).isFalse
        assertThat(draft.overallComment).isEqualTo("임시저장 총평")

        // when
        documentEvaluationRepositoryImpl.deleteByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, evaluatorA.id!!)
        flushAndClear()

        // then
        assertThat(documentEvaluationRepositoryImpl.findAllByApplicantId(savedApplicant.id!!)).isEmpty()
        assertThat(documentEvaluationRepositoryImpl.existsBySectionIdIn(listOf(sectionId1, sectionId2))).isFalse
    }

    @Test
    fun `지울 평가가 없으면 아무 일도 일어나지 않는다`() {
        assertThatCode {
            documentEvaluationRepositoryImpl.deleteByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, evaluatorA.id!!)
        }.doesNotThrowAnyException()
    }

    private fun evaluationOf(
        evaluatorUserId: Long,
        score1: Int,
        score2: Int,
        overallComment: String,
        submittedAt: Instant?,
    ) = DocumentEvaluation(
        applicantId = savedApplicant.id!!,
        evaluatorUserId = evaluatorUserId,
        items = listOf(
            DocumentEvaluationItem(sectionId1, score1, "메모1"),
            DocumentEvaluationItem(sectionId2, score2, "메모2"),
        ),
        overallComment = overallComment,
        result = DocumentResult.PENDING,
        submittedAt = submittedAt,
    )

    private fun persistUser(name: String, email: String, oauthId: String): UserEntity = entityManager.persist(
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

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }
}
