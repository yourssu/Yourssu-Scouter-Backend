package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.recruiting.interview.implement.InterviewMemo
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoRepository
import org.springframework.stereotype.Repository

@Repository
class InterviewMemoRepositoryImpl(
    private val jpaInterviewMemoRepository: JpaInterviewMemoRepository,
) : InterviewMemoRepository {

    override fun findAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds: List<Long>): List<InterviewMemo> {
        return jpaInterviewMemoRepository.findAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds).map { it.toDomain() }
    }

    override fun replaceAll(questionnaireQuestionIds: List<Long>, memos: List<InterviewMemo>): List<InterviewMemo> {
        jpaInterviewMemoRepository.deleteAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds)

        val entities = memos.map { memo ->
            InterviewMemoEntity(
                questionnaireQuestionId = memo.questionnaireQuestionId,
                memo = memo.memo,
            )
        }

        return jpaInterviewMemoRepository.saveAll(entities).map { it.toDomain() }
    }
}
