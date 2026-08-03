package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.common.part.implement.Part
import com.yourssu.scouter.common.semester.implement.Semester
import java.time.Instant

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
    val assignmentResult: AssignmentResult,
    val applicationDateTime: Instant,
    val applicationSemester: Semester,
    val academicSemester: String,
    val availableTimes: List<Instant>,
) : Comparable<Applicant> {

    companion object {
        val ATTRIBUTE_KEYS = setOf(
            "applicant.name", "applicant.email", "applicant.phoneNumber",
            "applicant.department", "applicant.studentId", "applicant.age",
            "applicant.academicSemester", "applicant.part.name",
        )
    }

    fun toAttributeMap(): Map<String, String> = mapOf(
        "applicant.name" to name,
        "applicant.email" to email,
        "applicant.phoneNumber" to phoneNumber,
        "applicant.department" to department,
        "applicant.studentId" to studentId,
        "applicant.age" to age,
        "applicant.academicSemester" to academicSemester,
        "applicant.part.name" to part.name,
    )

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

        other as Applicant

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
