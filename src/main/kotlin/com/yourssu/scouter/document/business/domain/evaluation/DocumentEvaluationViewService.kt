package com.yourssu.scouter.document.business.domain.evaluation

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantService
import com.yourssu.scouter.document.business.domain.comment.DocumentCommentService
import org.springframework.stereotype.Service

@Service
class DocumentEvaluationViewService(
    private val applicantService: ApplicantService,
    private val documentEvaluationService: DocumentEvaluationService,
    private val documentCommentService: DocumentCommentService,
) {

    fun read(applicantId: Long, viewerUserId: Long): DocumentEvaluationViewDto {
        return DocumentEvaluationViewDto(
            answers = applicantService.readAnswersByApplicantId(applicantId),
            myEvaluation = documentEvaluationService.readMy(applicantId, viewerUserId),
            others = documentEvaluationService.readOthers(applicantId, viewerUserId),
            comments = documentCommentService.readAllByApplicantId(applicantId),
            evaluatorStatuses = documentEvaluationService.readStatuses(applicantId),
        )
    }
}
