package com.yourssu.scouter.ats.implement.domain.applicant

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

    fun delete(applicant: Applicant) {
        applicantRepository.deleteById(applicant.id!!)
    }
}
