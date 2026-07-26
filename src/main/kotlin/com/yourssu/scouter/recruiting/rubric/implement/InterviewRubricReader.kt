package com.yourssu.scouter.recruiting.rubric.implement

interface InterviewRubricReader {
    fun getByPartIdAndSemester(partId: Long, semester: String): InterviewRubric
    fun findByPartIdAndSemester(partId: Long, semester: String): InterviewRubric?
}
