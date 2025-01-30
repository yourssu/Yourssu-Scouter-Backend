package com.yourssu.scouter.ats.application.domain.applicant

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.business.support.utils.ApplicantStateConverter
import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import java.time.LocalDate

data class ReadApplicantResponse(

    val applicantId: Long,

    val division: String,

    val part: String,

    val name: String,

    val state: String,

    @JsonFormat(pattern = "yyyy.MM.dd")
    val applicationDate: LocalDate,

    val email: String,

    val phoneNumber: String,

    val department: String,

    val studentId: String,

    val semester: String,

    val age: String,
) {

    companion object {
        fun from(applicantDto: ApplicantDto): ReadApplicantResponse = ReadApplicantResponse(
            applicantId = applicantDto.id,
            division = applicantDto.part.division.name,
            part = applicantDto.part.name,
            name = applicantDto.name,
            state = ApplicantStateConverter.convertToString(applicantDto.state),
            applicationDate = applicantDto.applicationDate,
            email = applicantDto.email,
            phoneNumber = applicantDto.phoneNumber,
            department = applicantDto.department.name,
            studentId = applicantDto.studentId,
            semester = SemesterConverter.convertToString(applicantDto.applicationSemester),
            age = applicantDto.age,
        )
    }
}
