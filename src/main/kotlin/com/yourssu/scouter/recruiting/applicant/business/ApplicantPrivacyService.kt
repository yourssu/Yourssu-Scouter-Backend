package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantDto

import org.springframework.stereotype.Service

data class ApplicantAccessScope(
    val isPrivileged: Boolean,
    val memberPartIds: Set<Long>,
)

@Service
class ApplicantPrivacyService {

    fun filterAccessibleApplicants(scope: ApplicantAccessScope, applicantDtos: List<ApplicantDto>): List<ApplicantDto> {
        if (scope.isPrivileged) {
            return applicantDtos
        }
        return applicantDtos.filter { dto -> dto.part.id in scope.memberPartIds }
    }
}
