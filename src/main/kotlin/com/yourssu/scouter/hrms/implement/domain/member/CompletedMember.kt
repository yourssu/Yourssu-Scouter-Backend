package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester

class CompletedMember(
    val id: Long? = null,
    val member: Member,
    val activePeriod: SemesterPeriod,
    val isAdvisorDesired: Boolean = false,
) : Comparable<CompletedMember> {

    constructor(
        id: Long? = null,
        member: Member,
        joinSemester: Semester,
        previousSemesterBeforeStateChange: Semester,
    ) : this(
        id = id,
        member = member,
        activePeriod = SemesterPeriod(
            startSemester = joinSemester,
            endSemester = previousSemesterBeforeStateChange,
        ),
        isAdvisorDesired = false,
    )

    override fun compareTo(other: CompletedMember): Int {
        return other.member.stateUpdatedTime.compareTo(this.member.stateUpdatedTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompletedMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
