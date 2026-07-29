package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.interview.business.dto.InterviewMemoDto
import com.yourssu.scouter.recruiting.interview.business.dto.SaveInterviewMemoCommand
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemo
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoWriter
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireReader
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionnaireQuestionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewMemoService(
    private val applicantReader: ApplicantReader,
    private val questionnaireReader: QuestionnaireReader,
    private val interviewMemoReader: InterviewMemoReader,
    private val interviewMemoWriter: InterviewMemoWriter,
) {

    fun readByApplicantId(applicantId: Long): List<InterviewMemoDto> {
        applicantReader.readById(applicantId)

        val questionIds = readQuestionnaireQuestionIds(applicantId)
        val memosByQuestionId = interviewMemoReader
            .readAllByQuestionnaireQuestionIdIn(questionIds)
            .associateBy { it.questionnaireQuestionId }

        return questionIds.map { questionId ->
            InterviewMemoDto(
                questionnaireQuestionId = questionId,
                memo = memosByQuestionId[questionId]?.memo.orEmpty(),
            )
        }
    }

    @Transactional
    fun upsert(applicantId: Long, commands: List<SaveInterviewMemoCommand>): List<InterviewMemoDto> {
        applicantReader.readById(applicantId)

        val questionIds = readQuestionnaireQuestionIds(applicantId).toSet()
        commands.forEach { command ->
            if (command.questionnaireQuestionId !in questionIds) {
                throw QuestionnaireQuestionNotFoundException("해당 지원자의 질문지에 존재하지 않는 문항입니다: ${command.questionnaireQuestionId}")
            }
        }

        val memos = commands.map { command ->
            InterviewMemo(
                questionnaireQuestionId = command.questionnaireQuestionId,
                memo = command.memo,
            )
        }
        val saved = interviewMemoWriter.replaceAll(questionIds.toList(), memos)

        return saved.map { InterviewMemoDto(questionnaireQuestionId = it.questionnaireQuestionId, memo = it.memo) }
    }

    private fun readQuestionnaireQuestionIds(applicantId: Long): List<Long> {
        return questionnaireReader
            .readQuestionsByApplicantId(applicantId)
            .sortedBy { it.sortOrder }
            .map { it.id!! }
    }
}
