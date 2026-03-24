package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.hrms.business.domain.member.SemesterPeriodDto
import io.swagger.v3.oas.annotations.media.Schema

data class ReadSemesterPeriodInMemberResponse(
    @field:Schema(description = "시작 학기", example = "2023-2")
    val startSemester: String,
    @field:Schema(description = "종료 학기", example = "2024-2")
    val endSemester: String,
) {

    companion object {
        fun from(semesterPeriodDto: SemesterPeriodDto) = ReadSemesterPeriodInMemberResponse(
            startSemester = SemesterConverter.convertToIntString(semesterPeriodDto.startSemester),
            endSemester = SemesterConverter.convertToIntString(semesterPeriodDto.endSemester),
        )
    }
}
