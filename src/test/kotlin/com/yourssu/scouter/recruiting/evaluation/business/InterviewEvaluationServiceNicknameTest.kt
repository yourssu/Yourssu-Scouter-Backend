package com.yourssu.scouter.recruiting.evaluation.business

import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.evaluation.implement.*
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.business.EvaluatorInfo
import com.yourssu.scouter.recruiting.support.business.EvaluatorSummary
import com.yourssu.scouter.masterdata.part.implement.Part
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class InterviewEvaluationServiceNicknameTest {

    private val interviewEvaluationReader: InterviewEvaluationReader = mock(InterviewEvaluationReader::class.java)
    private val interviewEvaluationWriter: InterviewEvaluationWriter = mock(InterviewEvaluationWriter::class.java)
    private val finalEvaluationReader: FinalEvaluationReader = mock(FinalEvaluationReader::class.java)
    private val finalEvaluationWriter: FinalEvaluationWriter = mock(FinalEvaluationWriter::class.java)
    private val interviewRubricReader: InterviewRubricReader = mock(InterviewRubricReader::class.java)
    private val interviewRubricWriter: InterviewRubricWriter = mock(InterviewRubricWriter::class.java)
    private val applicantReader: ApplicantReader = mock(ApplicantReader::class.java)
    private val userReader: UserReader = mock(UserReader::class.java)
    private val evaluatorDirectory: EvaluatorDirectory = mock(EvaluatorDirectory::class.java)

    private lateinit var service: InterviewEvaluationService

    @BeforeEach
    fun setUp() {
        service = InterviewEvaluationService(
            interviewEvaluationReader,
            interviewEvaluationWriter,
            finalEvaluationReader,
            finalEvaluationWriter,
            interviewRubricReader,
            interviewRubricWriter,
            applicantReader,
            userReader,
            evaluatorDirectory
        )
    }

    @Test
    @DisplayName("readStatuses 호출 시 EvaluatorStatusDto에 nickname이 정상 포함되어야 한다")
    fun readStatuses_includesNickname() {
        val applicantId = 1L
        val partId = 10L
        val part = mock(Part::class.java)
        given(part.id).willReturn(partId)

        val applicant = mock(Applicant::class.java)
        given(applicant.part).willReturn(part)
        given(applicantReader.readById(applicantId)).willReturn(applicant)

        val summary = EvaluatorSummary(
            email = "test@yourssu.com",
            name = "홍길동",
            nickname = "gildong(길동)",
            memberId = 100L
        )
        given(evaluatorDirectory.findEvaluatorsByPartId(partId)).willReturn(listOf(summary))

        val userInfo = mock(UserInfo::class.java)
        given(userInfo.email).willReturn("test@yourssu.com")
        given(userInfo.name).willReturn("홍길동")

        val user = mock(User::class.java)
        given(user.id).willReturn(200L)
        given(user.userInfo).willReturn(userInfo)

        given(userReader.readAllByEmails(listOf("test@yourssu.com"))).willReturn(listOf(user))
        given(finalEvaluationReader.readAllByApplicantId(applicantId)).willReturn(emptyList())

        val statuses = service.readStatuses(applicantId)

        assertThat(statuses).hasSize(1)
        assertThat(statuses[0].name).isEqualTo("홍길동")
        assertThat(statuses[0].nickname).isEqualTo("gildong(길동)")
    }

    @Test
    @DisplayName("readOthers 호출 시 OtherInterviewEvaluationDto에 evaluatorNickname이 정상 포함되어야 한다")
    fun readOthers_includesEvaluatorNickname() {
        val applicantId = 1L
        val viewerUserId = 999L
        val evaluatorUserId = 200L

        val finalEval = mock(FinalEvaluation::class.java)
        given(finalEval.evaluatorUserId).willReturn(evaluatorUserId)
        given(finalEval.submit).willReturn(true)
        given(finalEval.score).willReturn(90)
        given(finalEval.interviewResult).willReturn(InterviewResult.FINAL_PASS)
        given(finalEval.overallComment).willReturn("Good")

        given(finalEvaluationReader.readAllByApplicantId(applicantId)).willReturn(listOf(finalEval))
        given(finalEvaluationReader.readByApplicantIdAndEvaluatorUserId(applicantId, viewerUserId)).willReturn(null)

        val evaluatorInfoObj = mock(UserInfo::class.java)
        given(evaluatorInfoObj.email).willReturn("evaluator@yourssu.com")
        given(evaluatorInfoObj.name).willReturn("홍길동")

        val user = mock(User::class.java)
        given(user.id).willReturn(evaluatorUserId)
        given(user.userInfo).willReturn(evaluatorInfoObj)

        given(userReader.readById(evaluatorUserId)).willReturn(user)
        given(evaluatorDirectory.findEvaluatorInfo("evaluator@yourssu.com")).willReturn(
            EvaluatorInfo(memberId = 100L, nicknameEnglish = "gildong", nicknameKorean = "길동", partName = "Backend")
        )

        given(interviewEvaluationReader.readAllByApplicantId(applicantId)).willReturn(emptyList())

        val others = service.readOthers(applicantId, viewerUserId)

        assertThat(others).hasSize(1)
        assertThat(others[0].evaluatorName).isEqualTo("홍길동")
        assertThat(others[0].evaluatorNickname).isEqualTo("gildong(길동)")
    }
}
