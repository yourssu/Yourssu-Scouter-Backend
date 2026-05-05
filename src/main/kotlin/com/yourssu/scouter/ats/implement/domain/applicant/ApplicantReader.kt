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

    fun readByPartId(partId: Long): List<Applicant> {
        return applicantRepository.findAllByPartId(partId)
    }

    fun readByPartIdUnderReview(partId: Long): List<Applicant> {
        return applicantRepository.findAllByPartIdAndState(partId, ApplicantState.UNDER_REVIEW)
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

    // email 목록으로 배치 조회. 반환 map 에 없는 email = Applicant 미존재
    fun readByIds(ids: List<Long>): Map<Long, Applicant> = applicantRepository.findAllByIdIn(ids).associateBy { it.id!! }

    fun readByEmails(emails: List<String>): Map<String, Applicant> = applicantRepository.findAllByEmailIn(emails).associateBy { it.email }
}
