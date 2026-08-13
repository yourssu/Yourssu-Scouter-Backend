package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester

interface InterviewRubricReader {
    fun getByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric
    fun findByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric?
}
