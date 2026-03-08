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
    val part: Part,
    val locationType: ScheduleLocationType = ScheduleLocationType.CLUB_ROOM,
    val locationDetail: String? = null,
) {
    init {
        requireNotNull(part.id) {
            throw InvalidScheduleException("Schedule 생성 실패: part Id가 null입니다. (startTime: $startTime)")
        }
        require(endTime.isAfter(startTime)) {
            throw InvalidScheduleException(message = "면접 종료 시각은 시작 시간 이후여야 합니다. (startTime: $startTime, endTime: $endTime)")
        }
    }

    companion object {
        fun create(
            applicant: Applicant,
            startTime: Instant,
            endTime: Instant,
            part: Part,
            locationType: ScheduleLocationType = ScheduleLocationType.CLUB_ROOM,
            locationDetail: String? = null,
        ): Schedule {
            return Schedule(null, applicant, startTime, endTime, part, locationType, locationDetail)
        }
    }

    fun getDuplicateKey(): ScheduleDuplicateKey {
        return ScheduleDuplicateKey.ofUnsafe(part.id!!, startTime)
    }
}
