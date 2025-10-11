package com.yourssu.scouter.ats.implement.domain.recruiter

import java.time.LocalDateTime

class ScheduleDuplicateKey internal constructor(
    val partId: Long,
    val interviewTime: LocalDateTime
) {

    companion object {
        fun of(partId: Long, interviewTime: LocalDateTime): ScheduleDuplicateKey {
            require(partId > 0) { "Part ID는 양수여야 합니다: $partId" }
            require(interviewTime.isAfter(LocalDateTime.now())) {
                "면접 시간은 현재 시간 이후여야 합니다: $interviewTime"
            }
            return ScheduleDuplicateKey(partId, interviewTime)
        }

        internal fun ofUnsafe(partId: Long, interviewTime: LocalDateTime) =
            ScheduleDuplicateKey(partId, interviewTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScheduleDuplicateKey

        return partId == other.partId && interviewTime.isEqual(other.interviewTime)
    }

    override fun hashCode(): Int {
        var result = partId.hashCode()
        result = 31 * result + interviewTime.hashCode()
        return result
    }

    override fun toString(): String {
        return "ScheduleDuplicateKey(partId=$partId, interviewTime=$interviewTime)"
    }


}