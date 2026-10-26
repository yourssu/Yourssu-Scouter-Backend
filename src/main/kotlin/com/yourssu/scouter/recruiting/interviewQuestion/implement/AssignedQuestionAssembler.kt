package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component

/**
 * 저장 요청(AssignedQuestionDraft)을 실제로 저장할 AssignedQuestion 인스턴스 목록으로 조립한다.
 *
 * 파트가 잠긴 상태(= 같은 파트에서 이미 면접 평가가 저장되어 루브릭이 잠긴 상태)에서는
 * 요청으로 PART 질문을 추가하거나, 요청 목록에서 빼는 방식으로 삭제할 수 없어야 한다.
 */
@Component
class AssignedQuestionAssembler(
    private val assignedQuestionReader: AssignedQuestionReader,
) {

    /** 실제로 저장될 문항에 배정된 면접관 id. 잠금으로 무시되는 요청의 면접관은 포함하지 않는다. */
    fun editableAssignedMemberIds(drafts: List<AssignedQuestionDraft>, partLocked: Boolean): List<Long> =
        drafts.editable(partLocked).map { it.assignedMemberId }.distinct()

    fun assemble(
        applicantId: Long,
        drafts: List<AssignedQuestionDraft>,
        newPartQuestionIds: Map<Int, Long>,
        selectedCultureQuestionIds: Set<Long>,
        partLocked: Boolean,
    ): List<AssignedQuestion> {
        val questions = drafts.withIndex()
            .filterNot { (_, draft) -> draft.isIgnoredWhileLocked(partLocked) }
            .mapIndexed { sortOrder, (originalIndex, draft) ->
                val sourceQuestionId = newPartQuestionIds[originalIndex] ?: draft.sourceQuestionId

                AssignedQuestion(
                    assignedMemberId = draft.assignedMemberId,
                    applicantId = applicantId,
                    sourceQuestionId = sourceQuestionId,
                    content = if (draft.category == AssignedQuestionCategory.PERSONAL) draft.content else null,
                    category = draft.category,
                    sortOrder = sortOrder,
                    isSelected = resolveIsSelected(draft, sourceQuestionId, selectedCultureQuestionIds),
                    requirementIds = if (draft.category == AssignedQuestionCategory.PERSONAL) {
                        draft.requirementIds ?: emptyList()
                    } else {
                        emptyList()
                    },
                )
            }

        return questions + restorePartQuestionsRemovedWhileLocked(applicantId, questions, partLocked)
    }

    /**
     * CULTURE 선택 여부는 지원자 개인 값이 아닌 파트+학기 단위 공유 상태라 인스턴스에 저장하지 않는다.
     * AssignedQuestionValidator가 선택 개수를 세므로 검증 이후, 저장 직전에 적용해야 한다.
     */
    fun stripSharedCultureSelection(questions: List<AssignedQuestion>): List<AssignedQuestion> =
        questions.map { question ->
            if (question.category != AssignedQuestionCategory.CULTURE || question.isSelected == null) {
                question
            } else {
                AssignedQuestion(
                    id = question.id,
                    assignedMemberId = question.assignedMemberId,
                    applicantId = question.applicantId,
                    sourceQuestionId = question.sourceQuestionId,
                    content = question.content,
                    category = question.category,
                    sortOrder = question.sortOrder,
                    isSelected = null,
                    requirementIds = question.requirementIds,
                )
            }
        }

    /**
     * replaceAll은 요청에 없는 인스턴스를 그대로 지워버리므로, 잠긴 상태에서 요청에서 누락된 기존 PART
     * 인스턴스를 찾아 그대로 복원해 함께 저장되도록 한다.
     */
    private fun restorePartQuestionsRemovedWhileLocked(
        applicantId: Long,
        questions: List<AssignedQuestion>,
        partLocked: Boolean,
    ): List<AssignedQuestion> {
        if (!partLocked) return emptyList()

        val submittedPartSourceIds = questions.partSourceQuestionIds()
        val missingPartQuestions = assignedQuestionReader.readAllByApplicantId(applicantId)
            .filter { it.category == AssignedQuestionCategory.PART && it.sourceQuestionId !in submittedPartSourceIds }
        if (missingPartQuestions.isEmpty()) return emptyList()

        val startSortOrder = (questions.maxOfOrNull { it.sortOrder } ?: -1) + 1

        return missingPartQuestions.mapIndexed { offset, question ->
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

    /** CULTURE 선택 여부는 지원자 개인 값이 아닌, 파트+학기 단위로 결정된 선택 결과를 그대로 따른다. */
    private fun resolveIsSelected(
        draft: AssignedQuestionDraft,
        sourceQuestionId: Long?,
        selectedCultureQuestionIds: Set<Long>,
    ): Boolean? =
        if (draft.category == AssignedQuestionCategory.CULTURE) {
            sourceQuestionId != null && sourceQuestionId in selectedCultureQuestionIds
        } else {
            draft.isSelected
        }

    private fun List<AssignedQuestionDraft>.editable(partLocked: Boolean): List<AssignedQuestionDraft> =
        filterNot { it.isIgnoredWhileLocked(partLocked) }

    /** 파트가 잠긴 상태에서 신규 PART 질문 생성 요청은 조용히 무시한다. */
    private fun AssignedQuestionDraft.isIgnoredWhileLocked(partLocked: Boolean): Boolean =
        partLocked && isNewPartQuestion
}
