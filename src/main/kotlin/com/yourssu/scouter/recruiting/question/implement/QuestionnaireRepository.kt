package com.yourssu.scouter.recruiting.question.implement

interface QuestionnaireRepository {

    fun findByApplicantId(applicantId: Long): Questionnaire?

    fun upsert(questionnaire: Questionnaire): Questionnaire
}
