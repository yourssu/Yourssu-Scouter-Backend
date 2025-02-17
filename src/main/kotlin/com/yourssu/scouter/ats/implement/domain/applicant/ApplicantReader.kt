package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.common.implement.domain.semester.Semester
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ApplicantReader(
    private val applicantRepository: ApplicantRepository,
) {

    fun readById(applicantId: Long): Applicant =
        applicantRepository.findById(applicantId) ?: throw ApplicantNotFoundException("지정한 지원자를 찾을 수 없습니다.")

    fun readAll(): List<Applicant> = applicantRepository.findAll()
    fun searchAlByName(name: String): List<Applicant> = applicantRepository.findAllByName(name)
    fun filterByState(applicantState: ApplicantState): List<Applicant> {
        return applicantRepository.findAllByState(applicantState)
    }

    fun filterBySemester(semester: Semester): List<Applicant> = applicantRepository.findAllBySemesterId(semester.id!!)
}
