package com.yourssu.scouter.common.business.domain.part

import com.yourssu.scouter.common.implement.domain.part.PartReader
import org.springframework.stereotype.Service

@Service
class PartService(
    private val partReader: PartReader,
) {

    fun readAll(): ReadPartsResult {
        val parts = partReader.readAll()

        return ReadPartsResult.from(parts)
    }
}
