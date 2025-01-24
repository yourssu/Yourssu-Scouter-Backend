package com.yourssu.scouter.common.business.domain.part

import com.yourssu.scouter.common.business.domain.division.DivisionDto
import com.yourssu.scouter.common.implement.domain.part.Part

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
