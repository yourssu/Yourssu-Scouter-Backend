package com.yourssu.scouter.document.business.domain.evaluation

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantAnswerDto
import com.yourssu.scouter.document.business.domain.comment.DocumentCommentDto

data class DocumentEvaluationViewDto(
    val answers: List<ApplicantAnswerDto>,
    val myEvaluation: DocumentEvaluationDto,
    val others: List<OtherDocumentEvaluationDto>,
    val comments: List<DocumentCommentDto>,
    val evaluatorStatuses: List<EvaluatorStatusDto>,
)
