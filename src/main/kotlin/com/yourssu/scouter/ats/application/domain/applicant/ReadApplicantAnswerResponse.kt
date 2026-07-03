package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantAnswerDto

data class ReadApplicantAnswerResponse(

    val question: String,

    val answer: String,
) {

    companion object {
        fun from(applicantAnswerDto: ApplicantAnswerDto): ReadApplicantAnswerResponse = ReadApplicantAnswerResponse(
            question = applicantAnswerDto.question,
            answer = applicantAnswerDto.answer,
        )
    }
}
