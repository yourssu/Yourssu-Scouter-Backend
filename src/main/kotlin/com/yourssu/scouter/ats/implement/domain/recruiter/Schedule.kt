package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.support.exception.InvalidScheduleException
import com.yourssu.scouter.common.implement.domain.part.Part
import java.time.LocalDateTime

data class Schedule(
    val id: Long?,
    val applicant: Applicant,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val part: Part
) {
    init {
        requireNotNull(part.id) {
            throw InvalidScheduleException("Schedule 생성 실패: part Id가 null입니다. (startTime: $startTime)")
        }
        require(endTime.isAfter(startTime)) {
            throw InvalidScheduleException("면접 종료 시간은 시작 시간 이후여야 합니다. (startTime: $startTime, endTime: $endTime)")
        }
    }

    companion object {
        fun create(
            applicant: Applicant,
            startTime: LocalDateTime,
            endTime: LocalDateTime,
            part: Part
        ): Schedule {
            validateInterviewTime(startTime)
            return Schedule(null, applicant, startTime, endTime, part)
        }

        private fun validateInterviewTime(time: LocalDateTime) {
            require(time.isAfter(LocalDateTime.now())) {
                throw InvalidScheduleException("면접 시간은 현재 시간 이후여야 합니다: $time")
            }
        }
    }

    fun getDuplicateKey(): ScheduleDuplicateKey {
        return ScheduleDuplicateKey.ofUnsafe(part.id!!, startTime)
    }
}