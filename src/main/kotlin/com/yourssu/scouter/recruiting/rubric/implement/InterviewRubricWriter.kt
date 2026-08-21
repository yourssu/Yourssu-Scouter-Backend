package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester

interface InterviewRubricWriter {
    fun save(interviewRubric: InterviewRubric): InterviewRubric
    fun deleteByPartIdAndSemester(partId: Long, semester: Semester)
}