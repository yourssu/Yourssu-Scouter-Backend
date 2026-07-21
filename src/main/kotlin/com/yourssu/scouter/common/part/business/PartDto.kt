package com.yourssu.scouter.common.part.business

import com.yourssu.scouter.common.division.business.DivisionDto
import com.yourssu.scouter.common.part.implement.Part

data class PartDto(
    val id: Long,
    val division: DivisionDto,
    val name: String,
) {

    companion object {
        fun from(part: Part): PartDto = PartDto(
            id = part.id!!,
            division = DivisionDto.from(part.division),
            name = part.name,
        )
    }
}
