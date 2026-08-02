package com.yourssu.scouter.common.part.business.dto

import com.yourssu.scouter.common.division.business.dto.DivisionDto
import com.yourssu.scouter.common.part.implement.Part

data class PartDto(
    val id: Long,
    val division: DivisionDto,
    val name: String,
    val hasAssignment: Boolean = false,
) {

    companion object {
        fun from(part: Part): PartDto = PartDto(
            id = part.id!!,
            division = DivisionDto.from(part.division),
            name = part.name,
            hasAssignment = part.hasAssignment,
        )
    }
}
