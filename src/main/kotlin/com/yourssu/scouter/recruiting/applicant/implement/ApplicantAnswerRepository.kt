package com.yourssu.scouter.recruiting.applicant.implement

interface ApplicantAnswerRepository {
    fun saveAll(answers: List<ApplicantAnswer>)

    fun findAllByApplicantId(applicantId: Long): List<ApplicantAnswer>
}
