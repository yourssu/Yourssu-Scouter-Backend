package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component

/**
 * CULTURE 질문 선택 여부는 지원자 개인이 아닌 파트+학기 단위로 공유된다.
 * 한 지원자의 저장으로 바뀌면 같은 파트 지원자 전원의 조회 결과에 반영된다.
 * 잠긴 파트에서는 호출부가 이 교체를 건너뛰고 기존 선택을 그대로 쓴다.
 */
@Component
class PartCultureSelectionResolver(
    private val partCultureSelectionReader: PartCultureSelectionReader,
    private val partCultureSelectionWriter: PartCultureSelectionWriter,
) {

    fun replace(drafts: List<AssignedQuestionDraft>, partId: Long, semesterId: Long): Set<Long> {
        val currentSelection = partCultureSelectionReader.readSelectedQuestionIds(partId, semesterId)
        val newSelection = drafts
            .filter { it.category == AssignedQuestionCategory.CULTURE && it.isSelected == true }
            .mapNotNull { it.sourceQuestionId }
            .toSet()
        if (newSelection == currentSelection) return currentSelection

        partCultureSelectionWriter.replaceSelection(partId, semesterId, newSelection.toList())
        return newSelection
    }
}
