package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.PartInterviewRequirementDto

data class ReadPartInterviewRequirementItemResponse(
    val id: Long?,
    val content: String,
)

data class ReadPartInterviewRequirementResponse(
    val culture: List<ReadPartInterviewRequirementItemResponse>,
    val team: List<ReadPartInterviewRequirementItemResponse>,
    val job: List<ReadPartInterviewRequirementItemResponse>,
    val other: List<ReadPartInterviewRequirementItemResponse>,
) {
    companion object {
        fun from(dto: PartInterviewRequirementDto): ReadPartInterviewRequirementResponse = ReadPartInterviewRequirementResponse(
            culture = dto.culture.map { ReadPartInterviewRequirementItemResponse(it.id, it.content) },
            team = dto.team.map { ReadPartInterviewRequirementItemResponse(it.id, it.content) },
            job = dto.job.map { ReadPartInterviewRequirementItemResponse(it.id, it.content) },
            other = dto.other.map { ReadPartInterviewRequirementItemResponse(it.id, it.content) },
        )
    }
}
