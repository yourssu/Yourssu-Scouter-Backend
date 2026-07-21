package com.yourssu.scouter.common.division.application

import com.yourssu.scouter.common.division.business.DivisionDto

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
