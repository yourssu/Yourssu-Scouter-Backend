package com.yourssu.scouter.recruiting.evaluation.business

import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.evaluation.business.dto.*
import com.yourssu.scouter.recruiting.evaluation.implement.*
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.implement.exception.InterviewEvaluationInvalidScoreException
import com.yourssu.scouter.recruiting.support.implement.exception.InterviewEvaluationItemNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class InterviewEvaluationService(
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val interviewEvaluationWriter: InterviewEvaluationWriter,
    private val finalEvaluationReader: FinalEvaluationReader,
    private val finalEvaluationWriter: FinalEvaluationWriter,
    private val interviewRubricReader: InterviewRubricReader,
    private val interviewRubricWriter: InterviewRubricWriter,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
    private val evaluatorDirectory: EvaluatorDirectory,
) {

    fun readMy(applicantId: Long, evaluatorUserId: Long): InterviewEvaluationDto {
        val applicant = applicantReader.readById(applicantId)
        val rubric = interviewRubricReader.getByPartIdAndSemester(applicant.part.id!!, applicant.applicationSemester)
        val rubricItems = rubric.items.associateBy { it.id }

        val evaluation = interviewEvaluationReader.readByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)
        val finalEval = finalEvaluationReader.readByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)

        if (evaluation == null) {
            return InterviewEvaluationDto(
                totalScore = 0,
                items =
                    rubric.items.map { item ->
                      InterviewEvaluationItemDto(
                          evaluationItemId = item.id!!,
                          keyword = item.keyword,
                          rubricType = item.rubricType,
                          maxScore = item.maxScore,
                          score = 0,
                      )
                  },
                overallComment = finalEval?.overallComment ?: "",
                result = finalEval?.interviewResult,
                submittedAt = finalEval?.submittedAt,
            )
        }

        return toDto(evaluation, finalEval, rubricItems)
    }

    @Transactional
    fun save(command: SaveInterviewEvaluationCommand) {
        val applicant = applicantReader.readById(command.applicantId)
        val rubric = interviewRubricReader.getByPartIdAndSemester(applicant.part.id!!, applicant.applicationSemester)
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

        val evaluation = InterviewEvaluation(
            id = null,
            applicantId = command.applicantId,
            evaluatorUserId = command.evaluatorUserId,
            items = items
        )

        val savedEvaluation = interviewEvaluationWriter.write(evaluation)

        if (!rubric.isLocked) {
            interviewRubricWriter.save(rubric.lock())
        }

        val existingFinal = finalEvaluationReader.readByApplicantIdAndEvaluatorUserId(
            command.applicantId,
            command.evaluatorUserId
        )

        val finalEvaluation = FinalEvaluation(
            id = existingFinal?.id,
            evaluatorUserId = command.evaluatorUserId,
            interviewRubricId = rubric.id!!,
            applicantId = command.applicantId,
            overallComment = command.overallComment,
            interviewResult = command.result,
            score = savedEvaluation.totalScore(),
            submit = command.submit,
            submittedAt = if (command.submit) LocalDateTime.now() else existingFinal?.submittedAt
        )

        finalEvaluationWriter.write(finalEvaluation)
    }

    fun readOthers(applicantId: Long, viewerUserId: Long): List<OtherInterviewEvaluationDto> {
        val finalEvaluations = finalEvaluationReader.readAllByApplicantId(applicantId)
            .filter { it.submit && it.evaluatorUserId != viewerUserId }

        val viewerHasSubmitted = finalEvaluationReader.readByApplicantIdAndEvaluatorUserId(
            applicantId,
            viewerUserId
        )?.submit ?: false

        val evaluators = finalEvaluations.map { finalEval ->
                    userReader.readById(finalEval.evaluatorUserId)
                }.associateBy { it.id }

        val evaluationsByEvaluator = interviewEvaluationReader.readAllByApplicantId(applicantId)
            .associateBy { it.evaluatorUserId }

        return finalEvaluations.map { finalEval ->
            val evaluator = evaluators[finalEval.evaluatorUserId]
            val comment = if (viewerHasSubmitted) finalEval.overallComment else "" // 내 평가를 제출하지 않았다면 다른 사람의 총평을 볼수 없게 한다.
            val evaluation = evaluationsByEvaluator[finalEval.evaluatorUserId]

            OtherInterviewEvaluationDto(
                evaluatorId = finalEval.evaluatorUserId,
                evaluatorName = evaluator?.userInfo?.name ?: "",
                totalScore = finalEval.score,
                result = finalEval.interviewResult,
                overallComment = comment,
                items = evaluation?.items?.map { OtherInterviewEvaluationItemDto(it.evaluationItemId, it.score) } ?: emptyList()
            )
        }
    }

    fun readStatuses(applicantId: Long): List<EvaluatorStatusDto> {
        val applicant = applicantReader.readById(applicantId)
        val partId = applicant.part.id!!

        val evaluators = evaluatorDirectory.findEvaluatorsByPartId(partId)
        val usersByEmail = userReader.readAllByEmails(evaluators.map { it.email }).associateBy { it.userInfo.email }

        val finalEvaluationsByEvaluator = finalEvaluationReader.readAllByApplicantId(applicantId).associateBy { it.evaluatorUserId }

        return evaluators.map { evaluator ->
            val user = usersByEmail[evaluator.email]
            val finalEval = user?.let { finalEvaluationsByEvaluator[it.id] }
            val status = when {
                finalEval == null -> EvaluationStatus.NOT_STARTED
                finalEval.submit -> EvaluationStatus.SUBMITTED
                else -> EvaluationStatus.IN_PROGRESS
            }

            EvaluatorStatusDto(
                memberId = evaluator.memberId,
                userId = user?.id,
                name = evaluator.name,
                status = status
            )
        }
    }

    @Transactional
    fun deleteAllByApplicantId(applicantId: Long) {
        interviewEvaluationWriter.deleteAllByApplicantId(applicantId)
        finalEvaluationWriter.deleteAllByApplicantId(applicantId)
    }

    private fun toDto(
        evaluation: InterviewEvaluation,
        finalEvaluation: FinalEvaluation?,
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
            overallComment = finalEvaluation?.overallComment ?: "",
            result = finalEvaluation?.interviewResult ?: InterviewResult.PENDING,
            submittedAt = finalEvaluation?.submittedAt
        )
    }
}


