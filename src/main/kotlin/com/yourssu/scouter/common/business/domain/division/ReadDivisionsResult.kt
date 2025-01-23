package com.yourssu.scouter.common.business.domain.division

import com.yourssu.scouter.common.implement.domain.division.Division

data class ReadDivisionsResult(
    val divisionDtos: List<DivisionDto>,
) {

    companion object {
        fun from(divisions: List<Division>): ReadDivisionsResult = ReadDivisionsResult(
            divisionDtos = divisions.map { DivisionDto.from(it) },
        )
    }
}
