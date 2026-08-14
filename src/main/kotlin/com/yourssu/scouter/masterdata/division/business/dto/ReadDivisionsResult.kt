package com.yourssu.scouter.masterdata.division.business.dto

import com.yourssu.scouter.masterdata.division.implement.Division

data class ReadDivisionsResult(
    val divisionDtos: List<DivisionDto>,
) {

    companion object {
        fun from(divisions: List<Division>): ReadDivisionsResult = ReadDivisionsResult(
            divisionDtos = divisions.map { DivisionDto.from(it) },
        )
    }
}
