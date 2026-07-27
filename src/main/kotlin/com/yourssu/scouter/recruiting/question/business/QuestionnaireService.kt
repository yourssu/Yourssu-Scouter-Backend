package com.yourssu.scouter.recruiting.question.business

import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.question.business.dto.QuestionnaireDto
import com.yourssu.scouter.recruiting.question.business.dto.QuestionnaireQuestionDto
import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireCommand
import com.yourssu.scouter.recruiting.question.implement.FixedQuestion
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionReader
import com.yourssu.scouter.recruiting.question.implement.Questionnaire
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestion
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireValidator
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireReader
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuestionnaireService(
    private val questionnaireReader: QuestionnaireReader,
    private val questionnaireWriter: QuestionnaireWriter,
    private val fixedQuestionReader: FixedQuestionReader,
    private val questionnaireValidator: QuestionnaireValidator,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
) {

    fun readByApplicantId(applicantId: Long): QuestionnaireDto {
        applicantReader.readById(applicantId)

        val questionnaire = questionnaireReader.readByApplicantId(applicantId)
        val questions = questionnaireReader.readQuestionsByApplicantId(applicantId)
        val fixedQuestionsById = readFixedQuestionsById(questions)

        return QuestionnaireDto(
            applicantId = applicantId,
            assignedInterviewerUserId = questionnaire?.assignedInterviewerUserId,
            questions = questions
                .sortedBy { it.sortOrder }
                .map { it.toDto(fixedQuestionsById) },
        )
    }

    @Transactional
    fun upsert(applicantId: Long, command: SaveQuestionnaireCommand): QuestionnaireDto {
        applicantReader.readById(applicantId)
        userReader.readById(command.assignedInterviewerUserId)

        val questions = command.questions.mapIndexed { index, question ->
            QuestionnaireQuestion(
                questionnaireId = applicantId,
                group = question.group,
                sourceQuestionId = question.sourceQuestionId,
                content = question.content,
                sortOrder = index,
            )
        }
        val fixedQuestionsById = readFixedQuestionsById(questions)
        questionnaireValidator.validate(questions, fixedQuestionsById)

        val saved = questionnaireWriter.upsert(
            Questionnaire(applicantId = applicantId, assignedInterviewerUserId = command.assignedInterviewerUserId),
            questions,
        )

        return QuestionnaireDto(
            applicantId = applicantId,
            assignedInterviewerUserId = command.assignedInterviewerUserId,
            questions = saved
                .sortedBy { it.sortOrder }
                .map { it.toDto(fixedQuestionsById) },
        )
    }

    private fun readFixedQuestionsById(questions: List<QuestionnaireQuestion>): Map<Long?, FixedQuestion> {
        return fixedQuestionReader
            .readAllByIdIn(questions.mapNotNull { it.sourceQuestionId })
            .associateBy { it.id }
    }

    private fun QuestionnaireQuestion.toDto(
        fixedQuestionsById: Map<Long?, FixedQuestion>,
    ): QuestionnaireQuestionDto = QuestionnaireQuestionDto(
        id = id!!,
        group = group,
        sourceQuestionId = sourceQuestionId,
        content = content ?: fixedQuestionsById[sourceQuestionId]?.content.orEmpty(),
        sortOrder = sortOrder,
    )
}
