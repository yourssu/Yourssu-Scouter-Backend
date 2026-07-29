package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.common.part.storage.JpaPartRepository
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.common.semester.storage.JpaSemesterRepository
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementRepository
import org.springframework.stereotype.Repository

@Repository
class PartInterviewRequirementRepositoryImpl(
    private val jpaPartInterviewRequirementRepository: JpaPartInterviewRequirementRepository,
    private val jpaPartRepository: JpaPartRepository,
    private val jpaSemesterRepository: JpaSemesterRepository,
) : PartInterviewRequirementRepository {

    override fun findAllByPartIdAndSemester(partId: Long, semester: Semester): List<PartInterviewRequirement> {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term) ?: return emptyList()
        return jpaPartInterviewRequirementRepository.findAllByPartIdAndSemester(partId, semesterEntity)
            .map { it.toDomain() }
    }

    override fun saveAll(
        requirements: List<PartInterviewRequirement>,
        partId: Long,
        semester: Semester
    ): List<PartInterviewRequirement> {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term)
            ?: throw IllegalArgumentException("해당 학기 정보를 찾을 수 없습니다: ${semester.year}-${semester.term.intValue}")

        val existingEntities = jpaPartInterviewRequirementRepository.findAllByPartIdAndSemester(partId, semesterEntity)
            .associateBy { it.id }

        val partRef = jpaPartRepository.getReferenceById(partId)

        val updatedIds = requirements.mapNotNull { it.id }.toSet()
        val toDelete = existingEntities.filterKeys { it !in updatedIds }.values
        jpaPartInterviewRequirementRepository.deleteAll(toDelete)

        val savedEntities = requirements.map { req ->
            val entity = if (req.id != null && existingEntities.containsKey(req.id)) {
                PartInterviewRequirementEntity(
                    id = req.id,
                    part = partRef,
                    semester = semesterEntity,
                    rubricType = req.rubricType,
                    content = req.content
                )
            } else {
                PartInterviewRequirementEntity(
                    id = null,
                    part = partRef,
                    semester = semesterEntity,
                    rubricType = req.rubricType,
                    content = req.content
                )
            }
            jpaPartInterviewRequirementRepository.save(entity)
        }

        return savedEntities.map { it.toDomain() }
    }
}
