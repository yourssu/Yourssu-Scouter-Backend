package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester

class GraduatedMember(
    val id: Long? = null,
    val member: Member,
    val activePeriod: SemesterPeriod,
    val isAdvisorDesired: Boolean,
) {

    constructor(
        member: Member,
        joinSemester: Semester,
        previousSemesterBeforeStateChange: Semester,
    ) : this(
        member = member,
        activePeriod = SemesterPeriod(
            startSemester = joinSemester,
            endSemester = previousSemesterBeforeStateChange,
        ),
        isAdvisorDesired = false,
    )

    fun updateAdvisorDesired(isAdvisorDesired: Boolean): GraduatedMember {
        return GraduatedMember(
            id = id,
            member = member,
            activePeriod = activePeriod,
            isAdvisorDesired = isAdvisorDesired,
        )
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

    override fun toString(): String {
        return "GraduatedMember(id=$id, member=$member, activePeriod=$activePeriod, isAdvisorDesired=$isAdvisorDesired)"
    }
}
