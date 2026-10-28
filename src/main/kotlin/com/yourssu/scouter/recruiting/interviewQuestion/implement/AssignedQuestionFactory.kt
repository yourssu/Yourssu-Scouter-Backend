package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component

/** 저장 요청(AssignedQuestionDraft)으로 저장할 AssignedQuestion 인스턴스를 만든다. 응답 조립은 AssignedQuestionViewAssembler가 맡는다. */
@Component
class AssignedQuestionFactory(
    private val assignedQuestionReader: AssignedQuestionReader,
) {

    fun assemble(applicantId: Long, drafts: List<AssignedQuestionDraft>): List<AssignedQuestion> =
        drafts.mapIndexed { sortOrder, draft ->
            val personal = draft.category == AssignedQuestionCategory.PERSONAL
            AssignedQuestion(
                assignedMemberId = draft.assignedMemberId,
                applicantId = applicantId,
                sourceQuestionId = draft.sourceQuestionId,
                content = draft.content.takeIf { personal },
                category = draft.category,
                sortOrder = sortOrder,
                // CULTURE 선택 여부는 지원자 개인 값이 아니라 파트+학기 단위 공유 상태라 인스턴스에 담지 않는다.
                isSelected = draft.isSelected.takeIf { draft.category != AssignedQuestionCategory.CULTURE },
                requirementIds = if (personal) draft.requirementIds ?: emptyList() else emptyList(),
            )
        }

    /**
     * 요청 목록에서 빠진 이 지원자의 기존 PART 인스턴스를 복원한다.
     * replaceAll은 요청에 없는 인스턴스를 그대로 지워버리므로, 파트가 잠긴 동안에는
     * 요청에서 빼는 방식의 우회 삭제를 막기 위해 호출한다.
     */
    fun partQuestionsDroppedFromRequest(
        applicantId: Long,
        questions: List<AssignedQuestion>,
    ): List<AssignedQuestion> {
        val submittedPartSourceIds = questions.partSourceQuestionIds()
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
}
