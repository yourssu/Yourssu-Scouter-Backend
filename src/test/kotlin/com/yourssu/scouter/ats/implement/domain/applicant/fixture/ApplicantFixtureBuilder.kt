package com.yourssu.scouter.ats.implement.domain.applicant.fixture

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.common.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import java.time.Instant

class ApplicantFixtureBuilder {

    private var id: Long? = null
    private var name = "홍길동"
    private var email = "test@example.com"
    private var phoneNumber = "010-1234-5678"
    private var age = "22"
    private var department = "컴퓨터공학부"
    private var studentId = "20210001"
    private var part = PartFixtureBuilder().build()
    private var state = ApplicantState.UNDER_REVIEW
    private var applicationDateTime = Instant.parse("2025-09-24T12:30:00Z")
    private var applicationSemester = SemesterFixtureBuilder().build()
    private var academicSemester = "2-2"
    private var availableTimes = emptyList<Instant>()

    fun id(id: Long) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun email(email: String) = apply { this.email = email }
    fun phoneNumber(phoneNumber: String) = apply { this.phoneNumber = phoneNumber }
    fun age(age: String) = apply { this.age = age }
    fun department(department: String) = apply { this.department = department }
    fun studentId(studentId: String) = apply { this.studentId = studentId }
    fun part(part: Part) = apply { this.part = part }
    fun state(state: ApplicantState) = apply { this.state = state }
    fun applicationDateTime(applicationDateTime: Instant) =
        apply { this.applicationDateTime = applicationDateTime }

    fun applicationSemester(applicationSemester: Semester) = apply { this.applicationSemester = applicationSemester }
    fun academicSemester(academicSemester: String) = apply { this.academicSemester = academicSemester }
    fun availableTimes(availableTimes: List<Instant>) = apply { this.availableTimes = availableTimes }

    fun build() = Applicant(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        age = age,
        department = department,
        studentId = studentId,
        part = part,
        state = state,
        applicationDateTime = applicationDateTime,
        applicationSemester = applicationSemester,
        academicSemester = academicSemester,
        availableTimes = availableTimes
    )
}