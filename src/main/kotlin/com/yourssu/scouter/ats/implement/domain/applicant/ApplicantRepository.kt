package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantRepository {

    fun save(applicant: Applicant): Applicant
    fun findById(applicantId: Long): Applicant?
    fun findAll(): List<Applicant>
    fun findAllByName(name: String): List<Applicant>
    fun findAllByState(state: ApplicantState): List<Applicant>
    fun findAllBySemesterId(semesterId: Long): List<Applicant>
    fun deleteById(applicantId: Long)
}
