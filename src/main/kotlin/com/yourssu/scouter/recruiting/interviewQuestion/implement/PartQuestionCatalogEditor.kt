package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.springframework.stereotype.Component

/**
 * PART 질문은 지원자 개인이 아닌 파트+학기 카탈로그(Question)에 귀속된다.
 * 지원자별 질문지 저장 요청에 섞여 들어온 카탈로그 생성/수정/삭제를 여기서 처리한다.
 *
 * 잠긴 파트에서 카탈로그를 지키는 책임은 호출부에 있다. 이미 제출된 평가가 근거한 질문이
 * 아직 평가받지 않은 지원자의 요청으로 바뀌면 안 되므로, 호출부가 잠긴 동안
 * [resolveNewPartQuestions]에 신규 요청을 넘기지 않고 [updatePartQuestions]와
 * [deleteRemovedPartQuestions]를 호출하지 않는다.
 */
@Component
class PartQuestionCatalogEditor(
    private val questionReader: QuestionReader,
    private val questionWriter: QuestionWriter,
    private val assignedQuestionWriter: AssignedQuestionWriter,
) {

    /**
     * sourceQuestionId 없이 들어온 신규 PART 질문을 카탈로그 Question으로 먼저 저장하고,
     * 발급된 id를 채운 요청 목록을 돌려준다. (PART는 카탈로그 카테고리라 AssignedQuestion
     * 생성자가 sourceQuestionId를 필수로 요구하기 때문)
     */
    fun resolveNewPartQuestions(
        drafts: List<AssignedQuestionDraft>,
        partId: Long,
        semesterId: Long,
    ): List<AssignedQuestionDraft> {
        if (drafts.none { it.isNewPartQuestion }) return drafts

        var nextSortOrder = questionReader.readAllByPartIdAndSemesterId(partId, semesterId).size
        return drafts.map { draft ->
            if (!draft.isNewPartQuestion) return@map draft

            val content = draft.content ?: throw QuestionInvalidException("신규 PART 질문은 content가 필요합니다.")
            val saved = questionWriter.save(
                Question(
                    partId = partId,
                    semesterId = semesterId,
                    category = QuestionCategory.PART,
                    content = content,
                    sortOrder = nextSortOrder++,
                    requirementIds = draft.requirementIds ?: emptyList(),
                ),
            )
            draft.copy(sourceQuestionId = saved.id!!)
        }
    }

    /**
     * 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)의 content와 요구조건은 인스턴스가 아닌
     * 원본 질문(Question)에 매핑된다. 그중 INTRO/OUTRO/CULTURE는 이 요청으로 바꿀 수 없다.
     * 잠금과 무관하게 항상 거절한다.
     */
    fun rejectSharedCatalogChanges(
        drafts: List<AssignedQuestionDraft>,
        sourceQuestionsById: Map<Long?, Question>,
    ) {
        drafts.forEach { draft ->
            if (draft.sourceQuestionId == null) return@forEach

            val sourceQuestion = sourceQuestionsById.getValue(draft.sourceQuestionId)
            if (sourceQuestion.category == QuestionCategory.PART) return@forEach

            if (draft.requirementIds != null) {
                throw QuestionInvalidException("${sourceQuestion.category} 질문은 요구조건을 변경할 수 없습니다.")
            }
            if (draft.content != null) {
                throw QuestionInvalidException("${sourceQuestion.category} 질문은 content를 변경할 수 없습니다.")
            }
        }
    }

    fun updatePartQuestions(
        drafts: List<AssignedQuestionDraft>,
        sourceQuestionsById: Map<Long?, Question>,
    ) {
        drafts.forEach { draft ->
            if (draft.sourceQuestionId == null) return@forEach

            val sourceQuestion = sourceQuestionsById.getValue(draft.sourceQuestionId)
            if (sourceQuestion.category != QuestionCategory.PART) return@forEach

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

    /**
     * 이번 저장에서 빠진 기존 PART 질문을 카탈로그에서 삭제하고,
     * 이를 참조하던 모든 지원자의 인스턴스도 함께 정리한다.
     * (신규 추가가 카탈로그에 즉시 반영되는 것과 대칭되는 동작)
     */
    fun deleteRemovedPartQuestions(
        assignedQuestions: List<AssignedQuestion>,
        partId: Long,
        semesterId: Long,
    ) {
        val existingPartQuestionIds =
            questionReader.readAllByPartIdAndSemesterId(partId, semesterId).mapNotNull { it.id }.toSet()
        val removedIds = existingPartQuestionIds - assignedQuestions.partSourceQuestionIds()
        if (removedIds.isEmpty()) return

        assignedQuestionWriter.deleteAllBySourceQuestionIdIn(removedIds.toList())
        questionWriter.deleteAllByIdIn(removedIds)
    }
}
