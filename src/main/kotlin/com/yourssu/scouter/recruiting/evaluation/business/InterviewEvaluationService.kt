package com.yourssu.scouter.recruiting.evaluation.business

import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.evaluation.business.dto.*
import com.yourssu.scouter.recruiting.evaluation.implement.*
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.implement.exception.InterviewEvaluationInvalidScoreException
import com.yourssu.scouter.recruiting.support.implement.exception.InterviewEvaluationItemNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class InterviewEvaluationService(
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val interviewEvaluationWriter: InterviewEvaluationWriter,
    private val interviewRubricReader: InterviewRubricReader,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
    private val evaluatorDirectory: EvaluatorDirectory,
) {

    fun readMy(applicantId: Long, evaluatorUserId: Long): InterviewEvaluationDto {
        val applicant = applicantReader.readById(applicantId)
        val semesterStr = "${applicant.applicationSemester.year.value}-${applicant.applicationSemester.term.intValue}"
        val rubric = interviewRubricReader.getByPartIdAndSemester(applicant.part.id!!, semesterStr)
        val rubricItems = rubric.items.associateBy { it.id }

        val evaluation = interviewEvaluationReader.readByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)
            ?: return InterviewEvaluationDto(
                totalScore = 0,
                items = rubric.items.map { item ->
                    InterviewEvaluationItemDto(
                        evaluationItemId = item.id!!,
                        keyword = item.keyword,
                        rubricType = item.rubricType,
                        maxScore = item.maxScore,
                        score = 0
                    )
                },
                overallComment = "",
                result = InterviewResult.PENDING,
                submittedAt = null
            )

        return toDto(evaluation, rubricItems)
    }

    @Transactional
    fun save(command: SaveInterviewEvaluationCommand) {
        val applicant = applicantReader.readById(command.applicantId)
        val semesterStr = "${applicant.applicationSemester.year.value}-${applicant.applicationSemester.term.intValue}"
        val rubric = interviewRubricReader.getByPartIdAndSemester(applicant.part.id!!, semesterStr)
        val rubricItems = rubric.items.associateBy { it.id }

        val items = command.items.map { item ->
            val rubricItem = rubricItems[item.evaluationItemId]
                ?: throw InterviewEvaluationItemNotFoundException("해당 루브릭에 존재하지 않는 항목입니다: ${item.evaluationItemId}")

            if (item.score > rubricItem.maxScore) {
                throw InterviewEvaluationInvalidScoreException("점수는 배점(${rubricItem.maxScore})을 초과할 수 없습니다.")
            }

            InterviewEvaluationScoreItem(
                evaluationItemId = item.evaluationItemId,
                score = item.score
            )
        }

        val existing = interviewEvaluationReader.readByApplicantIdAndEvaluatorUserId(
            command.applicantId,
            command.evaluatorUserId
        )

        val evaluation = InterviewEvaluation(
            id = existing?.id,
            applicantId = command.applicantId,
            evaluatorUserId = command.evaluatorUserId,
            items = items,
            overallComment = command.overallComment,
            result = command.result,
            submittedAt = if (command.submit) Instant.now() else existing?.submittedAt
        )

        interviewEvaluationWriter.write(evaluation)
    }

    fun readOthers(applicantId: Long, viewerUserId: Long): List<OtherInterviewEvaluationDto> {
        val evaluations = interviewEvaluationReader.readAllByApplicantId(applicantId)
            .filter { it.isSubmitted() && it.evaluatorUserId != viewerUserId }

        val viewerHasSubmitted = interviewEvaluationReader.readByApplicantIdAndEvaluatorUserId(
            applicantId,
            viewerUserId
        )?.isSubmitted() ?: false

        val evaluators = userReader.readAllByIds(evaluations.map { it.evaluatorUserId }).associateBy { it.id }

        return evaluations.map { evaluation ->
            val evaluator = evaluators[evaluation.evaluatorUserId]
            val comment = if (viewerHasSubmitted) evaluation.overallComment else ""
            OtherInterviewEvaluationDto(
                evaluatorId = evaluation.evaluatorUserId,
                evaluatorName = evaluator?.userInfo?.name ?: "",
                totalScore = evaluation.totalScore(),
                result = evaluation.result,
                overallComment = comment,
                items = evaluation.items.map { OtherInterviewEvaluationItemDto(it.evaluationItemId, it.score) }
            )
        }
    }

    fun readStatuses(applicantId: Long): List<EvaluatorStatusDto> {
        val applicant = applicantReader.readById(applicantId)
        val partId = applicant.part.id!!

        val evaluators = evaluatorDirectory.findEvaluatorsByPartId(partId)
        val usersByEmail = userReader.readAllByEmails(evaluators.map { it.email }).associateBy { it.userInfo.email }

        val evaluationsByEvaluator = interviewEvaluationReader.readAllByApplicantId(applicantId).associateBy { it.evaluatorUserId }

        return evaluators.mapNotNull { evaluator ->
            val user = usersByEmail[evaluator.email] ?: return@mapNotNull null
            val evaluation = evaluationsByEvaluator[user.id]
            val status = when {
                evaluation == null -> EvaluationStatus.NOT_STARTED
                evaluation.isSubmitted() -> EvaluationStatus.SUBMITTED
                else -> EvaluationStatus.IN_PROGRESS
            }

            EvaluatorStatusDto(
                userId = user.id!!,
                name = evaluator.name,
                status = status
            )
        }
    }

    private fun toDto(
        evaluation: InterviewEvaluation,
        rubricItems: Map<Long?, InterviewEvaluationItem>
    ): InterviewEvaluationDto {
        val items = evaluation.items.map { item ->
            val rubricItem = rubricItems[item.evaluationItemId]
            InterviewEvaluationItemDto(
                evaluationItemId = item.evaluationItemId,
                keyword = rubricItem?.keyword ?: "",
                rubricType = rubricItem?.rubricType ?: RubricGroupType.JOB,
                maxScore = rubricItem?.maxScore ?: 0,
                score = item.score
            )
        }

        return InterviewEvaluationDto(
            totalScore = evaluation.totalScore(),
            items = items,
            overallComment = evaluation.overallComment,
            result = evaluation.result,
            submittedAt = evaluation.submittedAt
        )
    }
}
