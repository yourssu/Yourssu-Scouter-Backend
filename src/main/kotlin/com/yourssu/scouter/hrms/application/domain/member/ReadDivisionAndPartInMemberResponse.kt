package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.business.domain.part.PartDto

data class ReadDivisionAndPartInMemberResponse(

    val division: String,
    val part: String,
) {
    companion object {
        fun from(partDto: PartDto): ReadDivisionAndPartInMemberResponse = ReadDivisionAndPartInMemberResponse(
            division = partDto.division.name,
            part = partDto.name,
        )
    }
}
