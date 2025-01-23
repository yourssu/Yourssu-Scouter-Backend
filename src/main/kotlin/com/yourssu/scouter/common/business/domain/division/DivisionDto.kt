package com.yourssu.scouter.common.business.domain.division

import com.yourssu.scouter.common.implement.domain.division.Division

data class DivisionDto(
    val id: Long,
    val name: String,
) {

    companion object {
        fun from(division: Division): DivisionDto = DivisionDto(
            id = division.id!!,
            name = division.name,
        )
    }
}
