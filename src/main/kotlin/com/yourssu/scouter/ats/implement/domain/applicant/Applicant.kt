package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.hrms.implement.domain.member.Member
import java.time.LocalDateTime

class Applicant(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val age: String,
    val department: String,
    val studentId: String,
    val part: Part,
    val state: ApplicantState,
    val applicationDateTime: LocalDateTime,
    val applicationSemester: Semester,
    val academicSemester: String,
) : Comparable<Applicant> {

    override fun compareTo(other: Applicant): Int {
        val partCompare = this.part.compareTo(other.part)
        if (partCompare != 0) {
            return partCompare
        }

        return this.applicationDateTime.compareTo(other.applicationDateTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Member

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
