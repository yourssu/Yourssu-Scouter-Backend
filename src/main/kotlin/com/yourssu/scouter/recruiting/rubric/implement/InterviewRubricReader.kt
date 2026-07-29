package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.common.semester.implement.Semester

interface InterviewRubricReader {
    fun getByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric
    fun findByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric?
}
