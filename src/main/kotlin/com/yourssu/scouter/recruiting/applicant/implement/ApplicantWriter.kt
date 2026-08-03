package com.yourssu.scouter.recruiting.applicant.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class ApplicantWriter(
    private val applicantRepository: ApplicantRepository,
) {

    fun write(applicant: Applicant): Applicant {
        return applicantRepository.save(applicant)
    }

    fun writeAll(applicants: List<Applicant>): List<Applicant> {
        return applicantRepository.saveAll(applicants)
    }

    fun updateAssignmentResult(applicantId: Long, assignmentResult: AssignmentResult) {
        applicantRepository.updateAssignmentResult(applicantId, assignmentResult)
    }

    fun delete(applicant: Applicant) {
        applicantRepository.deleteById(applicant.id!!)
    }
}
