package com.yourssu.scouter.recruiting.applicant.application.dto

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantDto
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

    val applicationSemester: String,

    val email: String,

    val phoneNumber: String,

    val department: String,

    val studentId: String,

    val academicSemester: String,

    val age: String,

    val availableTimes: List<Instant>,

    val documentAverageScore: Double?,

    val interviewAverageScore: Double?,

    val partId: Long,
) {

    companion object {
        fun from(applicantDto: ApplicantDto): ReadApplicantResponse = ReadApplicantResponse(
            applicantId = applicantDto.id,
            division = applicantDto.part.division.name,
            part = applicantDto.part.name,
            name = applicantDto.name,
            state = applicantDto.state.name,
            applicationDate = applicantDto.applicationDateTime.atZone(ZoneOffset.UTC).toLocalDate(),
            applicationSemester = applicantDto.applicationSemester.toString(),
            email = applicantDto.email,
            phoneNumber = applicantDto.phoneNumber,
            department = applicantDto.department,
            studentId = applicantDto.studentId,
            academicSemester = applicantDto.academicSemester,
            age = applicantDto.age,
            availableTimes = applicantDto.availableTimes,
            documentAverageScore = applicantDto.documentAverageScore,
            interviewAverageScore = applicantDto.interviewAverageScore,
            partId = applicantDto.part.id,
        )
    }
}
