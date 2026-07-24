package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.PartInterviewRequirementDto

data class ReadPartInterviewRequirementResponse(
    val culture: String?,
    val team: String?,
    val job: String?,
) {
    companion object {
        fun from(dto: PartInterviewRequirementDto): ReadPartInterviewRequirementResponse = ReadPartInterviewRequirementResponse(
            culture = dto.culture,
            team = dto.team,
            job = dto.job,
        )
    }
}
