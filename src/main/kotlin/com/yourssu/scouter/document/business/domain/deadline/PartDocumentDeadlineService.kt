package com.yourssu.scouter.document.business.domain.deadline

import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.document.implement.domain.deadline.PartDocumentDeadline
import com.yourssu.scouter.document.implement.domain.deadline.PartDocumentDeadlineReader
import com.yourssu.scouter.document.implement.domain.deadline.PartDocumentDeadlineWriter
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
