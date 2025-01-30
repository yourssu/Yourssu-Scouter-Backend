package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester

class InactiveMember(
    val id: Long? = null,
    val member: Member,
    val activePeriod: SemesterPeriod,
    val expectedReturnSemester: Semester,
    val inactivePeriod: SemesterPeriod,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InactiveMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "InactiveMember(id=$id, member=$member, activePeriod=$activePeriod, expectedReturnSemester=$expectedReturnSemester, inactivePeriod=$inactivePeriod)"
    }
}
