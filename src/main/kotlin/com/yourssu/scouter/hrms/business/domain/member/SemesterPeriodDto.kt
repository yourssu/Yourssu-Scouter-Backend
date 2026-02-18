package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.hrms.implement.domain.member.SemesterPeriod

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
