package com.yourssu.scouter.common.division.business.dto

import com.yourssu.scouter.common.division.implement.Division

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
