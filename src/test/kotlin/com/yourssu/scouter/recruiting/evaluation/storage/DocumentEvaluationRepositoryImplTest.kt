package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationItem
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.applicant.storage.JpaApplicantRepository
import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.masterdata.semester.implement.Term
import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.storage.JpaUserRepository
import com.yourssu.scouter.auth.user.storage.UserEntity
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

@DataJpaTest
@Import(DocumentEvaluationRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class DocumentEvaluationRepositoryImplTest {

    @Autowired
    lateinit var documentEvaluationRepositoryImpl: DocumentEvaluationRepositoryImpl

    @Autowired
    lateinit var jpaApplicantRepository: JpaApplicantRepository

    @Autowired
    lateinit var jpaUserRepository: JpaUserRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    private lateinit var savedApplicant: ApplicantEntity
    private lateinit var savedEvaluator: UserEntity
    private var sectionId1: Long = 0
    private var sectionId2: Long = 0

    @BeforeEach
    fun setUp() {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        val part = entityManager.persist(PartEntity(null, division, "PM", 1))
        val semester = entityManager.persist(SemesterEntity(null, Year.of(2025), Term.FALL))
        savedApplicant = entityManager.persist(
            ApplicantEntity(
                id = null,
                name = "오태민",
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
        savedEvaluator = entityManager.persist(
            UserEntity(
                id = null,
                name = "오리",
                email = "ori@example.com",
                profileImageUrl = "",
                authType = AuthType.OAUTH2,
                oauthId = "oauth-ori",
                oauth2Type = OAuth2Type.GOOGLE,
                tokenPrefix = "Bearer",
                accessToken = "dummy",
                refreshToken = "dummy",
                accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
            ),
        )
        val section1 = entityManager.persist(DocumentSectionEntity(null, part, "지원 동기", 60, "지원 동기"))
        val section2 = entityManager.persist(DocumentSectionEntity(null, part, "문제 해결 경험", 40, "문제 해결"))
        sectionId1 = section1.id!!
        sectionId2 = section2.id!!

        flushAndClear()
    }

    @Test
    fun `평가를 처음 저장하면 문항 점수와 함께 조회할 수 있다`() {
        // given
        val evaluation = DocumentEvaluation(
            applicantId = savedApplicant.id!!,
            evaluatorUserId = savedEvaluator.id!!,
            items = listOf(
                DocumentEvaluationItem(sectionId1, 50, "지원 동기가 분명합니다."),
                DocumentEvaluationItem(sectionId2, 30, "문제 해결 사례가 구체적입니다."),
            ),
            overallComment = "종합 의견",
            result = DocumentResult.PENDING,
            submittedAt = null,
        )

        // when
        val saved = documentEvaluationRepositoryImpl.save(evaluation)

        // then
        assertThat(saved.id).isNotNull
        assertThat(saved.items).hasSize(2)
        assertThat(saved.totalScore()).isEqualTo(80)
    }

    @Test
    fun `임시저장 후 다시 저장해도 유니크 제약 위반 없이 최신 값으로 갱신된다`() {
        // given: 임시저장 (submit=false에 해당)
        val tempSaved = documentEvaluationRepositoryImpl.save(
            DocumentEvaluation(
                applicantId = savedApplicant.id!!,
                evaluatorUserId = savedEvaluator.id!!,
                items = listOf(
                    DocumentEvaluationItem(sectionId1, 50, "지원 동기가 분명합니다."),
                    DocumentEvaluationItem(sectionId2, 30, "문제 해결 사례가 구체적입니다."),
                ),
                overallComment = "임시저장 코멘트",
                result = DocumentResult.PENDING,
                submittedAt = null,
            ),
        )

        // when: 같은 section들을 다시 참조하며 제출 완료로 재저장
        val submittedAt = Instant.now()
        assertThatCode {
            documentEvaluationRepositoryImpl.save(
                DocumentEvaluation(
                    id = tempSaved.id,
                    applicantId = savedApplicant.id!!,
                    evaluatorUserId = savedEvaluator.id!!,
                    items = listOf(
                        DocumentEvaluationItem(sectionId1, 55, "지원 동기가 분명하고 구체적입니다."),
                        DocumentEvaluationItem(sectionId2, 35, "문제 해결 사례가 좋습니다."),
                    ),
                    overallComment = "제출 완료 코멘트",
                    result = DocumentResult.DOCUMENT_PASS,
                    submittedAt = submittedAt,
                ),
            )
        }.doesNotThrowAnyException()

        // then
        val found = documentEvaluationRepositoryImpl.findByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, savedEvaluator.id!!)
        assertThat(found).isNotNull
        assertThat(found!!.items).hasSize(2)
        assertThat(found.totalScore()).isEqualTo(90)
        assertThat(found.overallComment).isEqualTo("제출 완료 코멘트")
        assertThat(found.result).isEqualTo(DocumentResult.DOCUMENT_PASS)
        assertThat(found.submittedAt).isNotNull
        assertThat(found.items.map { it.score }).containsExactlyInAnyOrder(55, 35)
    }

    @Test
    fun `existsBySectionIdIn은 해당 section을 참조하는 평가 항목이 있으면 true를 반환한다`() {
        // given
        documentEvaluationRepositoryImpl.save(
            DocumentEvaluation(
                applicantId = savedApplicant.id!!,
                evaluatorUserId = savedEvaluator.id!!,
                items = listOf(DocumentEvaluationItem(sectionId1, 50, "메모")),
                overallComment = "",
                result = DocumentResult.PENDING,
                submittedAt = null,
            ),
        )

        // when and then
        assertThat(documentEvaluationRepositoryImpl.existsBySectionIdIn(listOf(sectionId1))).isTrue
        assertThat(documentEvaluationRepositoryImpl.existsBySectionIdIn(listOf(sectionId2))).isFalse
    }

    @Test
    fun `deleteAllByApplicantId를 호출하면 해당 지원자의 평가와 문항이 모두 삭제된다`() {
        // given
        documentEvaluationRepositoryImpl.save(
            DocumentEvaluation(
                applicantId = savedApplicant.id!!,
                evaluatorUserId = savedEvaluator.id!!,
                items = listOf(
                    DocumentEvaluationItem(sectionId1, 50, "지원 동기가 분명합니다."),
                    DocumentEvaluationItem(sectionId2, 30, "문제 해결 사례가 구체적입니다."),
                ),
                overallComment = "종합 의견",
                result = DocumentResult.PENDING,
                submittedAt = null,
            ),
        )
        flushAndClear()

        // when
        documentEvaluationRepositoryImpl.deleteAllByApplicantId(savedApplicant.id!!)
        flushAndClear()

        // then
        assertThat(documentEvaluationRepositoryImpl.findAllByApplicantId(savedApplicant.id!!)).isEmpty()
        assertThat(
            documentEvaluationRepositoryImpl.findByApplicantIdAndEvaluatorUserId(savedApplicant.id!!, savedEvaluator.id!!),
        ).isNull()
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }
}
