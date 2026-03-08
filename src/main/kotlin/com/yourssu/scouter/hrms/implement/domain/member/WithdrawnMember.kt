package com.yourssu.scouter.hrms.implement.domain.member

import java.time.LocalDate

class WithdrawnMember(
    val id: Long? = null,
    val member: Member,
    val withdrawnDate: LocalDate? = null,
) : Comparable<WithdrawnMember> {

    override fun compareTo(other: WithdrawnMember): Int {
        return other.member.stateUpdatedTime.compareTo(this.member.stateUpdatedTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WithdrawnMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
