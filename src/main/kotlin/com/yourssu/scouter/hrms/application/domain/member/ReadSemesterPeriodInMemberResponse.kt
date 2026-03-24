package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.hrms.business.domain.member.SemesterPeriodDto
import io.swagger.v3.oas.annotations.media.Schema

data class ReadSemesterPeriodInMemberResponse(
    @field:Schema(description = "시작 학기(yy-term, 조회·저장 동일 형식)", example = "23-2")
    val startSemester: String,
    @field:Schema(description = "종료 학기(yy-term, 조회·저장 동일 형식)", example = "24-2")
    val endSemester: String,
) {

    companion object {
        fun from(semesterPeriodDto: SemesterPeriodDto) = ReadSemesterPeriodInMemberResponse(
            startSemester = SemesterConverter.convertToIntString(semesterPeriodDto.startSemester),
            endSemester = SemesterConverter.convertToIntString(semesterPeriodDto.endSemester),
        )
    }
}
