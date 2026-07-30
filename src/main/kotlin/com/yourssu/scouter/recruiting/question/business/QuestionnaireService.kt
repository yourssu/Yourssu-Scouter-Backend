package com.yourssu.scouter.recruiting.question.business

import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementReader
import com.yourssu.scouter.recruiting.question.business.dto.QuestionRequirementDto
import com.yourssu.scouter.recruiting.question.business.dto.QuestionnaireDto
import com.yourssu.scouter.recruiting.question.business.dto.QuestionnaireQuestionDto
import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireCommand
import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireRequirementMappingCommand
import com.yourssu.scouter.recruiting.question.implement.FixedQuestion
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionReader
import com.yourssu.scouter.recruiting.question.implement.Questionnaire
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestion
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestionRequirement
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestionRequirementRepository
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
    private val questionnaireQuestionRequirementRepository: QuestionnaireQuestionRequirementRepository,
    private val partInterviewRequirementReader: PartInterviewRequirementReader,
) {

    fun readByApplicantId(applicantId: Long): QuestionnaireDto {
        val applicant = applicantReader.readById(applicantId)

        val questionnaire = questionnaireReader.readByApplicantId(applicantId)
        val questions = questionnaireReader.readQuestionsByApplicantId(applicantId)
        val fixedQuestionsById = readFixedQuestionsById(questions)

        val questionIds = questions.mapNotNull { it.id }
        val mappings = questionnaireQuestionRequirementRepository.findAllByQuestionQuestionIds(questionIds)
        val mappingsByQuestionId = mappings.groupBy { it.questionnaireQuestionId }

        val requirements = partInterviewRequirementReader.readAllByPartIdAndSemester(applicant.part.id!!, applicant.applicationSemester)
        val requirementsById = requirements.associateBy { it.id }

        return QuestionnaireDto(
            applicantId = applicantId,
            assignedInterviewerUserId = questionnaire?.assignedInterviewerUserId,
            questions = questions
                .sortedBy { it.sortOrder }
                .map { question ->
                    val mappedReqs = mappingsByQuestionId[question.id].orEmpty().mapNotNull { m ->
                        requirementsById[m.partInterviewRequirementId]?.let { req ->
                            QuestionRequirementDto(req.id!!, req.content)
                        }
                    }
                    question.toDto(fixedQuestionsById, mappedReqs)
                },
        )
    }

    @Transactional
    fun upsert(applicantId: Long, command: SaveQuestionnaireCommand): QuestionnaireDto {
        val applicant = applicantReader.readById(applicantId)
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

        val questionIds = saved.mapNotNull { it.id }
        val mappings = questionnaireQuestionRequirementRepository.findAllByQuestionQuestionIds(questionIds)
        val mappingsByQuestionId = mappings.groupBy { it.questionnaireQuestionId }

        val requirements = partInterviewRequirementReader.readAllByPartIdAndSemester(applicant.part.id!!, applicant.applicationSemester)
        val requirementsById = requirements.associateBy { it.id }

        return QuestionnaireDto(
            applicantId = applicantId,
            assignedInterviewerUserId = command.assignedInterviewerUserId,
            questions = saved
                .sortedBy { it.sortOrder }
                .map { question ->
                    val mappedReqs = mappingsByQuestionId[question.id].orEmpty().mapNotNull { m ->
                        requirementsById[m.partInterviewRequirementId]?.let { req ->
                            QuestionRequirementDto(req.id!!, req.content)
                        }
                    }
                    question.toDto(fixedQuestionsById, mappedReqs)
                },
        )
    }

    @Transactional
    fun saveMappings(applicantId: Long, command: SaveQuestionnaireRequirementMappingCommand): QuestionnaireDto {
        val applicant = applicantReader.readById(applicantId)

        val questions = questionnaireReader.readQuestionsByApplicantId(applicantId)
        val questionIds = questions.mapNotNull { it.id }.toSet()

        // Clean up existing mappings for this questionnaire's questions
        questionnaireQuestionRequirementRepository.deleteAllByQuestionQuestionIds(questionIds.toList())

        // Save new mappings
        val newMappings = command.toAllItems().flatMap { item ->
            item.questionIds
                .filter { questionIds.contains(it) } // Filter valid questionIds belonging to this applicant
                .map { questionId ->
                    QuestionnaireQuestionRequirement(
                        questionnaireQuestionId = questionId,
                        partInterviewRequirementId = item.requirementId,
                    )
                }
        }

        questionnaireQuestionRequirementRepository.saveAll(newMappings)

        return readByApplicantId(applicantId)
    }

    private fun readFixedQuestionsById(questions: List<QuestionnaireQuestion>): Map<Long?, FixedQuestion> {
        return fixedQuestionReader
            .readAllByIdIn(questions.mapNotNull { it.sourceQuestionId })
            .associateBy { it.id }
    }

    private fun QuestionnaireQuestion.toDto(
        fixedQuestionsById: Map<Long?, FixedQuestion>,
        requirements: List<QuestionRequirementDto>,
    ): QuestionnaireQuestionDto = QuestionnaireQuestionDto(
        id = id!!,
        group = group,
        sourceQuestionId = sourceQuestionId,
        content = content ?: fixedQuestionsById[sourceQuestionId]?.content.orEmpty(),
        sortOrder = sortOrder,
        requirements = requirements,
    )
}


