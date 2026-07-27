package com.yourssu.scouter.recruiting.question.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class QuestionnaireWriter(
    private val questionnaireRepository: QuestionnaireRepository,
    private val questionnaireQuestionRepository: QuestionnaireQuestionRepository,
) {

    fun upsert(questionnaire: Questionnaire, questions: List<QuestionnaireQuestion>): List<QuestionnaireQuestion> {
        val saved = questionnaireRepository.upsert(questionnaire)

        return questionnaireQuestionRepository.replaceAll(saved.applicantId, questions)
    }
}
