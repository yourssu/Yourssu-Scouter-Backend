package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.hrms.implement.domain.member.Member
import java.time.LocalDate

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
    val applicationDate: LocalDate,
    val applicationSemester: Semester,
    val academicSemester: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Member

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Applicant(id=$id, name='$name', email='$email', phoneNumber='$phoneNumber', age='$age', department=$department, studentId='$studentId', part=$part, state=$state, applicationDate=$applicationDate, applicationSemester=$applicationSemester, academicSemester='$academicSemester')"
    }
}
