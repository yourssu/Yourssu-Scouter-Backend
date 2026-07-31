package com.yourssu.scouter.recruiting.interviewQuestion.implement

interface AssignedQuestionRepository {

    fun findAllByApplicantId(applicantId: Long): List<AssignedQuestion>

    fun replaceAll(applicantId: Long, questions: List<AssignedQuestion>): List<AssignedQuestion>
}
