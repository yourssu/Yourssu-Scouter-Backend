package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class ReadApplicantResponse(

    val applicantId: Long,

    val division: String,

    val part: String,

    val name: String,

    val state: String,

    val applicationDate: LocalDate,

    val email: String,

    val phoneNumber: String,

    val department: String,

    val studentId: String,

    val semester: String,

    val age: String,

    val availableTimes: List<Instant>,

    val documentAverageScore: Double?,

    val interviewAverageScore: Double?,
) {

    companion object {
        fun from(applicantDto: ApplicantDto): ReadApplicantResponse = ReadApplicantResponse(
            applicantId = applicantDto.id,
            division = applicantDto.part.division.name,
            part = applicantDto.part.name,
            name = applicantDto.name,
            state = applicantDto.state.name,
            applicationDate = applicantDto.applicationDateTime.atZone(ZoneOffset.UTC).toLocalDate(),
            email = applicantDto.email,
            phoneNumber = applicantDto.phoneNumber,
            department = applicantDto.department,
            studentId = applicantDto.studentId,
            semester = applicantDto.academicSemester,
            age = applicantDto.age,
            availableTimes = applicantDto.availableTimes,
            documentAverageScore = applicantDto.documentAverageScore,
            interviewAverageScore = applicantDto.interviewAverageScore,
        )
    }
}
