package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.PartInterviewRequirementDto
import com.yourssu.scouter.recruiting.interview.business.dto.PartInterviewRequirementItemDto

data class ReadPartInterviewRequirementResponse(
    val culture: List<PartInterviewRequirementItemDto>,
    val team: List<PartInterviewRequirementItemDto>,
    val job: List<PartInterviewRequirementItemDto>,
    val other: List<PartInterviewRequirementItemDto>,
) {
    companion object {
        fun from(dto: PartInterviewRequirementDto): ReadPartInterviewRequirementResponse = ReadPartInterviewRequirementResponse(
            culture = dto.culture,
            team = dto.team,
            job = dto.job,
            other = dto.other,
        )
    }
}
