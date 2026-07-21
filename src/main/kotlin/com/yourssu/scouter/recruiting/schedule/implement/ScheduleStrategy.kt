package com.yourssu.scouter.recruiting.schedule.implement

import com.yourssu.scouter.recruiting.schedule.business.dto.AutoScheduleDto

interface ScheduleStrategy {

    fun getPenaltyScore(assignedSlot: Set<ScheduleDuplicateKey>, schedule: AutoScheduleDto): Long
}