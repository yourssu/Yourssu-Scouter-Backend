package com.yourssu.scouter.masterdata.semester.business.dto

import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.Term
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

    override fun toString(): String {
        return year.value.toString() + "-" + term.intValue.toString()
    }
}

