package com.yourssu.scouter.common.division.business

import com.yourssu.scouter.common.division.implement.Division
import com.yourssu.scouter.common.division.implement.DivisionReader
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
