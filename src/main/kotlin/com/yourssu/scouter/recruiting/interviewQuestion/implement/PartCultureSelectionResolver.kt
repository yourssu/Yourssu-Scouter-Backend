package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component

/**
 * CULTURE 질문 선택 여부는 지원자 개인이 아닌 파트+학기 단위로 공유된다.
 * 파트가 잠겨 있으면(= 같은 파트에서 이미 면접 평가가 저장되어 루브릭이 잠긴 상태) 선택 변경 요청은
 * 조용히 무시하고 기존 선택을 그대로 반환한다. 잠기지 않았을 때만 변경되며, 변경 시 같은 파트 지원자
 * 전원의 조회 결과에 반영된다.
 */
@Component
class PartCultureSelectionResolver(
    private val partCultureSelectionReader: PartCultureSelectionReader,
    private val partCultureSelectionWriter: PartCultureSelectionWriter,
) {

    fun resolve(
        drafts: List<AssignedQuestionDraft>,
        partId: Long,
        semesterId: Long,
        partLocked: Boolean,
    ): Set<Long> {
        val currentSelection = partCultureSelectionReader.readSelectedQuestionIds(partId, semesterId)
        if (partLocked) return currentSelection

        val newSelection = drafts
            .filter { it.category == AssignedQuestionCategory.CULTURE && it.isSelected == true }
            .mapNotNull { it.sourceQuestionId }
            .toSet()
        if (newSelection == currentSelection) return currentSelection

        partCultureSelectionWriter.replaceSelection(partId, semesterId, newSelection.toList())
        return newSelection
    }
}
