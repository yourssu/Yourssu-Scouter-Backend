package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
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
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionApplier
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartLockPolicy
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartQuestionAssignmentPolicy
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCatalogApplier
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementProfile
import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionLockedException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssignedQuestionService(
    private val assignedQuestionReader: AssignedQuestionReader,
    private val assignedQuestionWriter: AssignedQuestionWriter,
    private val questionReader: QuestionReader,
    private val assignedQuestionValidator: AssignedQuestionValidator,
    private val applicantReader: ApplicantReader,
    private val memberReader: MemberReader,
    private val interviewRequirementLookup: InterviewRequirementLookup,
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val partCultureSelectionReader: PartCultureSelectionReader,
    private val partLockPolicy : PartLockPolicy,
    private val questionCatalogApplier: QuestionCatalogApplier,
    private val partCultureSelectionApplier: PartCultureSelectionApplier,
    private val partQuestionAssignmentPolicy: PartQuestionAssignmentPolicy
) {
    fun readByApplicantId(applicantId: Long): AssignedQuestionsDto {
        val applicant = applicantReader.readById(applicantId)
        val applicantPartId = applicant.part.id!!
        val applicantSemesterId = applicant.applicationSemester.id!!
        val requirementsById = readRequirementsById(applicant)
        val selectedCultureQuestionIds = partCultureSelectionReader.readSelectedQuestionIds(applicantPartId, applicantSemesterId)

        val questions = assignedQuestionReader.readAllByApplicantId(applicantId)
        if (questions.isEmpty()) {
            return AssignedQuestionsDto(
                questions = readDefaultQuestions(applicantPartId, applicantSemesterId, requirementsById, selectedCultureQuestionIds),
            )
        }

        val sourceQuestionsById = readSourceQuestionsById(questions)

        return AssignedQuestionsDto(
            questions = toAssignedQuestionDtos(
                questions,
                sourceQuestionsById,
                requirementsById,
                selectedCultureQuestionIds,
                applicantPartId,
                applicantSemesterId,
            ),
        )
    }

    @Transactional
    fun upsert(
        applicantId: Long,
        command: SaveAssignedQuestionsCommand,
    ): AssignedQuestionsDto {
        if (interviewEvaluationReader.existsByApplicantId(applicantId)) {
            throw AssignedQuestionLockedException("해당 지원자의 면접 평가가 존재해 질문지를 수정할 수 없습니다. applicantId=$applicantId")
        }

        val applicant = applicantReader.readById(applicantId)
        val applicantPartId = applicant.part.id!!
        val applicantSemesterId = applicant.applicationSemester.id!!
        
        val partLocked = partLockPolicy.isPartLocked(applicantPartId, applicantSemesterId)

        val resolvedSourceQuestionIds =
            questionCatalogApplier.createNewPartQuestions(command.questions, applicantPartId, applicantSemesterId, partLocked)

        val selectedCultureQuestionIds =
            partCultureSelectionApplier.updateSelection(command.questions, applicantPartId, applicantSemesterId, partLocked)

        val questions = partQuestionAssignmentPolicy.resolve(command.questions, partLocked, applicantId,resolvedSourceQuestionIds, selectedCultureQuestionIds)


        val sourceQuestionsById = questionReader.readAllByIdIn(questions.mapNotNull { it.sourceQuestionId }).associateBy { it.id }
        assignedQuestionValidator.validate(questions, sourceQuestionsById, applicantPartId)

        questionCatalogApplier.applyCatalogUpdatesAndDeletions(
            command.questions, questions, sourceQuestionsById, applicantPartId, applicantSemesterId, partLocked,
        )

        val saved = assignedQuestionWriter.replaceAll(applicantId, questions.map { it.withoutCultureSelection() })

        val savedSourceQuestionsById = readSourceQuestionsById(saved)
        val requirementsById = readRequirementsById(applicant)

        return AssignedQuestionsDto(
            questions = toAssignedQuestionDtos(
                saved,
                savedSourceQuestionsById,
                requirementsById,
                selectedCultureQuestionIds,
                applicantPartId,
                applicantSemesterId,
            ),
        )
    }


    private fun AssignedQuestion.withoutCultureSelection(): AssignedQuestion =
        if (category == AssignedQuestionCategory.CULTURE && isSelected != null) {
            AssignedQuestion(
                id = id,
                assignedMemberId = assignedMemberId,
                applicantId = applicantId,
                sourceQuestionId = sourceQuestionId,
                content = content,
                category = category,
                sortOrder = sortOrder,
                isSelected = null,
                requirementIds = requirementIds,
            )
        } else {
            this
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
        selectedCultureQuestionIds: Set<Long>,
        applicantPartId: Long,
        applicantSemesterId: Long,
    ): List<AssignedQuestionDto> {
        val assignedMemberNamesById = readAssignedMemberNamesById(questions)

        val savedDtos = questions
            .sortedBy { it.sortOrder }
            .map { question ->
                question.toDto(sourceQuestionsById, requirementsById, assignedMemberNamesById, selectedCultureQuestionIds)
            }

        val unsavedCatalogDtos = readUnsavedCatalogQuestions(
            applicantPartId,
            applicantSemesterId,
            savedDtos,
            requirementsById,
            selectedCultureQuestionIds,
        )

        return savedDtos + unsavedCatalogDtos
    }

    /**
     * CULTURE/PART는 개별 지원자가 저장한 적 없어도(=한 번도 선택/배정될 필요가 없었던 문항이라
     * assignedMemberId를 정할 필요가 없었던 경우) 그 파트+학기의 카탈로그 문항 전체가 보여야 한다.
     * 아직 이 지원자에게 인스턴스로 저장되지 않은 문항을 조회 시 카탈로그에서 보충해서 채워준다.
     * (다른 지원자의 저장으로 새로 생긴 PART/CULTURE 카탈로그 문항도 이 방식으로 즉시 반영된다.)
     */
    private fun readUnsavedCatalogQuestions(
        applicantPartId: Long,
        applicantSemesterId: Long,
        savedDtos: List<AssignedQuestionDto>,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
        selectedCultureQuestionIds: Set<Long>,
    ): List<AssignedQuestionDto> {
        val savedSourceIdsByCategory = savedDtos
            .filter { it.category == AssignedQuestionCategory.CULTURE || it.category == AssignedQuestionCategory.PART }
            .groupBy({ it.category }) { it.sourceQuestionId }
            .mapValues { (_, ids) -> ids.toSet() }
        val savedCultureSourceIds = savedSourceIdsByCategory[AssignedQuestionCategory.CULTURE].orEmpty()
        val savedPartSourceIds = savedSourceIdsByCategory[AssignedQuestionCategory.PART].orEmpty()

        val unsavedCultureQuestions = questionReader
            .readAllBySemesterId(applicantSemesterId)
            .filter {
                it.partId == null && it.category == QuestionCategory.CULTURE && (it.id == null || it.id !in savedCultureSourceIds)
            }
        val unsavedPartQuestions = questionReader
            .readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId)
            .filter { it.id == null || it.id !in savedPartSourceIds }

        val unsavedCatalogQuestions = unsavedCultureQuestions + unsavedPartQuestions
        if (unsavedCatalogQuestions.isEmpty()) return emptyList()

        val startSortOrder = (savedDtos.maxOfOrNull { it.sortOrder } ?: -1) + 1

        return unsavedCatalogQuestions.mapIndexed { offset, question ->
            AssignedQuestionDto(
                id = null,
                assignedMemberId = null,
                assignedMemberName = null,
                sourceQuestionId = question.id,
                content = question.content,
                category = question.category.toAssignedQuestionCategory(),
                sortOrder = startSortOrder + offset,
                isSelected =
                    if (question.category == QuestionCategory.CULTURE) {
                        question.id != null && question.id in selectedCultureQuestionIds
                    } else {
                        null
                    },
                requirements = question.requirementIds.mapNotNull { requirementId ->
                    requirementsById[requirementId]?.let { requirement ->
                        QuestionRequirementDto(requirement.id, requirement.content)
                    }
                },
            )
        }
    }

    private fun readAssignedMemberNamesById(questions: List<AssignedQuestion>): Map<Long, String> {
        return questions
            .mapNotNull { it.assignedMemberId }
            .distinct()
            .mapNotNull { memberId ->
                val member = runCatching { memberReader.readById(memberId) }.getOrNull() ?: return@mapNotNull null
                val name = member.nicknameEnglish.ifBlank { member.name }
                memberId to name
            }.toMap()
    }

    private fun readDefaultQuestions(
        applicantPartId: Long,
        applicantSemesterId: Long,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
        selectedCultureQuestionIds: Set<Long>,
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
                    assignedMemberId = null,
                    assignedMemberName = null,
                    sourceQuestionId = question.id,
                    content = question.content,
                    category = question.category.toAssignedQuestionCategory(),
                    sortOrder = index,
                    isSelected =
                        if (question.category == QuestionCategory.CULTURE) {
                            question.id != null && question.id in selectedCultureQuestionIds
                        } else {
                            null
                        },
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
        assignedMemberNamesById: Map<Long, String>,
        selectedCultureQuestionIds: Set<Long>,
    ): AssignedQuestionDto {
        // 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)는 요구조건을 원본 질문(Question)에서 가져오고, PERSONAL만 인스턴스 자체 값을 사용한다.
        val effectiveRequirementIds =
            if (category == AssignedQuestionCategory.PERSONAL) {
                requirementIds
            } else {
                sourceQuestionsById[sourceQuestionId]?.requirementIds.orEmpty()
            }

        // CULTURE 선택 여부는 지원자 개인 값이 아닌 파트+학기 단위 공유 상태에서 가져온다.
        val effectiveIsSelected =
            if (category == AssignedQuestionCategory.CULTURE) {
                sourceQuestionId != null && sourceQuestionId in selectedCultureQuestionIds
            } else {
                isSelected
            }

        return AssignedQuestionDto(
            id = id!!,
            assignedMemberId = assignedMemberId,
            assignedMemberName = assignedMemberId?.let { assignedMemberNamesById[it] },
            sourceQuestionId = sourceQuestionId,
            content = content ?: sourceQuestionsById[sourceQuestionId]?.content.orEmpty(),
            category = category,
            sortOrder = sortOrder,
            isSelected = effectiveIsSelected,
            requirements =
                effectiveRequirementIds.mapNotNull { requirementId ->
                    requirementsById[requirementId]?.let { requirement ->
                        QuestionRequirementDto(requirement.id, requirement.content)
                    }
                },
        )
    }
}
