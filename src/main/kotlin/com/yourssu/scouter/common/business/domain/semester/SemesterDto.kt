package com.yourssu.scouter.common.business.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Semester

data class SemesterDto(
    val id: Long,
    val year: Int,
    val semester: Int,
) {

    companion object {
        fun from(semester: Semester): SemesterDto = SemesterDto(
            id = semester.id!!,
            year = semester.year,
            semester = semester.semester,
        )
    }
}
