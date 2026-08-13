package com.yourssu.scouter.masterdata.division.business

import com.yourssu.scouter.masterdata.division.business.dto.ReadDivisionsResult

import com.yourssu.scouter.masterdata.division.implement.Division
import com.yourssu.scouter.masterdata.division.implement.DivisionReader
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
