package com.yourssu.scouter.recruiting.interview.implement

interface InterviewMemoRepository {

    fun findAllByAssignedQuestionIdIn(assignedQuestionIds: List<Long>): List<InterviewMemo>

    fun replaceAll(assignedQuestionIds: List<Long>, memos: List<InterviewMemo>): List<InterviewMemo>
}
