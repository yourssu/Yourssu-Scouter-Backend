package com.yourssu.scouter.common.business.domain.division

import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.division.DivisionReader
import org.springframework.stereotype.Service

@Service
class DivisionService(
    private val divisionReader: DivisionReader,
) {

    fun readAll(): ReadDivisionsResult {
        val divisions: List<Division> = divisionReader.readAll()

        return ReadDivisionsResult.from(divisions)
    }
}
