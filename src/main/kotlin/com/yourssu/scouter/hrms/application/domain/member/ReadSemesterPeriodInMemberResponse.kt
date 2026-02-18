package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.hrms.business.domain.member.SemesterPeriodDto

data class ReadSemesterPeriodInMemberResponse(
    val startSemester: String,
    val endSemester: String,
) {

    companion object {
        fun from(semesterPeriodDto: SemesterPeriodDto) = ReadSemesterPeriodInMemberResponse(
            startSemester = SemesterConverter.convertToIntString(semesterPeriodDto.startSemester),
            endSemester = SemesterConverter.convertToIntString(semesterPeriodDto.endSemester),
        )
    }
}
