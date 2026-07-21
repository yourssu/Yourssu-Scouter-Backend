package com.yourssu.scouter.recruiting.applicant.business.dto

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswer

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
