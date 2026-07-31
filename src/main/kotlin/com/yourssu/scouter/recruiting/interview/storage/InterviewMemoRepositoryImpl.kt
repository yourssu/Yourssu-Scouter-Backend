package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.recruiting.interview.implement.InterviewMemo
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoRepository
import org.springframework.stereotype.Repository

@Repository
class InterviewMemoRepositoryImpl(
    private val jpaInterviewMemoRepository: JpaInterviewMemoRepository,
) : InterviewMemoRepository {

    override fun findAllByAssignedQuestionIdIn(assignedQuestionIds: List<Long>): List<InterviewMemo> {
        return jpaInterviewMemoRepository.findAllByAssignedQuestionIdIn(assignedQuestionIds).map { it.toDomain() }
    }

    override fun replaceAll(assignedQuestionIds: List<Long>, memos: List<InterviewMemo>): List<InterviewMemo> {
        jpaInterviewMemoRepository.deleteAllByAssignedQuestionIdIn(assignedQuestionIds)

        val entities = memos.map { memo ->
            InterviewMemoEntity(
                assignedQuestionId = memo.assignedQuestionId,
                memo = memo.memo,
            )
        }

        return jpaInterviewMemoRepository.saveAll(entities).map { it.toDomain() }
    }
}
