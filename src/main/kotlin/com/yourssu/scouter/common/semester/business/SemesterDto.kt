package com.yourssu.scouter.common.semester.business

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.common.semester.implement.Term
import java.time.Year

data class SemesterDto(
    val id: Long,
    val year: Year,
    val term: Term,
) {

    companion object {
        fun from(semester: Semester): SemesterDto = SemesterDto(
            id = semester.id!!,
            year = semester.year,
            term = semester.term,
        )
    }

    fun toDomain(): Semester = Semester(
        id = id,
        year = year,
        term = term,
    )
}
