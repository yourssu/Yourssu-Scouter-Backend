package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantAnswerRepository {
    fun saveAll(answers: List<ApplicantAnswer>)

    fun findAllByApplicantId(applicantId: Long): List<ApplicantAnswer>
}
