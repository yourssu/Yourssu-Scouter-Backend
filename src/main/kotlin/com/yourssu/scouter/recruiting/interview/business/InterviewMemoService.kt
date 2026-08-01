package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.interview.business.dto.InterviewMemoDto
import com.yourssu.scouter.recruiting.interview.business.dto.SaveInterviewMemoCommand
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemo
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewMemoService(
    private val applicantReader: ApplicantReader,
    private val assignedQuestionReader: AssignedQuestionReader,
    private val interviewMemoReader: InterviewMemoReader,
    private val interviewMemoWriter: InterviewMemoWriter,
) {

    fun readByApplicantId(applicantId: Long): List<InterviewMemoDto> {
        applicantReader.readById(applicantId)

        val questionIds = readAssignedQuestionIds(applicantId)
        val memosByQuestionId = interviewMemoReader
            .readAllByAssignedQuestionIdIn(questionIds)
            .associateBy { it.assignedQuestionId }

        return questionIds.map { questionId ->
            InterviewMemoDto(
                assignedQuestionId = questionId,
                memo = memosByQuestionId[questionId]?.memo.orEmpty(),
            )
        }
    }

    @Transactional
    fun upsert(applicantId: Long, commands: List<SaveInterviewMemoCommand>): List<InterviewMemoDto> {
        applicantReader.readById(applicantId)

        val questionIds = readAssignedQuestionIds(applicantId).toSet()
        commands.forEach { command ->
            if (command.assignedQuestionId !in questionIds) {
                throw AssignedQuestionNotFoundException("해당 지원자의 면접 질문에 존재하지 않는 문항입니다: ${command.assignedQuestionId}")
            }
        }

        val memos = commands.map { command ->
            InterviewMemo(
                assignedQuestionId = command.assignedQuestionId,
                memo = command.memo,
            )
        }
        val saved = interviewMemoWriter.replaceAll(questionIds.toList(), memos)

        return saved.map { InterviewMemoDto(assignedQuestionId = it.assignedQuestionId, memo = it.memo) }
    }

    private fun readAssignedQuestionIds(applicantId: Long): List<Long> {
        return assignedQuestionReader
            .readAllByApplicantId(applicantId)
            .sortedBy { it.sortOrder }
            .map { it.id!! }
    }
}
