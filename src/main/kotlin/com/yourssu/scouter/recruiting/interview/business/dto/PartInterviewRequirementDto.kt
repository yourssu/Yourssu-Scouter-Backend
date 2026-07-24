package com.yourssu.scouter.recruiting.interview.business.dto

import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement

data class PartInterviewRequirementDto(
    val culture: String?,
    val team: String?,
    val job: String?,
) {
    companion object {
        fun from(requirement: PartInterviewRequirement?): PartInterviewRequirementDto = PartInterviewRequirementDto(
            culture = requirement?.culture,
            team = requirement?.team,
            job = requirement?.job,
        )
    }
}
