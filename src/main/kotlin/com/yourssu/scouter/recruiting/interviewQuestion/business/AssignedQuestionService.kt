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
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionWriter
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
    private val questionWriter: QuestionWriter,
    private val assignedQuestionValidator: AssignedQuestionValidator,
    private val applicantReader: ApplicantReader,
    private val memberReader: MemberReader,
    private val interviewRequirementLookup: InterviewRequirementLookup,
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val partCultureSelectionReader: PartCultureSelectionReader,
    private val partCultureSelectionWriter: PartCultureSelectionWriter,
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

        // 같은 파트의 다른 지원자가 이미 면접 평가를 받았다면, 그 평가가 근거한 파트 공유 질문(카탈로그)이
        // 아직 평가받지 않은 지원자의 요청으로 우회 수정되지 않도록 파트 단위로 잠근다.
        // 잠긴 파트에서는 카탈로그 생성/변경/삭제와 CULTURE 선택 변경 요청을 모두 조용히 무시한다.
        val partLocked = isPartLocked(applicantPartId, applicantSemesterId)

        val acceptedCommands = if (partLocked) command.questions.filterNot(::isNewPartQuestion) else command.questions
        val resolvedQuestions = resolveSourceQuestions(acceptedCommands, applicantPartId, applicantSemesterId)
        val selectedCultureQuestionIds =
            if (partLocked) {
                partCultureSelectionReader.readSelectedQuestionIds(applicantPartId, applicantSemesterId)
            } else {
                replacePartCultureSelection(command.questions, applicantPartId, applicantSemesterId)
            }

        val questions = resolvedQuestions
            .mapIndexed { sortOrder, (questionCommand, sourceQuestionId) ->
                memberReader.readById(questionCommand.assignedMemberId)
                questionCommand.toAssignedQuestion(applicantId, sourceQuestionId, sortOrder)
            }
            .let { if (partLocked) it + partQuestionsDroppedFromRequest(applicantId, it) else it }

        val sourceQuestionsById = readSourceQuestionsById(questions)
        assignedQuestionValidator.validate(questions, sourceQuestionsById, applicantPartId, selectedCultureQuestionIds)

        rejectSharedCatalogChanges(command.questions, sourceQuestionsById)
        if (!partLocked) {
            updatePartCatalogQuestions(command.questions, sourceQuestionsById)
            deleteRemovedPartQuestions(questions, applicantPartId, applicantSemesterId)
        }

        val saved = assignedQuestionWriter.replaceAll(applicantId, questions)

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

    private fun isNewPartQuestion(command: SaveAssignedQuestionCommand): Boolean =
        command.category == AssignedQuestionCategory.PART && command.sourceQuestionId == null

    private data class ResolvedQuestion(
        val command: SaveAssignedQuestionCommand,
        val sourceQuestionId: Long?,
    )

    /**
     * sourceQuestionId 없이 들어온 신규 PART 질문을 카탈로그 Question으로 먼저 저장하고, 발급된 id를 요청에 붙여준다.
     * (PART는 카탈로그 카테고리라 AssignedQuestion 생성자가 sourceQuestionId를 필수로 요구하기 때문)
     */
    private fun resolveSourceQuestions(
        commands: List<SaveAssignedQuestionCommand>,
        applicantPartId: Long,
        applicantSemesterId: Long,
    ): List<ResolvedQuestion> {
        if (commands.none(::isNewPartQuestion)) {
            return commands.map { ResolvedQuestion(it, it.sourceQuestionId) }
        }

        var nextSortOrder = questionReader.readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId).size
        return commands.map { command ->
            if (!isNewPartQuestion(command)) return@map ResolvedQuestion(command, command.sourceQuestionId)

            val content = command.content ?: throw QuestionInvalidException("신규 PART 질문은 content가 필요합니다.")
            val saved = questionWriter.save(
                Question(
                    partId = applicantPartId,
                    semesterId = applicantSemesterId,
                    category = QuestionCategory.PART,
                    content = content,
                    sortOrder = nextSortOrder++,
                    requirementIds = command.requirementIds ?: emptyList(),
                ),
            )
            ResolvedQuestion(command, saved.id!!)
        }
    }

    private fun SaveAssignedQuestionCommand.toAssignedQuestion(
        applicantId: Long,
        sourceQuestionId: Long?,
        sortOrder: Int,
    ): AssignedQuestion {
        val personal = category == AssignedQuestionCategory.PERSONAL
        return AssignedQuestion(
            assignedMemberId = assignedMemberId,
            applicantId = applicantId,
            sourceQuestionId = sourceQuestionId,
            content = content.takeIf { personal },
            category = category,
            sortOrder = sortOrder,
            // CULTURE 선택 여부는 지원자 개인 값이 아니라 파트+학기 단위 공유 상태라 인스턴스에 담지 않는다.
            isSelected = isSelected.takeIf { category != AssignedQuestionCategory.CULTURE },
            requirementIds = if (personal) requirementIds ?: emptyList() else emptyList(),
        )
    }

    /**
     * CULTURE 질문 선택 여부는 지원자 개인이 아닌 파트+학기 단위로 공유된다.
     * 변경하면 같은 파트 지원자 전원의 조회 결과에 반영된다.
     */
    private fun replacePartCultureSelection(
        commands: List<SaveAssignedQuestionCommand>,
        applicantPartId: Long,
        applicantSemesterId: Long,
    ): Set<Long> {
        val currentSelection = partCultureSelectionReader.readSelectedQuestionIds(applicantPartId, applicantSemesterId)
        val newSelection = commands
            .filter { it.category == AssignedQuestionCategory.CULTURE && it.isSelected == true }
            .mapNotNull { it.sourceQuestionId }
            .toSet()
        if (newSelection == currentSelection) return currentSelection

        partCultureSelectionWriter.replaceSelection(applicantPartId, applicantSemesterId, newSelection.toList())
        return newSelection
    }

    /**
     * 잠긴 파트에서는 요청 목록에서 기존 PART 질문을 빼는 방식으로 우회 삭제할 수 없어야 한다.
     * replaceAll은 요청에 없는 인스턴스를 그대로 지워버리므로, 요청에서 누락된 기존 PART 인스턴스를 찾아
     * 그대로 복원해 함께 저장되도록 한다.
     */
    private fun partQuestionsDroppedFromRequest(
        applicantId: Long,
        questions: List<AssignedQuestion>,
    ): List<AssignedQuestion> {
        val submittedPartSourceIds = questions
            .filter { it.category == AssignedQuestionCategory.PART }
            .mapNotNull { it.sourceQuestionId }
            .toSet()
        val startSortOrder = (questions.maxOfOrNull { it.sortOrder } ?: -1) + 1

        return assignedQuestionReader.readAllByApplicantId(applicantId)
            .filter { it.category == AssignedQuestionCategory.PART && it.sourceQuestionId !in submittedPartSourceIds }
            .mapIndexed { offset, question ->
                AssignedQuestion(
                    assignedMemberId = question.assignedMemberId,
                    applicantId = applicantId,
                    sourceQuestionId = question.sourceQuestionId,
                    content = question.content,
                    category = question.category,
                    sortOrder = startSortOrder + offset,
                    isSelected = question.isSelected,
                    requirementIds = question.requirementIds,
                )
            }
    }

    /**
     * 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)의 content와 요구조건은 인스턴스가 아닌 원본 질문(Question)에 매핑된다.
     * 그중 INTRO/OUTRO/CULTURE는 이 요청으로 값을 변경할 수 없다. (PART만 updatePartCatalogQuestions에서 갱신된다)
     */
    private fun rejectSharedCatalogChanges(
        commands: List<SaveAssignedQuestionCommand>,
        sourceQuestionsById: Map<Long?, Question>,
    ) {
        commands.forEach { command ->
            if (command.sourceQuestionId == null) return@forEach

            val sourceQuestion = sourceQuestionsById.getValue(command.sourceQuestionId)
            if (sourceQuestion.category == QuestionCategory.PART) return@forEach

            if (command.requirementIds != null) {
                throw QuestionInvalidException("${sourceQuestion.category} 질문은 요구조건을 변경할 수 없습니다.")
            }
            if (command.content != null) {
                throw QuestionInvalidException("${sourceQuestion.category} 질문은 content를 변경할 수 없습니다.")
            }
        }
    }

    private fun updatePartCatalogQuestions(
        commands: List<SaveAssignedQuestionCommand>,
        sourceQuestionsById: Map<Long?, Question>,
    ) {
        commands.forEach { command ->
            if (command.sourceQuestionId == null) return@forEach

            val sourceQuestion = sourceQuestionsById.getValue(command.sourceQuestionId)
            if (sourceQuestion.category != QuestionCategory.PART) return@forEach

            questionWriter.update(
                Question(
                    id = sourceQuestion.id,
                    partId = sourceQuestion.partId,
                    semesterId = sourceQuestion.semesterId,
                    category = sourceQuestion.category,
                    content = command.content ?: sourceQuestion.content,
                    sortOrder = sourceQuestion.sortOrder,
                    requirementIds = command.requirementIds ?: sourceQuestion.requirementIds,
                ),
            )
        }
    }

    /**
     * PART 질문은 지원자 개인이 아닌 파트+학기 카탈로그에 귀속된다.
     * 이번 저장에서 빠진 기존 PART 질문은 카탈로그에서 삭제하고, 이를 참조하던 모든 지원자의
     * 인스턴스도 함께 정리한다. (신규 추가가 카탈로그에 즉시 반영되는 것과 대칭되는 동작)
     */
    private fun deleteRemovedPartQuestions(
        questions: List<AssignedQuestion>,
        applicantPartId: Long,
        applicantSemesterId: Long,
    ) {
        val existingPartQuestionIds =
            questionReader.readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId).mapNotNull { it.id }.toSet()
        val submittedPartSourceIds =
            questions.filter { it.category == AssignedQuestionCategory.PART }.mapNotNull { it.sourceQuestionId }.toSet()

        val removedIds = existingPartQuestionIds - submittedPartSourceIds
        if (removedIds.isEmpty()) return

        assignedQuestionWriter.deleteAllBySourceQuestionIdIn(removedIds.toList())
        questionWriter.deleteAllByIdIn(removedIds)
    }

    private fun isPartLocked(partId: Long, semesterId: Long): Boolean {
        val applicantIdsInPart = applicantReader.readByPartId(partId)
            .filter { it.applicationSemester.id == semesterId }
            .mapNotNull { it.id }
        if (applicantIdsInPart.isEmpty()) return false

        return interviewEvaluationReader.readAllByApplicantIdIn(applicantIdsInPart).isNotEmpty()
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
