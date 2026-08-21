package com.yourssu.scouter.recruiting.evaluation.business

import com.yourssu.scouter.recruiting.evaluation.business.dto.SaveDocumentEvaluationCommand

import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherDocumentEvaluationItemDto

import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherDocumentEvaluationDto

import com.yourssu.scouter.recruiting.evaluation.business.dto.EvaluatorStatusDto

import com.yourssu.scouter.recruiting.evaluation.business.dto.DocumentEvaluationItemDto

import com.yourssu.scouter.recruiting.evaluation.business.dto.DocumentEvaluationDto

import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationItem
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationReader
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationWriter
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult
import com.yourssu.scouter.recruiting.evaluation.implement.EvaluationStatus
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionReader
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentEvaluationInvalidScoreException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentSectionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DocumentEvaluationService(
    private val documentEvaluationReader: DocumentEvaluationReader,
    private val documentEvaluationWriter: DocumentEvaluationWriter,
    private val documentSectionReader: DocumentSectionReader,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
    private val evaluatorDirectory: EvaluatorDirectory,
) {

    fun readMy(applicantId: Long, evaluatorUserId: Long): DocumentEvaluationDto {
        val applicant = applicantReader.readById(applicantId)
        val sections = documentSectionReader.readAllByPartId(applicant.part.id!!).associateBy { it.id }
        val evaluation = documentEvaluationReader.readByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)
            ?: return DocumentEvaluationDto(
                totalScore = 0,
                items = emptyList(),
                overallComment = "",
                result = DocumentResult.PENDING,
                submittedAt = null,
            )

        return toDto(evaluation, sections)
    }

    @Transactional
    fun save(command: SaveDocumentEvaluationCommand) {
        val applicant: Applicant = applicantReader.readById(command.applicantId)
        val sections = documentSectionReader.readAllByPartId(applicant.part.id!!).associateBy { it.id }

        val items = command.items.map { item ->
            val section = sections[item.sectionId]
                ?: throw DocumentSectionNotFoundException("해당 파트에 존재하지 않는 문항입니다: ${item.sectionId}")
            if (item.score > section.maxScore) {
                throw DocumentEvaluationInvalidScoreException("점수는 배점(${section.maxScore})을 초과할 수 없습니다.")
            }

            DocumentEvaluationItem(sectionId = item.sectionId, score = item.score, memo = item.memo)
        }

        val existing = documentEvaluationReader
            .readByApplicantIdAndEvaluatorUserId(command.applicantId, command.evaluatorUserId)

        val evaluation = DocumentEvaluation(
            id = existing?.id,
            applicantId = command.applicantId,
            evaluatorUserId = command.evaluatorUserId,
            items = items,
            overallComment = command.overallComment,
            result = command.result,
            submittedAt = if (command.submit) Instant.now() else existing?.submittedAt,
        )

        documentEvaluationWriter.write(evaluation)
    }

    fun readOthers(applicantId: Long, viewerUserId: Long): List<OtherDocumentEvaluationDto> {
        val applicant = applicantReader.readById(applicantId)
        val evaluations = documentEvaluationReader.readAllByApplicantId(applicantId)
            .filter { it.isSubmitted() && it.evaluatorUserId != viewerUserId }

        val viewerHasSubmitted = documentEvaluationReader
            .readByApplicantIdAndEvaluatorUserId(applicantId, viewerUserId)?.isSubmitted() ?: false

        val evaluators = userReader.readAllByIds(evaluations.map { it.evaluatorUserId }).associateBy { it.id }

        return evaluations.map { evaluation ->
            val masked = evaluation.maskedFor(viewerHasSubmitted)
            val evaluator: User? = evaluators[evaluation.evaluatorUserId]
            OtherDocumentEvaluationDto(
                evaluatorId = evaluation.evaluatorUserId,
                evaluatorName = evaluator?.userInfo?.name ?: "",
                totalScore = evaluation.totalScore(),
                result = evaluation.result,
                overallComment = masked.overallComment,
                items = masked.items.map { OtherDocumentEvaluationItemDto(it.sectionId, it.score, it.memo) },
            )
        }
    }

    fun readStatuses(applicantId: Long): List<EvaluatorStatusDto> {
        val applicant = applicantReader.readById(applicantId)
        val partId = applicant.part.id!!

        val evaluators = evaluatorDirectory.findEvaluatorsByPartId(partId)
        val usersByEmail: Map<String, User> = userReader
            .readAllByEmails(evaluators.map { it.email })
            .associateBy { it.userInfo.email }

        val evaluationsByEvaluator = documentEvaluationReader.readAllByApplicantId(applicantId)
            .associateBy { it.evaluatorUserId }

        return evaluators.map { evaluator ->
            val user = usersByEmail[evaluator.email]
            val evaluation = user?.let { evaluationsByEvaluator[it.id] }
            val status = when {
                evaluation == null -> EvaluationStatus.NOT_STARTED
                evaluation.isSubmitted() -> EvaluationStatus.SUBMITTED
                else -> EvaluationStatus.IN_PROGRESS
            }

            EvaluatorStatusDto(userId = user?.id, name = evaluator.name, status = status)
        }
    }

    @Transactional
    fun deleteAllByApplicantId(applicantId: Long) {
        documentEvaluationWriter.deleteAllByApplicantId(applicantId)
    }

    private fun toDto(evaluation: DocumentEvaluation, sections: Map<Long?, DocumentSection>): DocumentEvaluationDto {
        val items = evaluation.items.map { item ->
            val section = sections[item.sectionId]
            DocumentEvaluationItemDto(
                sectionId = item.sectionId,
                question = section?.question ?: "",
                maxScore = section?.maxScore ?: 0,
                score = item.score,
                memo = item.memo,
            )
        }

        return DocumentEvaluationDto(
            totalScore = evaluation.totalScore(),
            items = items,
            overallComment = evaluation.overallComment,
            result = evaluation.result,
            submittedAt = evaluation.submittedAt,
        )
    }
}
