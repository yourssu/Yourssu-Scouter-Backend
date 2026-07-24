package com.yourssu.scouter.recruiting.interview.implement

interface InterviewScriptRepository {

    fun findByPartId(partId: Long): InterviewScript?

    fun upsert(script: InterviewScript): InterviewScript
}
