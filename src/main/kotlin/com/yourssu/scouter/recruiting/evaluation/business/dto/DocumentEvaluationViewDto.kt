package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantAnswerDto
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto

data class DocumentEvaluationViewDto(
    val answers: List<ApplicantAnswerDto>,
    val myEvaluation: DocumentEvaluationDto,
    val others: List<OtherDocumentEvaluationDto>,
    val comments: List<DocumentCommentDto>,
    val evaluatorStatuses: List<EvaluatorStatusDto>,
)
