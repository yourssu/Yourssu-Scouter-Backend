package com.yourssu.scouter.common.part.business

import com.yourssu.scouter.common.part.business.dto.ReadPartsResult
import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.common.part.implement.PartWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartService(
    private val partReader: PartReader,
    private val partWriter: PartWriter,
) {

    fun readAll(): ReadPartsResult {
        val parts = partReader.readAll()
        return ReadPartsResult.from(parts)
    }

    @Transactional
    fun toggleAssignment(partId: Long) {
        val part = partReader.readById(partId)
        partWriter.updateHasAssignment(partId, !part.hasAssignment)
    }
}
