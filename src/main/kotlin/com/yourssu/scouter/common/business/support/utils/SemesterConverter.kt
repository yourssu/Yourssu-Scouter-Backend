package com.yourssu.scouter.common.business.support.utils

import com.yourssu.scouter.common.business.domain.semester.SemesterDto

object SemesterConverter {

    fun convertToString(semester: SemesterDto): String {
        val year: Int = semester.year.value % 100
        val term: Int = semester.term.intValue

        return "${year}-${term}학기"
    }

    fun convertToIntString(semester: SemesterDto): String {
        val year: Int = semester.year.value
        val term: Int = semester.term.intValue

        return "${year}-${term}"
    }
}
