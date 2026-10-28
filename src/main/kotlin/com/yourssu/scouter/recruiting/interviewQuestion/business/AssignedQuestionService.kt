package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionsDto
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionsCommand
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.toDraft
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionAssembler
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionDraft
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionValidator
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionResolver
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartQuestionCatalogEditor
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricLockPolicy
import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionLockedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssignedQuestionService(
    private val assignedQuestionReader: AssignedQuestionReader,
    private val assignedQuestionWriter: AssignedQuestionWriter,
    private val assignedQuestionAssembler: AssignedQuestionAssembler,
    private val assignedQuestionValidator: AssignedQuestionValidator,
    private val assignedQuestionViewAssembler: AssignedQuestionViewAssembler,
    private val partQuestionCatalogEditor: PartQuestionCatalogEditor,
    private val partCultureSelectionResolver: PartCultureSelectionResolver,
    private val partCultureSelectionReader: PartCultureSelectionReader,
    private val questionReader: QuestionReader,
    private val applicantReader: ApplicantReader,
    private val memberReader: MemberReader,
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val interviewRubricLockPolicy: InterviewRubricLockPolicy,
) {
    fun readByApplicantId(applicantId: Long): AssignedQuestionsDto {
        val applicant = applicantReader.readById(applicantId)
        val selectedCultureQuestionIds = readSelectedCultureQuestionIds(applicant)

        val questions = assignedQuestionReader.readAllByApplicantId(applicantId)
        if (questions.isEmpty()) {
            return assignedQuestionViewAssembler.assembleDefaults(applicant, selectedCultureQuestionIds)
        }

        return assignedQuestionViewAssembler.assembleSaved(applicant, questions, selectedCultureQuestionIds)
    }

    @Transactional
    fun upsert(
        applicantId: Long,
        command: SaveAssignedQuestionsCommand,
    ): AssignedQuestionsDto {
        if (interviewEvaluationReader.existsByApplicantId(applicantId)) {
            throw AssignedQuestionLockedException("해당 지원자의 면접 평가가 존재해 질문지를 수정할 수 없습니다. applicantId=$applicantId")
        }

        val applicant = applicantReader.readById(applicantId)
        val partId = applicant.part.id!!
        val semesterId = applicant.applicationSemester.id!!
        val partLocked = isPartLocked(applicant)

        val requested = command.questions.map(SaveAssignedQuestionCommand::toDraft)
        val accepted = if (partLocked) requested.filterNot(AssignedQuestionDraft::isNewPartQuestion) else requested
        val drafts = partQuestionCatalogEditor.resolveNewPartQuestions(accepted, partId, semesterId)

        val selectedCultureQuestionIds =
            if (partLocked) {
                readSelectedCultureQuestionIds(applicant)
            } else {
                partCultureSelectionResolver.replace(requested, partId, semesterId)
            }

        drafts.map { it.assignedMemberId }.distinct().forEach(memberReader::readById)
        val questions = assignedQuestionAssembler.assemble(applicantId, drafts)
            .let { if (partLocked) it + assignedQuestionAssembler.partQuestionsDroppedFromRequest(applicantId, it) else it }

        val sourceQuestionsById = questionReader
            .readAllByIdIn(questions.mapNotNull { it.sourceQuestionId })
            .associateBy { it.id }
        assignedQuestionValidator.validate(questions, sourceQuestionsById, partId, selectedCultureQuestionIds)

        partQuestionCatalogEditor.rejectSharedCatalogChanges(requested, sourceQuestionsById)
        if (!partLocked) {
            partQuestionCatalogEditor.updatePartQuestions(requested, sourceQuestionsById)
            partQuestionCatalogEditor.deleteRemovedPartQuestions(questions, partId, semesterId)
        }

        val saved = assignedQuestionWriter.replaceAll(applicantId, questions)

        return assignedQuestionViewAssembler.assembleSaved(applicant, saved, selectedCultureQuestionIds)
    }

    /**
     * 같은 파트에서 첫 면접 평가가 저장되면 그 파트+학기의 면접 루브릭이 잠긴다.
     * 잠긴 뒤에는 이미 제출된 평가가 근거한 파트 공유 질문(카탈로그)이 아직 평가받지 않은 지원자의
     * 요청으로 우회 수정되지 않도록, 카탈로그를 바꾸는 요청을 파트 단위로 막는다.
     */
    private fun isPartLocked(applicant: Applicant): Boolean =
        interviewRubricLockPolicy.isLocked(applicant.part.id!!, applicant.applicationSemester)

    private fun readSelectedCultureQuestionIds(applicant: Applicant): Set<Long> =
        partCultureSelectionReader.readSelectedQuestionIds(applicant.part.id!!, applicant.applicationSemester.id!!)
}
