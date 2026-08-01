package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionInvalidException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionNotFoundException
import org.springframework.stereotype.Component

@Component
class AssignedQuestionValidator {

    private companion object {
        const val SELECTED_CULTURE_QUESTIONS = 2
    }

    fun validate(questions: List<AssignedQuestion>, sourceQuestionsById: Map<Long?, Question>, applicantPartId: Long) {
        val selectedCultureCount = questions.count {
            it.category == AssignedQuestionCategory.CULTURE && it.isSelected == true
        }
        if (selectedCultureCount != SELECTED_CULTURE_QUESTIONS) {
            throw AssignedQuestionInvalidException(
                "CULTURE 카테고리 문항은 정확히 ${SELECTED_CULTURE_QUESTIONS}개 선택되어야 합니다.",
            )
        }

        questions.filter { it.category.isCatalog }.forEach { assignedQuestion ->
            val sourceQuestion = sourceQuestionsById[assignedQuestion.sourceQuestionId]
                ?: throw QuestionNotFoundException("존재하지 않는 질문입니다: ${assignedQuestion.sourceQuestionId}")

            if (sourceQuestion.category.name != assignedQuestion.category.name) {
                throw AssignedQuestionInvalidException(
                    "원본 질문의 카테고리(${sourceQuestion.category})가 할당 질문의 카테고리(${assignedQuestion.category})와 일치하지 않습니다.",
                )
            }

            if (sourceQuestion.category == QuestionCategory.PART && sourceQuestion.partId != applicantPartId) {
                throw AssignedQuestionInvalidException("파트 질문은 지원자의 파트에 속한 질문이어야 합니다: ${sourceQuestion.id}")
            }

            if (sourceQuestion.category != QuestionCategory.PART && sourceQuestion.partId != null) {
                throw AssignedQuestionInvalidException("파트에 묶인 질문은 PART 카테고리여야 합니다: ${sourceQuestion.id}")
            }
        }
    }
}
