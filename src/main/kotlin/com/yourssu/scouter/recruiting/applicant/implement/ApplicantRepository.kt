package com.yourssu.scouter.recruiting.applicant.implement

interface ApplicantRepository {
    fun save(applicant: Applicant): Applicant

    fun saveAll(applicants: List<Applicant>): List<Applicant>

    fun findById(applicantId: Long): Applicant?

    fun findAll(): List<Applicant>

    fun findAllByState(state: ApplicantState): List<Applicant>

    fun findAllByIdInWithoutAvailableTimes(applicantIds: List<Long>): List<Applicant>

    fun deleteById(applicantId: Long)

    fun findAllByPartId(partId: Long): List<Applicant>

    fun findAllByPartIdAndState(
        partId: Long,
        state: ApplicantState,
    ): List<Applicant>

    fun findAllByIdIn(applicantIds: List<Long>): List<Applicant>

    fun findAllByEmailIn(emails: List<String>): List<Applicant>

    fun updateAssignmentResult(applicantId: Long, assignmentResult: AssignmentResult)
}
