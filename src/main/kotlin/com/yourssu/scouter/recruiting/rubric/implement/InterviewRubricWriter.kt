package com.yourssu.scouter.recruiting.rubric.implement

interface InterviewRubricWriter {
    fun save(interviewRubric: InterviewRubric): InterviewRubric
}