package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import org.springframework.stereotype.Component

@Component
class PartQuestionAssignmentPolicy(
    private val memberReader: MemberReader,
    private val assignedQuestionReader: AssignedQuestionReader
) {
    fun resolve(requestQuestions : List<SaveAssignedQuestionCommand>,
                                   partLocked : Boolean,
                                   applicantId:Long,
                                   resolvedSourceQuestionIds :Map<Int, Long>,
                                   selectedCultureQuestionIds :Set<Long>
                                   ) : List<AssignedQuestion>{
        val questions = requestQuestions.withIndex()
            .filterNot { (_, question) ->
                // 파트가 잠긴 상태에서 신규 PART 질문 생성 요청은 조용히 무시한다.
                partLocked && question.category == AssignedQuestionCategory.PART && question.sourceQuestionId == null
            }
            .mapIndexed { sortOrder, (originalIndex, question) ->
                memberReader.readById(question.assignedMemberId)

                val sourceQuestionId = resolvedSourceQuestionIds[originalIndex] ?: question.sourceQuestionId
                // CULTURE 선택 여부는 지원자 개인 값이 아닌, 위에서 결정된 파트+학기 단위 선택 결과를 그대로 따른다.
                val isSelected = if (question.category == AssignedQuestionCategory.CULTURE) {
                    sourceQuestionId != null && sourceQuestionId in selectedCultureQuestionIds
                } else {
                    question.isSelected
                }

                AssignedQuestion(
                    assignedMemberId = question.assignedMemberId,
                    applicantId = applicantId,
                    sourceQuestionId = sourceQuestionId,
                    content = if (question.category == AssignedQuestionCategory.PERSONAL) question.content else null,
                    category = question.category,
                    sortOrder = sortOrder,
                    isSelected = isSelected,
                    requirementIds =
                        if (question.category == AssignedQuestionCategory.PERSONAL) {
                            question.requirementIds ?: emptyList()
                        } else {
                            emptyList()
                        },
                )
            }
        return restorePartQuestionsRemovedWhileLocked(applicantId, questions, partLocked)
    }

    private fun restorePartQuestionsRemovedWhileLocked(
        applicantId: Long,
        questions: List<AssignedQuestion>,
        partLocked: Boolean,
    ): List<AssignedQuestion> {
        if (!partLocked) return questions

        val submittedPartSourceIds = questions
            .filter { it.category == AssignedQuestionCategory.PART }
            .mapNotNull { it.sourceQuestionId }
            .toSet()

        val missingPartQuestions = assignedQuestionReader.readAllByApplicantId(applicantId)
            .filter { it.category == AssignedQuestionCategory.PART && it.sourceQuestionId !in submittedPartSourceIds }
        if (missingPartQuestions.isEmpty()) return questions

        val startSortOrder = (questions.maxOfOrNull { it.sortOrder } ?: -1) + 1
        return questions + missingPartQuestions.mapIndexed { offset, question ->
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