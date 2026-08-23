package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantSyncMappingNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ApplicantSyncMappingReader(
    private val applicantSyncMappingRepository: ApplicantSyncMappingRepository,
) {

    fun readAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncMapping> {
        return applicantSyncMappingRepository.findAllByApplicationSemesterId(applicationSemesterId)
    }

    fun readByFormId(formId: String): ApplicantSyncMapping {
        return applicantSyncMappingRepository.findByFormId(formId)
            ?: throw ApplicantSyncMappingNotFoundException("해당 formId에 대한 동기화 매핑이 존재하지 않습니다: $formId")
    }
}
