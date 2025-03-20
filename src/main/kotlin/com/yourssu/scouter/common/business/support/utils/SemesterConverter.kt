package com.yourssu.scouter.common.business.support.utils

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.Term
import java.time.LocalDate
import java.time.Year

object SemesterConverter {

    fun convertToIntString(semester: SemesterDto): String {
        return convertToIntString(semester.year, semester.term)
    }

    private fun convertToIntString(year: Year, term: Term): String {
        val yearValue: Int = year.value % 1000
        val termValue: Int = term.intValue

        return "${yearValue}${Semester.DELIMITER}${termValue}"
    }

    fun convertToStringWithTermLabel(semester: SemesterDto): String {
        return "${convertToIntString(semester)}학기"
    }

    fun convertToIntString(date: LocalDate): String {
        val semester = Semester.of(date)

        return convertToIntString(semester.year, semester.term)
    }
}
