package com.yourssu.scouter.hrms.member.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester

class CompletedMember(
    val id: Long? = null,
    val member: Member,
    val completionSemester: Semester,
) : Comparable<CompletedMember> {

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
