package com.yourssu.scouter.common.part.business

import com.yourssu.scouter.common.part.implement.PartReader
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
