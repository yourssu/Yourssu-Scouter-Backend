package com.yourssu.scouter.masterdata.part.business.dto

import com.yourssu.scouter.masterdata.division.business.dto.DivisionDto
import com.yourssu.scouter.masterdata.part.implement.Part

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
