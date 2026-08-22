package com.yourssu.scouter.recruiting.interviewQuestion.implement

interface PartCultureSelectionRepository {

    fun findSelectedQuestionIds(partId: Long, semesterId: Long): Set<Long>

    fun replaceSelection(partId: Long, semesterId: Long, questionIds: List<Long>)

    fun deleteAllByQuestionIdIn(questionIds: List<Long>)
}
