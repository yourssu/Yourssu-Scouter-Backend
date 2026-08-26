package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.applicant.application.dto.ReadApplicantAnswerResponse
import com.yourssu.scouter.recruiting.comment.application.dto.ReadDocumentCommentResponse
import com.yourssu.scouter.recruiting.evaluation.business.dto.DocumentEvaluationViewDto

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
