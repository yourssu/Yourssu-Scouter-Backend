package com.yourssu.scouter.recruiting.applicant.business.dto

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswer

/**
 * 어드민 전용. 개별 답변을 수정·삭제하려면 id 가 필요하다.
 * 기존 ApplicantAnswerDto 는 REST 응답(ReadApplicantAnswerResponse, DocumentEvaluationViewDto)이 쓰고 있어 건드리지 않는다.
 */
data class AdminApplicantAnswerDto(
    val id: Long,
    val sectionId: Long?,
    val question: String,
    val answer: String,
) {

    companion object {
        fun from(applicantAnswer: ApplicantAnswer): AdminApplicantAnswerDto = AdminApplicantAnswerDto(
            id = applicantAnswer.id!!,
            sectionId = applicantAnswer.sectionId,
            question = applicantAnswer.question,
            answer = applicantAnswer.answer,
        )
    }
}
