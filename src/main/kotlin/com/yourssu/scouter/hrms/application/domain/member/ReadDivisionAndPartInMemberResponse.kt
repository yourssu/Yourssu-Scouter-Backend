package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.business.domain.part.PartDto
import io.swagger.v3.oas.annotations.media.Schema

data class ReadDivisionAndPartInMemberResponse(
    @field:Schema(description = "소속 구분(예: 개발, 디자인)", example = "개발")
    val division: String,
    @field:Schema(description = "파트명", example = "백엔드")
    val part: String,
) {
    companion object {
        fun from(partDto: PartDto): ReadDivisionAndPartInMemberResponse = ReadDivisionAndPartInMemberResponse(
            division = partDto.division.name,
            part = partDto.name,
        )
    }
}
