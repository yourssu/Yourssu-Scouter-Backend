package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester

interface InterviewRubricReader {
    fun getByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric
    fun findByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric?

    /** 해당 학기에 잠긴 루브릭을 가진 파트 id. 학기 전체 잠금 판정이 파트 수만큼 조회하지 않도록 한 번에 읽는다. */
    fun findLockedPartIdsBySemester(semester: Semester): List<Long>
}
