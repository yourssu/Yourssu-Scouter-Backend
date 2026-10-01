package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import org.springframework.stereotype.Component
import kotlin.collections.contains

@Component
class PartCultureSelectionApplier (
    private final val partCultureSelectionReader: PartCultureSelectionReader,
    private final val partCultureSelectionWriter: PartCultureSelectionWriter
){

    fun updateSelection(
        questions: List<SaveAssignedQuestionCommand>,
        applicantPartId: Long,
        applicantSemesterId: Long,
        partLocked: Boolean,
    ): Set<Long> {
        val currentSelection = partCultureSelectionReader.readSelectedQuestionIds(applicantPartId, applicantSemesterId)
        if (partLocked) return currentSelection

        val newSelection =
            questions
                .filter { it.category == AssignedQuestionCategory.CULTURE && it.isSelected == true }
                .mapNotNull { it.sourceQuestionId }
                .toSet()
        if (newSelection == currentSelection) return currentSelection

        partCultureSelectionWriter.replaceSelection(applicantPartId, applicantSemesterId, newSelection.toList())
        return newSelection
    }

}