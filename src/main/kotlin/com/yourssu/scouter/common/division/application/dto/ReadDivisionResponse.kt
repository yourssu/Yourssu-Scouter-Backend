package com.yourssu.scouter.common.division.application.dto

import com.yourssu.scouter.common.division.business.dto.DivisionDto

data class ReadDivisionResponse(
    val divisionId: Long,
    val divisionName: String,
) {

    companion object {
        fun from(divisionDto: DivisionDto) = ReadDivisionResponse(
            divisionId = divisionDto.id,
            divisionName = divisionDto.name,
        )
    }
}
