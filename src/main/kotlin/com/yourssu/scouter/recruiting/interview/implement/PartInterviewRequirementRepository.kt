package com.yourssu.scouter.recruiting.interview.implement

interface PartInterviewRequirementRepository {

    fun findByPartId(partId: Long): PartInterviewRequirement?

    fun upsert(requirement: PartInterviewRequirement): PartInterviewRequirement
}
