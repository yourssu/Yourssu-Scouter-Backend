package com.yourssu.scouter.recruiting.question.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class QuestionnaireReader(
    private val questionnaireRepository: QuestionnaireRepository,
    private val questionnaireQuestionRepository: QuestionnaireQuestionRepository,
) {

    fun readByApplicantId(applicantId: Long): Questionnaire? {
        return questionnaireRepository.findByApplicantId(applicantId)
    }

    fun readQuestionsByApplicantId(applicantId: Long): List<QuestionnaireQuestion> {
        return questionnaireQuestionRepository.findAllByQuestionnaireId(applicantId)
    }
}
