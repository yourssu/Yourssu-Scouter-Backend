package com.yourssu.scouter.hrms.member.business.dto

import com.yourssu.scouter.masterdata.semester.business.dto.SemesterDto
import com.yourssu.scouter.hrms.member.implement.SemesterPeriod

data class SemesterPeriodDto(
    val startSemester: SemesterDto,
    val endSemester: SemesterDto,
) {

    companion object {
        fun from(semesterPeriod: SemesterPeriod): SemesterPeriodDto = SemesterPeriodDto(
            startSemester = SemesterDto.from(semesterPeriod.startSemester),
            endSemester = SemesterDto.from(semesterPeriod.endSemester),
        )
    }
}
