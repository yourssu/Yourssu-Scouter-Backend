package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.support.exception.InvalidScheduleException
import com.yourssu.scouter.common.implement.domain.part.Part
import java.time.Instant

data class Schedule(
    val id: Long?,
    val applicant: Applicant,
    val startTime: Instant,
    val endTime: Instant,
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
            startTime: Instant,
            endTime: Instant,
            part: Part
        ): Schedule {
            validateInterviewTime(startTime)
            return Schedule(null, applicant, startTime, endTime, part)
        }

        private fun validateInterviewTime(time: Instant) {
            require(time.isAfter(Instant.now())) {
                throw InvalidScheduleException("면접 시간은 현재 시간 이후여야 합니다: $time")
            }
        }
    }

    fun getDuplicateKey(): ScheduleDuplicateKey {
        return ScheduleDuplicateKey.ofUnsafe(part.id!!, startTime)
    }
}