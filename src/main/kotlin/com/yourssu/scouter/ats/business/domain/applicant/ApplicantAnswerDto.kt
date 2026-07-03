package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantAnswer

data class ApplicantAnswerDto(
    val question: String,
    val answer: String,
) {
    companion object {
        fun from(applicantAnswer: ApplicantAnswer): ApplicantAnswerDto = ApplicantAnswerDto(
            question = applicantAnswer.question,
            answer = applicantAnswer.answer,
        )
    }
}
