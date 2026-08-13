package com.yourssu.scouter.masterdata.semester.application.dto

import com.yourssu.scouter.masterdata.semester.business.dto.SemesterDto

data class ReadSemesterResponse(
    val semesterId: Long,
    val year: Int,
    val term: Int,
) {

    companion object {
        fun from(semesterDto: SemesterDto): ReadSemesterResponse {
            return ReadSemesterResponse(
                semesterId = semesterDto.id,
                year = semesterDto.year.value,
                term = semesterDto.term.intValue,
            )
        }
    }
}
