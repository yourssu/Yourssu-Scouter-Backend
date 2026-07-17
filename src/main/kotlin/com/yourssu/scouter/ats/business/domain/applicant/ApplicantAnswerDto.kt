package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantAnswer

data class ApplicantAnswerDto(
    val sectionId: Long?,
    val question: String,
    val answer: String,
) {
    companion object {
        fun from(applicantAnswer: ApplicantAnswer): ApplicantAnswerDto = ApplicantAnswerDto(
            sectionId = applicantAnswer.sectionId,
            question = applicantAnswer.question,
            answer = applicantAnswer.answer,
        )
    }
}
