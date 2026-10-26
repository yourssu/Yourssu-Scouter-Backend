package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.springframework.stereotype.Component

/**
 * PART 질문은 지원자 개인이 아닌 파트+학기 카탈로그(Question)에 귀속된다.
 * 지원자별 질문지 저장 요청에 섞여 들어온 카탈로그 생성/수정/삭제를 여기서 처리한다.
 *
 * 파트가 잠긴 상태(= 같은 파트에서 이미 면접 평가가 저장되어 루브릭이 잠긴 상태)에서는
 * 카탈로그를 바꾸는 요청을 모두 조용히 무시한다. 이미 제출된 평가가 근거한 질문이
 * 아직 평가받지 않은 지원자의 요청으로 우회 수정되면 안 되기 때문이다.
 */
@Component
class PartQuestionCatalogEditor(
    private val questionReader: QuestionReader,
    private val questionWriter: QuestionWriter,
    private val assignedQuestionWriter: AssignedQuestionWriter,
) {

    /**
     * sourceQuestionId 없이 들어온 신규 PART 질문을 카탈로그 Question으로 먼저 저장하고,
     * 발급된 id를 AssignedQuestion 생성 전에 미리 매칭시켜준다. (PART는 카탈로그 카테고리라
     * AssignedQuestion 생성자가 sourceQuestionId를 필수로 요구하기 때문)
     * 반환값은 요청 목록 내 index -> 새로 저장된 Question id.
     */
    fun createNewPartQuestions(
        drafts: List<AssignedQuestionDraft>,
        partId: Long,
        semesterId: Long,
        partLocked: Boolean,
    ): Map<Int, Long> {
        if (partLocked) return emptyMap()

        val newPartQuestions = drafts.withIndex().filter { (_, question) -> question.isNewPartQuestion }
        if (newPartQuestions.isEmpty()) return emptyMap()

        val existingPartQuestionCount = questionReader.readAllByPartIdAndSemesterId(partId, semesterId).size

        return newPartQuestions.mapIndexed { offset, (index, question) ->
            val content = question.content
                ?: throw QuestionInvalidException("신규 PART 질문은 content가 필요합니다.")

            val saved = questionWriter.save(
                Question(
                    partId = partId,
                    semesterId = semesterId,
                    category = QuestionCategory.PART,
                    content = content,
                    sortOrder = existingPartQuestionCount + offset,
                    requirementIds = question.requirementIds ?: emptyList(),
                ),
            )
            index to saved.id!!
        }.toMap()
    }

    /**
     * 카탈로그 content/요구조건 갱신 후, 이번 저장에서 빠진 기존 PART 질문을 카탈로그에서 삭제한다.
     * (신규 추가가 카탈로그에 즉시 반영되는 것과 대칭되는 동작)
     *
     * 순서 주의: 갱신이 먼저다. 갱신은 [sourceQuestionsById]에 요청된 모든 원본 질문이 들어 있다고
     * 전제하므로 호출 전에 AssignedQuestionValidator.validate가 먼저 수행되어야 한다.
     */
    fun applyChanges(
        drafts: List<AssignedQuestionDraft>,
        sourceQuestionsById: Map<Long?, Question>,
        assignedQuestions: List<AssignedQuestion>,
        partId: Long,
        semesterId: Long,
        partLocked: Boolean,
    ) {
        updateCatalogQuestions(drafts, sourceQuestionsById, partLocked)
        deleteRemovedPartQuestions(assignedQuestions, partId, semesterId, partLocked)
    }

    private fun updateCatalogQuestions(
        drafts: List<AssignedQuestionDraft>,
        sourceQuestionsById: Map<Long?, Question>,
        partLocked: Boolean,
    ) {
        // 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)의 content와 요구조건은 인스턴스가 아닌 원본 질문(Question)에 매핑된다.
        // INTRO/OUTRO/CULTURE는 이 요청으로 값을 변경할 수 없고, PART만 여기서 갱신 가능하다.
        drafts
            .filter { it.sourceQuestionId != null }
            .forEach { draft ->
                val sourceQuestion = sourceQuestionsById.getValue(draft.sourceQuestionId)
                when (sourceQuestion.category) {
                    QuestionCategory.INTRO, QuestionCategory.OUTRO, QuestionCategory.CULTURE -> {
                        if (draft.requirementIds != null) {
                            throw QuestionInvalidException("${sourceQuestion.category} 질문은 요구조건을 변경할 수 없습니다.")
                        }
                        if (draft.content != null) {
                            throw QuestionInvalidException("${sourceQuestion.category} 질문은 content를 변경할 수 없습니다.")
                        }
                    }

                    QuestionCategory.PART -> {
                        if (partLocked) return@forEach

                        questionWriter.update(
                            Question(
                                id = sourceQuestion.id,
                                partId = sourceQuestion.partId,
                                semesterId = sourceQuestion.semesterId,
                                category = sourceQuestion.category,
                                content = draft.content ?: sourceQuestion.content,
                                sortOrder = sourceQuestion.sortOrder,
                                requirementIds = draft.requirementIds ?: sourceQuestion.requirementIds,
                            ),
                        )
                    }
                }
            }
    }

    /**
     * 이번 저장에서 빠진 기존 PART 질문을 카탈로그에서 삭제하고,
     * 이를 참조하던 모든 지원자의 인스턴스도 함께 정리한다.
     */
    private fun deleteRemovedPartQuestions(
        assignedQuestions: List<AssignedQuestion>,
        partId: Long,
        semesterId: Long,
        partLocked: Boolean,
    ) {
        if (partLocked) return

        val existingPartQuestionIds =
            questionReader.readAllByPartIdAndSemesterId(partId, semesterId).mapNotNull { it.id }.toSet()
        val submittedPartSourceIds = assignedQuestions.partSourceQuestionIds()

        val removedIds = existingPartQuestionIds - submittedPartSourceIds
        if (removedIds.isEmpty()) return

        assignedQuestionWriter.deleteAllBySourceQuestionIdIn(removedIds.toList())
        questionWriter.deleteAllByIdIn(removedIds)
    }
}
