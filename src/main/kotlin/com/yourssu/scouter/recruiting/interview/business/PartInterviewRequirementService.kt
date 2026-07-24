package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.recruiting.interview.business.dto.PartInterviewRequirementDto
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementReader
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartInterviewRequirementService(
    private val partInterviewRequirementReader: PartInterviewRequirementReader,
    private val partInterviewRequirementWriter: PartInterviewRequirementWriter,
    private val partReader: PartReader,
) {

    fun readByPartId(partId: Long): PartInterviewRequirementDto {
        partReader.readById(partId)

        return PartInterviewRequirementDto.from(partInterviewRequirementReader.readByPartId(partId))
    }

    @Transactional
    fun upsert(partId: Long, culture: String, team: String, job: String) {
        partReader.readById(partId)

        partInterviewRequirementWriter.upsert(PartInterviewRequirement(partId, culture, team, job))
    }
}
