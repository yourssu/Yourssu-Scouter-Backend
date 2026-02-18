package com.yourssu.scouter.common.application.domain.division

import com.yourssu.scouter.common.business.domain.division.DivisionDto

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
