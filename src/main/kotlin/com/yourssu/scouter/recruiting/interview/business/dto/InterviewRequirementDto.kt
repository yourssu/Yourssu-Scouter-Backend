package com.yourssu.scouter.recruiting.interview.business.dto

import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType

data class InterviewRequirementItemDto(
    val id: Long?,
    val content: String,
)

data class InterviewRequirementDto(
    val culture: List<InterviewRequirementItemDto>,
    val team: List<InterviewRequirementItemDto>,
    val job: List<InterviewRequirementItemDto>,
    val other: List<InterviewRequirementItemDto>,
) {
    companion object {
        fun from(requirements: List<InterviewRequirement>): InterviewRequirementDto {
            val grouped = requirements.groupBy { it.rubricType }
            return InterviewRequirementDto(
                culture = grouped[RubricGroupType.CULTURE]?.map { InterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
                team = grouped[RubricGroupType.TEAM]?.map { InterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
                job = grouped[RubricGroupType.JOB]?.map { InterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
                other = grouped[RubricGroupType.OTHER]?.map { InterviewRequirementItemDto(it.id, it.content) } ?: emptyList(),
            )
        }
    }
}
