package com.yourssu.scouter.common.division.business

import com.yourssu.scouter.common.division.implement.Division

data class ReadDivisionsResult(
    val divisionDtos: List<DivisionDto>,
) {

    companion object {
        fun from(divisions: List<Division>): ReadDivisionsResult = ReadDivisionsResult(
            divisionDtos = divisions.map { DivisionDto.from(it) },
        )
    }
}
