package com.yourssu.scouter.ats.implement.domain.recruiter

import java.time.Instant

class ScheduleDuplicateKey internal constructor(
    val partId: Long,
    val startTime: Instant
) {

    companion object {
        fun of(partId: Long, startTime: Instant): ScheduleDuplicateKey {
            require(partId > 0) { "Part ID는 양수여야 합니다: $partId" }
            require(startTime.isAfter(Instant.now())) {
                "면접 시간은 현재 시간 이후여야 합니다: $startTime"
            }
            return ScheduleDuplicateKey(partId, startTime)
        }

        internal fun ofUnsafe(partId: Long, startTime: Instant) =
            ScheduleDuplicateKey(partId, startTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScheduleDuplicateKey

        return partId == other.partId && startTime.equals(other.startTime)
    }

    override fun hashCode(): Int {
        var result = partId.hashCode()
        result = 31 * result + startTime.hashCode()
        return result
    }

    override fun toString(): String {
        return "ScheduleDuplicateKey(partId=$partId, startTime=$startTime)"
    }


}