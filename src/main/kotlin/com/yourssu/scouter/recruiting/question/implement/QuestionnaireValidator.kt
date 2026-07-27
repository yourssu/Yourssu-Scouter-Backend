package com.yourssu.scouter.recruiting.question.implement

import com.yourssu.scouter.recruiting.support.implement.exception.FixedQuestionNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionnaireGroupInvalidException
import org.springframework.stereotype.Component

@Component
class QuestionnaireValidator {

    private companion object {
        const val MIN_CULTURE_FIT_QUESTIONS = 2
    }

    fun validate(questions: List<QuestionnaireQuestion>, fixedQuestionsById: Map<Long?, FixedQuestion>) {
        val cultureFitCount = questions.count { it.group == QuestionGroup.CULTURE_FIT }
        if (cultureFitCount < MIN_CULTURE_FIT_QUESTIONS) {
            throw QuestionnaireGroupInvalidException("CULTURE_FIT 그룹 문항은 최소 ${MIN_CULTURE_FIT_QUESTIONS}개 이상이어야 합니다.")
        }

        questions.filter { it.group.isFixed }.forEach { question ->
            val fixedQuestion = fixedQuestionsById[question.sourceQuestionId]
                ?: throw FixedQuestionNotFoundException("존재하지 않는 고정 질문입니다: ${question.sourceQuestionId}")

            if (fixedQuestion.category.name != question.group.name) {
                throw QuestionnaireGroupInvalidException(
                    "고정 질문의 카테고리(${fixedQuestion.category})가 그룹(${question.group})과 일치하지 않습니다.",
                )
            }
        }
    }
}
