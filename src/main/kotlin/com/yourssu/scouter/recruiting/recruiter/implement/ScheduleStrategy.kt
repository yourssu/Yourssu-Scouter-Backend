package com.yourssu.scouter.recruiting.recruiter.implement

import com.yourssu.scouter.recruiting.recruiter.business.dto.AutoScheduleDto

interface ScheduleStrategy {

    fun getPenaltyScore(assignedSlot: Set<ScheduleDuplicateKey>, schedule: AutoScheduleDto): Long
}