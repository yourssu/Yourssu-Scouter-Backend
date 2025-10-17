package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ApplicantReader(
    private val applicantRepository: ApplicantRepository,
) {

    fun readById(applicantId: Long): Applicant {
        return applicantRepository.findById(applicantId) ?: throw ApplicantNotFoundException("지정한 지원자를 찾을 수 없습니다.")
    }

    fun readByIdsWithoutAvailableTimes(applicantIds: List<Long>): List<Applicant> {
        return applicantRepository.findAllByIdInWithoutAvailableTimes(applicantIds)
    }

    fun readAll(): List<Applicant> {
        return applicantRepository.findAll()
    }

    fun filterByState(applicantState: ApplicantState): List<Applicant> {
        return applicantRepository.findAllByState(applicantState)
    }
}
