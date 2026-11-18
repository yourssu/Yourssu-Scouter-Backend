package com.yourssu.scouter.recruiting.applicant.implement

interface ApplicantAnswerRepository {
    fun saveAll(answers: List<ApplicantAnswer>)

    fun save(answer: ApplicantAnswer): ApplicantAnswer

    fun findAllByApplicantId(applicantId: Long): List<ApplicantAnswer>

    fun findById(answerId: Long): ApplicantAnswer?

    fun deleteById(answerId: Long)

    fun deleteAllByApplicantId(applicantId: Long)
}
