package com.yourssu.scouter.common.business.domain.semester

object SemesterConverter {

    fun convertToString(semester: SemesterDto): String {
        return "${semester.year}-${semester.semester}학기"
    }
}
