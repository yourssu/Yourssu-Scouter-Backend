package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import org.springframework.stereotype.Service

@Service
class ApplicantPrivacyService(
    private val memberPrivacyService: MemberPrivacyService,
) {

    fun filterAccessibleApplicants(userId: Long, applicantDtos: List<ApplicantDto>): List<ApplicantDto> {
        if (memberPrivacyService.isPrivilegedUser(userId)) {
            return applicantDtos
        }
        val memberPartIds: Set<Long> = memberPrivacyService.getMemberPartIds(userId)
        return applicantDtos.filter { dto -> dto.part.id in memberPartIds }
    }
}
