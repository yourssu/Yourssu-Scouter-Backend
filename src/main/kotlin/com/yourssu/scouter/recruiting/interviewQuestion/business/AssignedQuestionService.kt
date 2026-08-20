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
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
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
    private val evaluatorDirectory: EvaluatorDirectory,
    private val interviewRequirementLookup: InterviewRequirementLookup,
) {
    fun readByApplicantId(applicantId: Long): AssignedQuestionsDto {
        val applicant = applicantReader.readById(applicantId)
        val applicantPartId = applicant.part.id!!
        val applicantSemesterId = applicant.applicationSemester.id!!
        val requirementsById = readRequirementsById(applicant)

        val questions = assignedQuestionReader.readAllByApplicantId(applicantId)
        if (questions.isEmpty()) {
            return AssignedQuestionsDto(
                questions = readDefaultQuestions(applicantPartId, applicantSemesterId, requirementsById),
            )
        }

        val sourceQuestionsById = readSourceQuestionsById(questions)

        return AssignedQuestionsDto(
            questions = toAssignedQuestionDtos(questions, sourceQuestionsById, requirementsById),
        )
    }

    @Transactional
    fun upsert(
        applicantId: Long,
        command: SaveAssignedQuestionsCommand,
    ): AssignedQuestionsDto {
        val applicant = applicantReader.readById(applicantId)
        val applicantPartId = applicant.part.id!!
        val applicantSemesterId = applicant.applicationSemester.id!!

        val resolvedSourceQuestionIds = createNewPartQuestions(command.questions, applicantPartId, applicantSemesterId)

        val questions =
            command.questions.mapIndexed { index, question ->
                userReader.readById(question.assignedInterviewerUserId)

                AssignedQuestion(
                    assignedInterviewerUserId = question.assignedInterviewerUserId,
                    applicantId = applicantId,
                    sourceQuestionId = resolvedSourceQuestionIds[index] ?: question.sourceQuestionId,
                    content = if (question.category == AssignedQuestionCategory.PERSONAL) question.content else null,
                    category = question.category,
                    sortOrder = index,
                    isSelected = question.isSelected,
                    requirementIds =
                        if (question.category == AssignedQuestionCategory.PERSONAL) {
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

    /**
     * sourceQuestionId 없이 들어온 신규 PART 질문을 카탈로그 Question으로 먼저 저장하고,
     * 발급된 id를 AssignedQuestion 생성 전에 미리 매칭시켜준다. (PART는 카탈로그 카테고리라
     * AssignedQuestion 생성자가 sourceQuestionId를 필수로 요구하기 때문)
     * 반환값은 command 내 index -> 새로 저장된 Question id.
     */
    private fun createNewPartQuestions(
        questions: List<SaveAssignedQuestionCommand>,
        applicantPartId: Long,
        applicantSemesterId: Long,
    ): Map<Int, Long> {
        val newPartQuestions =
            questions.withIndex().filter { (_, question) ->
                question.category == AssignedQuestionCategory.PART && question.sourceQuestionId == null
            }
        if (newPartQuestions.isEmpty()) return emptyMap()

        val existingPartQuestionCount =
            questionReader.readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId).size

        return newPartQuestions.mapIndexed { offset, (index, question) ->
            val content =
                question.content
                    ?: throw QuestionInvalidException("신규 PART 질문은 content가 필요합니다.")

            val saved =
                questionWriter.save(
                    Question(
                        partId = applicantPartId,
                        semesterId = applicantSemesterId,
                        category = QuestionCategory.PART,
                        content = content,
                        sortOrder = existingPartQuestionCount + offset,
                        requirementIds = question.requirementIds,
                    ),
                )
            index to saved.id!!
        }.toMap()
    }

    private fun updateCatalogQuestions(
        questions: List<SaveAssignedQuestionCommand>,
        sourceQuestionsById: Map<Long?, Question>,
    ) {
        // 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)의 content와 요구조건은 인스턴스가 아닌 원본 질문(Question)에 매핑된다.
        // INTRO/OUTRO/CULTURE는 이 요청으로 값을 변경할 수 없고, PART만 여기서 갱신 가능하다.
        questions
            .filter { it.sourceQuestionId != null }
            .forEach { question ->
                val sourceQuestion = sourceQuestionsById.getValue(question.sourceQuestionId)
                when (sourceQuestion.category) {
                    QuestionCategory.INTRO, QuestionCategory.OUTRO, QuestionCategory.CULTURE -> {
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
                        val updatedQuestion =
                            Question(
                                id = sourceQuestion.id,
                                partId = sourceQuestion.partId,
                                semesterId = sourceQuestion.semesterId,
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

    private fun readSourceQuestionsById(questions: List<AssignedQuestion>): Map<Long?, Question> =
        questionReader
            .readAllByIdIn(questions.mapNotNull { it.sourceQuestionId })
            .associateBy { it.id }

    private fun readRequirementsById(applicant: Applicant): Map<Long?, InterviewRequirementProfile> =
        interviewRequirementLookup
            .findAllByPartIdAndSemester(applicant.part.id!!, applicant.applicationSemester)
            .associateBy { it.id }

    private fun toAssignedQuestionDtos(
        questions: List<AssignedQuestion>,
        sourceQuestionsById: Map<Long?, Question>,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
    ): List<AssignedQuestionDto> {
        val assignedInterviewerNamesById = readAssignedInterviewerNamesById(questions)

        return questions
            .sortedBy { it.sortOrder }
            .map { question ->
                question.toDto(sourceQuestionsById, requirementsById, assignedInterviewerNamesById)
            }
    }

    private fun readAssignedInterviewerNamesById(questions: List<AssignedQuestion>): Map<Long, String> {
        val usersById =
            userReader
                .readAllByIds(questions.map { it.assignedInterviewerUserId }.distinct())
                .associateBy { it.id!! }

        return questions
            .map { it.assignedInterviewerUserId }
            .distinct()
            .mapNotNull { userId ->
                val user = usersById[userId] ?: return@mapNotNull null
                val name =
                    evaluatorDirectory.findEvaluatorInfo(user.userInfo.email)?.nicknameEnglish
                        ?: user.userInfo.name
                userId to name
            }.toMap()
    }

    private fun readDefaultQuestions(
        applicantPartId: Long,
        applicantSemesterId: Long,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
    ): List<AssignedQuestionDto> {
        // INTRO / OUTRO : 학기에 무관하게 전체 공유
        val introOutroQuestions =
            questionReader
                .readAll()
                .filter { it.partId == null && it.category in setOf(QuestionCategory.INTRO, QuestionCategory.OUTRO) }

        // CULTURE / PART : 지원자의 학기에 귀속된 질문만 조회
        val cultureQuestions =
            questionReader
                .readAllBySemesterId(applicantSemesterId)
                .filter { it.partId == null && it.category == QuestionCategory.CULTURE }
        val partQuestions = questionReader.readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId)

        val catalogQuestions = introOutroQuestions + cultureQuestions + partQuestions

        return catalogQuestions
            .mapIndexed { index, question ->
                AssignedQuestionDto(
                    id = null,
                    assignedInterviewerUserId = null,
                    assignedInterviewerName = null,
                    sourceQuestionId = question.id,
                    content = question.content,
                    category = question.category.toAssignedQuestionCategory(),
                    sortOrder = index,
                    isSelected = if (question.category == QuestionCategory.CULTURE) false else null,
                    requirements =
                        question.requirementIds.mapNotNull { requirementId ->
                            requirementsById[requirementId]?.let { requirement ->
                                QuestionRequirementDto(requirement.id, requirement.content)
                            }
                        },
                )
            }
    }

    private fun QuestionCategory.toAssignedQuestionCategory(): AssignedQuestionCategory = AssignedQuestionCategory.valueOf(name)

    private fun AssignedQuestion.toDto(
        sourceQuestionsById: Map<Long?, Question>,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
        assignedInterviewerNamesById: Map<Long, String>,
    ): AssignedQuestionDto {
        // 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)는 요구조건을 원본 질문(Question)에서 가져오고, PERSONAL만 인스턴스 자체 값을 사용한다.
        val effectiveRequirementIds =
            if (category == AssignedQuestionCategory.PERSONAL) {
                requirementIds
            } else {
                sourceQuestionsById[sourceQuestionId]?.requirementIds.orEmpty()
            }

        return AssignedQuestionDto(
            id = id!!,
            assignedInterviewerUserId = assignedInterviewerUserId,
            assignedInterviewerName = assignedInterviewerNamesById[assignedInterviewerUserId],
            sourceQuestionId = sourceQuestionId,
            content = content ?: sourceQuestionsById[sourceQuestionId]?.content.orEmpty(),
            category = category,
            sortOrder = sortOrder,
            isSelected = isSelected,
            requirements =
                effectiveRequirementIds.mapNotNull { requirementId ->
                    requirementsById[requirementId]?.let { requirement ->
                        QuestionRequirementDto(requirement.id, requirement.content)
                    }
                },
        )
    }
}
