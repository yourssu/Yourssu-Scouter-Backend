package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.InterviewRequirementDto

data class ReadInterviewRequirementItemResponse(
    val id: Long?,
    val content: String,
)

data class ReadInterviewRequirementResponse(
    val culture: List<ReadInterviewRequirementItemResponse>,
    val team: List<ReadInterviewRequirementItemResponse>,
    val job: List<ReadInterviewRequirementItemResponse>,
    val other: List<ReadInterviewRequirementItemResponse>,
) {
    companion object {
        fun from(dto: InterviewRequirementDto): ReadInterviewRequirementResponse = ReadInterviewRequirementResponse(
            culture = dto.culture.map { ReadInterviewRequirementItemResponse(it.id, it.content) },
            team = dto.team.map { ReadInterviewRequirementItemResponse(it.id, it.content) },
            job = dto.job.map { ReadInterviewRequirementItemResponse(it.id, it.content) },
            other = dto.other.map { ReadInterviewRequirementItemResponse(it.id, it.content) },
        )
    }
}
