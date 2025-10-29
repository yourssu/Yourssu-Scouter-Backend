package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.AutoScheduleDto

interface ScheduleStrategy {

    fun getPenaltyScore(assignedSlot: Set<ScheduleDuplicateKey>, schedule: AutoScheduleDto): Long
}