package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.recruiting.applicant.business.dto.AdminApplicantAnswerDto
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswer
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswerReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswerWriter
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentWriter
import com.yourssu.scouter.recruiting.evaluation.business.DocumentEvaluationService
import com.yourssu.scouter.recruiting.evaluation.business.InterviewEvaluationService
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionWriter
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionReader
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 어드민 화면 전용 지원자 작업. 기존 ApplicantService 와 REST API 의 동작은 그대로 두고 여기에만 새 경로를 둔다.
 *
 * 삭제는 DB 의 ON DELETE CASCADE 에 기대지 않고 직접 자식 행을 지운다.
 * document_evaluation, document_comment, assigned_question 은 prod(MySQL)와 local(H2)의 외래 키가 서로 다르다.
 * cascade 에 기대면 local 에서만 통과하고 prod 에서 다르게 동작한다.
 */
@Service
class AdminApplicantService(
    private val applicantService: ApplicantService,
    private val applicantReader: ApplicantReader,
    private val applicantAnswerReader: ApplicantAnswerReader,
    private val applicantAnswerWriter: ApplicantAnswerWriter,
    private val documentSectionReader: DocumentSectionReader,
    private val documentEvaluationService: DocumentEvaluationService,
    private val interviewEvaluationService: InterviewEvaluationService,
    private val documentCommentWriter: DocumentCommentWriter,
    private val scheduleWriter: ScheduleWriter,
    private val assignedQuestionWriter: AssignedQuestionWriter,
) {

    fun readAnswers(applicantId: Long): List<AdminApplicantAnswerDto> {
        applicantReader.readById(applicantId)

        return applicantAnswerReader.readAllByApplicantId(applicantId).map(AdminApplicantAnswerDto::from)
    }

    fun readSections(partId: Long): List<DocumentSection> {
        return documentSectionReader.readAllByPartId(partId)
    }

    @Transactional
    fun addAnswer(applicantId: Long, question: String, answer: String, sectionId: Long?): AdminApplicantAnswerDto {
        applicantReader.readById(applicantId)

        val written = applicantAnswerWriter.write(
            ApplicantAnswer(
                applicantId = applicantId,
                sectionId = sectionId,
                question = question,
                answer = answer,
            ),
        )

        return AdminApplicantAnswerDto.from(written)
    }

    @Transactional
    fun updateAnswer(answerId: Long, question: String, answer: String, sectionId: Long?): AdminApplicantAnswerDto {
        val target: ApplicantAnswer = applicantAnswerReader.readById(answerId)

        val written = applicantAnswerWriter.write(
            ApplicantAnswer(
                id = target.id,
                applicantId = target.applicantId,
                sectionId = sectionId,
                question = question,
                answer = answer,
            ),
        )

        return AdminApplicantAnswerDto.from(written)
    }

    @Transactional
    fun deleteAnswer(answerId: Long) {
        val target: ApplicantAnswer = applicantAnswerReader.readById(answerId)

        applicantAnswerWriter.delete(target.id!!)
    }

    /**
     * 지원자와 그 지원자를 참조하는 행을 모두 지운다. 자식부터 지워야 외래 키에 막히지 않는다.
     * 면접 평가를 지우면 평가가 하나도 남지 않은 면접 루브릭의 잠금도 함께 풀린다.
     */
    @Transactional
    fun deleteWithRelatedData(applicantId: Long) {
        applicantReader.readById(applicantId)

        interviewEvaluationService.deleteAllByApplicantIdAndUnlockRubrics(applicantId)
        documentEvaluationService.deleteAllByApplicantId(applicantId)
        documentCommentWriter.deleteAllByApplicantId(applicantId)
        scheduleWriter.deleteAllByApplicantId(applicantId)
        assignedQuestionWriter.deleteAllByApplicantId(applicantId)
        applicantAnswerWriter.deleteAllByApplicantId(applicantId)

        applicantService.deleteById(applicantId)
    }
}
