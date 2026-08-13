package com.yourssu.scouter.recruiting.deadline.business

import com.yourssu.scouter.recruiting.deadline.business.dto.PartDocumentDeadlineDto

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadline
import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadlineReader
import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadlineWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PartDocumentDeadlineService(
    private val partDocumentDeadlineReader: PartDocumentDeadlineReader,
    private val partDocumentDeadlineWriter: PartDocumentDeadlineWriter,
    private val partReader: PartReader,
) {

    fun readByPartId(partId: Long): PartDocumentDeadlineDto {
        partReader.readById(partId)

        return PartDocumentDeadlineDto.from(partDocumentDeadlineReader.readByPartId(partId))
    }

    @Transactional
    fun upsert(partId: Long, deadline: Instant) {
        partReader.readById(partId)

        partDocumentDeadlineWriter.upsert(PartDocumentDeadline(partId, deadline))
    }
}
