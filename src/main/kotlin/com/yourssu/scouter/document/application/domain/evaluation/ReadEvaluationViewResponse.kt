package com.yourssu.scouter.document.application.domain.evaluation

import com.yourssu.scouter.ats.application.domain.applicant.ReadApplicantAnswerResponse
import com.yourssu.scouter.document.application.domain.comment.ReadDocumentCommentResponse
import com.yourssu.scouter.document.business.domain.evaluation.DocumentEvaluationViewDto

data class ReadEvaluationViewResponse(
    val answers: List<ReadApplicantAnswerResponse>,
    val myEvaluation: ReadDocumentEvaluationResponse,
    val others: List<ReadOtherDocumentEvaluationResponse>,
    val comments: List<ReadDocumentCommentResponse>,
    val evaluatorStatuses: List<ReadEvaluatorStatusResponse>,
) {
    companion object {
        fun from(dto: DocumentEvaluationViewDto): ReadEvaluationViewResponse = ReadEvaluationViewResponse(
            answers = dto.answers.map(ReadApplicantAnswerResponse::from),
            myEvaluation = ReadDocumentEvaluationResponse.from(dto.myEvaluation),
            others = dto.others.map(ReadOtherDocumentEvaluationResponse::from),
            comments = dto.comments.map(ReadDocumentCommentResponse::from),
            evaluatorStatuses = dto.evaluatorStatuses.map(ReadEvaluatorStatusResponse::from),
        )
    }
}
