package com.yourssu.scouter.recruiting.interview.implement

interface InterviewMemoRepository {

    fun findAllByQuestionnaireQuestionIdIn(questionnaireQuestionIds: List<Long>): List<InterviewMemo>

    fun replaceAll(questionnaireQuestionIds: List<Long>, memos: List<InterviewMemo>): List<InterviewMemo>
}
