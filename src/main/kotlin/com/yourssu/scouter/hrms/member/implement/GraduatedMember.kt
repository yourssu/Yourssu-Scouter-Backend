package com.yourssu.scouter.hrms.member.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester

class GraduatedMember(
    val id: Long? = null,
    val member: Member,
    val activePeriod: SemesterPeriod,
    val isAdvisorDesired: Boolean,
) : Comparable<GraduatedMember> {

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

    override fun compareTo(other: GraduatedMember): Int {
        return other.member.stateUpdatedTime.compareTo(this.member.stateUpdatedTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraduatedMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
