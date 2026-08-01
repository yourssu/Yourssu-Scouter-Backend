package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionDto
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionsDto
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.QuestionRequirementDto
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionsCommand
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionValidator
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionWriter
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementProfile
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssignedQuestionService(
    private val assignedQuestionReader: AssignedQuestionReader,
    private val assignedQuestionWriter: AssignedQuestionWriter,
    private val questionReader: QuestionReader,
    private val questionWriter: QuestionWriter,
    private val assignedQuestionValidator: AssignedQuestionValidator,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
    private val interviewRequirementLookup: InterviewRequirementLookup,
) {

    fun readByApplicantId(applicantId: Long): AssignedQuestionsDto {
        val applicant = applicantReader.readById(applicantId)
        val applicantPartId = applicant.part.id!!
        val requirementsById = readRequirementsById(applicant)

        val questions = assignedQuestionReader.readAllByApplicantId(applicantId)
        if (questions.isEmpty()) {
            return AssignedQuestionsDto(
                questions = readDefaultQuestions(applicantPartId, requirementsById),
            )
        }

        val sourceQuestionsById = readSourceQuestionsById(questions)

        return AssignedQuestionsDto(
            questions = toAssignedQuestionDtos(questions, sourceQuestionsById, requirementsById),
        )
    }

    @Transactional
    fun upsert(applicantId: Long, command: SaveAssignedQuestionsCommand): AssignedQuestionsDto {
        val applicant = applicantReader.readById(applicantId)
        val applicantPartId = applicant.part.id!!

        val questions = command.questions.mapIndexed { index, question ->
            userReader.readById(question.assignedInterviewerUserId)

            AssignedQuestion(
                assignedInterviewerUserId = question.assignedInterviewerUserId,
                applicantId = applicantId,
                sourceQuestionId = question.sourceQuestionId,
                content = if (question.category == AssignedQuestionCategory.PERSONAL) question.content else null,
                category = question.category,
                sortOrder = index,
                isSelected = question.isSelected,
                requirementIds = if (question.category == AssignedQuestionCategory.PERSONAL) {
                    question.requirementIds
                } else {
                    emptyList()
                },
            )
        }
        val sourceQuestionsById = readSourceQuestionsById(questions)
        assignedQuestionValidator.validate(questions, sourceQuestionsById, applicantPartId)

        updateCatalogQuestions(command.questions, sourceQuestionsById)

        val saved = assignedQuestionWriter.replaceAll(applicantId, questions)

        val savedSourceQuestionsById = readSourceQuestionsById(saved)
        val requirementsById = readRequirementsById(applicant)

        return AssignedQuestionsDto(
            questions = toAssignedQuestionDtos(saved, savedSourceQuestionsById, requirementsById),
        )
    }

    private fun updateCatalogQuestions(
        questions: List<SaveAssignedQuestionCommand>,
        sourceQuestionsById: Map<Long?, Question>,
    ) {
        // 카탈로그 카테고리(GLOBAL/CULTURE/PART)의 content와 요구조건은 인스턴스가 아닌 원본 질문(Question)에 매핑된다.
        // GLOBAL/CULTURE는 이 요청으로 값을 변경할 수 없고, PART만 여기서 갱신 가능하다.
        questions
            .filter { it.sourceQuestionId != null }
            .forEach { question ->
                val sourceQuestion = sourceQuestionsById.getValue(question.sourceQuestionId)
                when (sourceQuestion.category) {
                    QuestionCategory.GLOBAL, QuestionCategory.CULTURE -> {
                        if (question.requirementIds.isNotEmpty()) {
                            throw QuestionInvalidException(
                                "${sourceQuestion.category} 질문은 요구조건을 변경할 수 없습니다.",
                            )
                        }
                        if (question.content != null) {
                            throw QuestionInvalidException(
                                "${sourceQuestion.category} 질문은 content를 변경할 수 없습니다.",
                            )
                        }
                    }
                    QuestionCategory.PART -> {
                        val updatedQuestion = Question(
                            id = sourceQuestion.id,
                            partId = sourceQuestion.partId,
                            category = sourceQuestion.category,
                            content = question.content ?: sourceQuestion.content,
                            sortOrder = sourceQuestion.sortOrder,
                            requirementIds = question.requirementIds,
                        )
                        questionWriter.update(updatedQuestion)
                    }
                }
            }
    }

    private fun readSourceQuestionsById(questions: List<AssignedQuestion>): Map<Long?, Question> {
        return questionReader
            .readAllByIdIn(questions.mapNotNull { it.sourceQuestionId })
            .associateBy { it.id }
    }

    private fun readRequirementsById(applicant: Applicant): Map<Long?, InterviewRequirementProfile> {
        return interviewRequirementLookup
            .findAllByPartIdAndSemester(applicant.part.id!!, applicant.applicationSemester)
            .associateBy { it.id }
    }

    private fun toAssignedQuestionDtos(
        questions: List<AssignedQuestion>,
        sourceQuestionsById: Map<Long?, Question>,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
    ): List<AssignedQuestionDto> {
        return questions
            .sortedBy { it.sortOrder }
            .map { question ->
                question.toDto(sourceQuestionsById, requirementsById)
            }
    }

    private fun readDefaultQuestions(
        applicantPartId: Long,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
    ): List<AssignedQuestionDto> {
        val catalogQuestions = questionReader.readAll()
            .filter { it.partId == null || it.partId == applicantPartId }

        val fixedQuestions = catalogQuestions
            .filter { it.category == QuestionCategory.GLOBAL }

        val partQuestions = catalogQuestions
            .filter { it.category == QuestionCategory.PART }

        val cultureQuestions = catalogQuestions
            .filter { it.category == QuestionCategory.CULTURE }

        return (fixedQuestions + partQuestions + cultureQuestions)
            .mapIndexed { index, question ->
                AssignedQuestionDto(
                    id = null,
                    assignedInterviewerUserId = null,
                    sourceQuestionId = question.id,
                    content = question.content,
                    category = question.category.toAssignedQuestionCategory(),
                    sortOrder = index,
                    isSelected = if (question.category == QuestionCategory.CULTURE) false else null,
                    requirements = question.requirementIds.mapNotNull { requirementId ->
                        requirementsById[requirementId]?.let { requirement ->
                            QuestionRequirementDto(requirement.id, requirement.content)
                        }
                    },
                )
            }
    }

    private fun QuestionCategory.toAssignedQuestionCategory(): AssignedQuestionCategory {
        return AssignedQuestionCategory.valueOf(name)
    }

    private fun AssignedQuestion.toDto(
        sourceQuestionsById: Map<Long?, Question>,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
    ): AssignedQuestionDto {
        // 카탈로그 카테고리(GLOBAL/CULTURE/PART)는 요구조건을 원본 질문(Question)에서 가져오고, PERSONAL만 인스턴스 자체 값을 사용한다.
        val effectiveRequirementIds = if (category == AssignedQuestionCategory.PERSONAL) {
            requirementIds
        } else {
            sourceQuestionsById[sourceQuestionId]?.requirementIds.orEmpty()
        }

        return AssignedQuestionDto(
            id = id!!,
            assignedInterviewerUserId = assignedInterviewerUserId,
            sourceQuestionId = sourceQuestionId,
            content = content ?: sourceQuestionsById[sourceQuestionId]?.content.orEmpty(),
            category = category,
            sortOrder = sortOrder,
            isSelected = isSelected,
            requirements = effectiveRequirementIds.mapNotNull { requirementId ->
                requirementsById[requirementId]?.let { requirement ->
                    QuestionRequirementDto(requirement.id, requirement.content)
                }
            },
        )
    }
}
