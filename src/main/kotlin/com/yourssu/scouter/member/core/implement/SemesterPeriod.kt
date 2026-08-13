package com.yourssu.scouter.member.core.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester

class SemesterPeriod(
    val startSemester: Semester,
    val endSemester: Semester,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SemesterPeriod

        if (startSemester != other.startSemester) return false
        if (endSemester != other.endSemester) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startSemester.hashCode()
        result = 31 * result + endSemester.hashCode()
        return result
    }
}
