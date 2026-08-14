package com.yourssu.scouter.masterdata.division.business.dto

import com.yourssu.scouter.masterdata.division.implement.Division

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
