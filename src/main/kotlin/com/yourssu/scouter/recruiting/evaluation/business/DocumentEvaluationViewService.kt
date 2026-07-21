package com.yourssu.scouter.recruiting.evaluation.business

import com.yourssu.scouter.recruiting.evaluation.business.dto.DocumentEvaluationViewDto

import com.yourssu.scouter.recruiting.applicant.business.ApplicantService
import com.yourssu.scouter.recruiting.comment.business.DocumentCommentService
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
