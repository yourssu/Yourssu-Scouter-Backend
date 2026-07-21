package com.yourssu.scouter.common.semester.application

import com.yourssu.scouter.common.semester.business.SemesterDto
import com.yourssu.scouter.common.support.business.utils.SemesterConverter

data class ReadSemesterResponse(
    val semesterId: Long,
    val semester: String,
) {

    companion object {
        fun from(semesterDto: SemesterDto): ReadSemesterResponse {
            return ReadSemesterResponse(
                semesterId = semesterDto.id,
                semester = SemesterConverter.convertToStringWithTermLabel(semesterDto)
            )
        }
    }
}
