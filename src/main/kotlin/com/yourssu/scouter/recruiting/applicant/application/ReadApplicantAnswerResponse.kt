package com.yourssu.scouter.recruiting.applicant.application

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantAnswerDto

data class ReadApplicantAnswerResponse(

    val sectionId: Long?,

    val question: String,

    val answer: String,
) {

    companion object {
        fun from(applicantAnswerDto: ApplicantAnswerDto): ReadApplicantAnswerResponse = ReadApplicantAnswerResponse(
            sectionId = applicantAnswerDto.sectionId,
            question = applicantAnswerDto.question,
            answer = applicantAnswerDto.answer,
        )
    }
}
