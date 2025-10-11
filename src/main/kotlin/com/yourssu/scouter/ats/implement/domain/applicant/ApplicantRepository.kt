package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantRepository {

    fun save(applicant: Applicant): Applicant
    fun saveAll(applicants: List<Applicant>)
    fun findById(applicantId: Long): Applicant?
    fun findAll(): List<Applicant>
    fun findAllByState(state: ApplicantState): List<Applicant>
    fun findAllByIdIn(applicantIds: List<Long>): List<Applicant>
    fun deleteById(applicantId: Long)
}
