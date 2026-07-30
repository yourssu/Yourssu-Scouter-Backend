package com.yourssu.scouter.recruiting.interview.business.dto

import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType

data class PartInterviewRequirementItemDto(
    val id: Long?,
    val content: String,
)

data class PartInterviewRequirementDto(
    val culture: List<PartInterviewRequirementItemDto>,
    val team: List<PartInterviewRequirementItemDto>,
    val job: List<PartInterviewRequirementItemDto>,
    val other: List<PartInterviewRequirementItemDto>,
) {
    companion object {
        fun from(requirements: List<PartInterviewRequirement>): PartInterviewRequirementDto {
            val grouped = requirements.groupBy { it.rubricType }
            return PartInterviewRequirementDto(
                culture = grouped[RubricGroupType.CULTURE]?.map { PartInterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
                team = grouped[RubricGroupType.TEAM]?.map { PartInterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
                job = grouped[RubricGroupType.JOB]?.map { PartInterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
                other = grouped[RubricGroupType.OTHER]?.map { PartInterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
            )
        }
    }
}
