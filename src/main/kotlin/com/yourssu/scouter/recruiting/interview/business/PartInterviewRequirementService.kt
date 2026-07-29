package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.business.dto.PartInterviewRequirementDto
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementReader
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.interview.application.dto.UpdatePartInterviewRequirementRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartInterviewRequirementService(
    private val partInterviewRequirementReader: PartInterviewRequirementReader,
    private val partInterviewRequirementWriter: PartInterviewRequirementWriter,
    private val partReader: PartReader,
) {

    fun readByPartIdAndSemester(partId: Long, semester: Semester): PartInterviewRequirementDto {
        partReader.readById(partId)

        val requirements = partInterviewRequirementReader.readAllByPartIdAndSemester(partId, semester)
        return PartInterviewRequirementDto.from(requirements)
    }

    @Transactional
    fun saveAll(partId: Long, semester: Semester, request: UpdatePartInterviewRequirementRequest) {
        partReader.readById(partId)

        val domains = mutableListOf<PartInterviewRequirement>()

        request.culture.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, 0L, RubricGroupType.CULTURE, it.content))
        }
        request.team.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, 0L, RubricGroupType.TEAM, it.content))
        }
        request.job.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, 0L, RubricGroupType.JOB, it.content))
        }
        request.other.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, 0L, RubricGroupType.OTHER, it.content))
        }

        partInterviewRequirementWriter.saveAll(domains, partId, semester)
    }
}
