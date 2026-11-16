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
import com.yourssu.scouter.member.support.converter.NicknameConverter
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
            submittedAt = LocalDateTime.now()
        )

        finalEvaluationWriter.write(finalEvaluation)
    }

    fun readOthers(applicantId: Long, viewerUserId: Long): List<OtherInterviewEvaluationDto> {
        val finalEvaluations = finalEvaluationReader.readAllByApplicantId(applicantId)
            .filter { (it.submittedAt != null) && it.evaluatorUserId != viewerUserId }

        val viewerHasSubmitted = finalEvaluationReader.readByApplicantIdAndEvaluatorUserId(
            applicantId,
            viewerUserId
        )?.submittedAt ?: null

        val evaluators = finalEvaluations.map { finalEval ->
                    userReader.readById(finalEval.evaluatorUserId)
                }.associateBy { it.id }

        val evaluationsByEvaluator = interviewEvaluationReader.readAllByApplicantId(applicantId)
            .associateBy { it.evaluatorUserId }

        return finalEvaluations.map { finalEval ->
            val evaluator = evaluators[finalEval.evaluatorUserId]
            val evaluatorInfo = evaluator?.let { evaluatorDirectory.findEvaluatorInfo(it.userInfo.email) }
            val nickname = if (evaluatorInfo?.nicknameEnglish != null && evaluatorInfo.nicknameKorean != null) {
                NicknameConverter.combine(evaluatorInfo.nicknameEnglish, evaluatorInfo.nicknameKorean)
            } else ""

            val comment = if (viewerHasSubmitted != null) finalEval.overallComment else "" // 내 평가를 제출하지 않았다면 다른 사람의 총평을 볼수 없게 한다.
            val evaluation = evaluationsByEvaluator[finalEval.evaluatorUserId]

            OtherInterviewEvaluationDto(
                evaluatorId = finalEval.evaluatorUserId,
                evaluatorName = evaluator?.userInfo?.name ?: "",
                evaluatorNickname = nickname,
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
                finalEval.submittedAt != null  -> EvaluationStatus.SUBMITTED
                else -> EvaluationStatus.IN_PROGRESS
            }

            EvaluatorStatusDto(
                memberId = evaluator.memberId,
                userId = user?.id,
                name = evaluator.name,
                nickname = evaluator.nickname,
                status = status
            )
        }
    }

    /**
     * 어드민 전용 조회. 평가자 제외도, 제출 여부 필터도, 총평 마스킹도 하지 않는다.
     * 어드민 세션은 공용 비밀번호라 조회자 user id 자체가 없고, 임시저장 행도 루브릭을 잠그므로 삭제 대상으로 보여야 한다.
     * 평가자 목록은 두 테이블의 합집합이다. 점수 행이 모두 지워지고 final_evaluation 행만 남은 평가자가 있을 수 있다.
     */
    fun readAllForAdmin(applicantId: Long): List<AdminInterviewEvaluationDto> {
        val evaluations = interviewEvaluationReader.readAllByApplicantId(applicantId).associateBy { it.evaluatorUserId }
        val finalEvaluations = finalEvaluationReader.readAllByApplicantId(applicantId).associateBy { it.evaluatorUserId }

        val evaluatorUserIds = (evaluations.keys + finalEvaluations.keys).toList()
        if (evaluatorUserIds.isEmpty()) {
            return emptyList()
        }

        // 지원자의 현재 파트가 아니라, 평가 행이 실제로 가리키는 루브릭에서 항목 이름을 찾는다.
        // 평가 후 파트가 바뀌면 한 지원자의 평가가 서로 다른 루브릭을 가리킬 수 있다.
        val rubricItems = interviewEvaluationReader.readRubricIdsByApplicantId(applicantId)
            .mapNotNull(interviewRubricReader::findById)
            .flatMap(InterviewRubric::items)
            .associateBy { it.id }
        val evaluators = userReader.readAllByIds(evaluatorUserIds).associateBy { it.id }

        return evaluatorUserIds.map { evaluatorUserId ->
            val evaluation = evaluations[evaluatorUserId]
            val finalEvaluation = finalEvaluations[evaluatorUserId]
            val evaluator = evaluators[evaluatorUserId]

            AdminInterviewEvaluationDto(
                evaluatorUserId = evaluatorUserId,
                evaluatorName = evaluator?.userInfo?.name ?: "",
                evaluatorEmail = evaluator?.userInfo?.email ?: "",
                totalScore = evaluation?.totalScore() ?: finalEvaluation?.score ?: 0,
                result = finalEvaluation?.interviewResult,
                overallComment = finalEvaluation?.overallComment ?: "",
                submittedAt = finalEvaluation?.submittedAt,
                items = evaluation?.items.orEmpty().map { item ->
                    val rubricItem = rubricItems[item.evaluationItemId]
                    AdminInterviewEvaluationItemDto(
                        evaluationItemId = item.evaluationItemId,
                        keyword = rubricItem?.keyword ?: "",
                        rubricType = rubricItem?.rubricType,
                        maxScore = rubricItem?.maxScore ?: 0,
                        score = item.score,
                    )
                },
            )
        }
    }

    @Transactional
    fun deleteByEvaluator(applicantId: Long, evaluatorUserId: Long) {
        val affectedRubricIds = interviewEvaluationReader.readRubricIdsByApplicantId(applicantId)

        interviewEvaluationWriter.deleteByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)
        finalEvaluationWriter.deleteByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)

        unlockRubricsWithoutEvaluations(affectedRubricIds)
    }

    /**
     * 평가 행이 하나도 남지 않은 루브릭만 잠금을 푼다.
     * 판정 기준은 interview_evaluation -> interview_evaluation_item 의 루브릭이다.
     * final_evaluation.interview_rubric_id 는 평가 후 파트가 바뀌어도 갱신되지 않아 옛 루브릭을 가리킨 채 남는다.
     */
    private fun unlockRubricsWithoutEvaluations(rubricIds: List<Long>) {
        rubricIds
            .filterNot(interviewEvaluationReader::existsByInterviewRubricId)
            .mapNotNull(interviewRubricReader::findById)
            .filter { it.isLocked }
            .forEach { interviewRubricWriter.save(it.unlock()) }
    }

    @Transactional
    fun deleteAllByApplicantId(applicantId: Long) {
        interviewEvaluationWriter.deleteAllByApplicantId(applicantId)
        finalEvaluationWriter.deleteAllByApplicantId(applicantId)
    }

    /**
     * 지원자 삭제 경로용. 한 지원자의 면접 평가를 전부 지우고, 비게 된 루브릭의 잠금을 푼다.
     * 기존 deleteAllByApplicantId 는 잠금을 재계산하지 않으므로 호출자를 바꾸지 않고 따로 둔다.
     */
    @Transactional
    fun deleteAllByApplicantIdAndUnlockRubrics(applicantId: Long) {
        val affectedRubricIds = interviewEvaluationReader.readRubricIdsByApplicantId(applicantId)

        interviewEvaluationWriter.deleteAllByApplicantId(applicantId)
        finalEvaluationWriter.deleteAllByApplicantId(applicantId)

        unlockRubricsWithoutEvaluations(affectedRubricIds)
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


