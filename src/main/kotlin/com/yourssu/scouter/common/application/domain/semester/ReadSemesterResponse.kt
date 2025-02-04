package com.yourssu.scouter.common.application.domain.semester

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.common.business.support.utils.SemesterConverter

data class ReadSemesterResponse(
    val semesterId: Long,
    val semester: String,
) {

    companion object {
        fun from(semesterDto: SemesterDto): ReadSemesterResponse {
            return ReadSemesterResponse(
                semesterId = semesterDto.id,
                semester = SemesterConverter.convertToString(semesterDto)
            )
        }
    }
}
