package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.common.part.storage.JpaPartRepository
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.common.semester.storage.JpaSemesterRepository
import com.yourssu.scouter.common.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementRepository
import org.springframework.stereotype.Repository

@Repository
class InterviewRequirementRepositoryImpl(
    private val jpaInterviewRequirementRepository: JpaInterviewRequirementRepository,
    private val jpaPartRepository: JpaPartRepository,
    private val jpaSemesterRepository: JpaSemesterRepository,
) : InterviewRequirementRepository {

    override fun findAllByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirement> {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term) ?: return emptyList()
        return jpaInterviewRequirementRepository.findAllByPartIdAndSemester(partId, semesterEntity)
            .map { it.toDomain() }
    }

    override fun findAllGlobalBySemester(semester: Semester): List<InterviewRequirement> {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term) ?: return emptyList()
        return jpaInterviewRequirementRepository.findAllByPartIsNullAndSemester(semesterEntity)
            .map { it.toDomain() }
    }

    override fun findAllApplicableByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirement> {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term) ?: return emptyList()
        return jpaInterviewRequirementRepository.findAllApplicableByPartIdAndSemester(partId, semesterEntity)
            .map { it.toDomain() }
    }

    override fun saveAll(
        requirements: List<InterviewRequirement>,
        partId: Long,
        semester: Semester
    ): List<InterviewRequirement> {
        val semesterEntity = resolveSemesterEntity(semester)
        val existingEntities = jpaInterviewRequirementRepository
            .findAllByPartIdAndSemester(partId, semesterEntity)
            .associateBy { it.id }

        return replaceAll(requirements, existingEntities, jpaPartRepository.getReferenceById(partId), semesterEntity)
    }

    override fun saveAllGlobal(
        requirements: List<InterviewRequirement>,
        semester: Semester
    ): List<InterviewRequirement> {
        val semesterEntity = resolveSemesterEntity(semester)
        val existingEntities = jpaInterviewRequirementRepository
            .findAllByPartIsNullAndSemester(semesterEntity)
            .associateBy { it.id }

        return replaceAll(requirements, existingEntities, part = null, semesterEntity)
    }

    private fun resolveSemesterEntity(semester: Semester): SemesterEntity =
        jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term)
            ?: throw IllegalArgumentException("해당 학기 정보를 찾을 수 없습니다: ${semester.year}-${semester.term.intValue}")

    private fun replaceAll(
        requirements: List<InterviewRequirement>,
        existingEntities: Map<Long?, InterviewRequirementEntity>,
        part: PartEntity?,
        semesterEntity: SemesterEntity,
    ): List<InterviewRequirement> {
        val updatedIds = requirements.mapNotNull { it.id }.toSet()
        val toDelete = existingEntities.filterKeys { it !in updatedIds }.values
        jpaInterviewRequirementRepository.deleteAll(toDelete)

        val savedEntities = requirements.map { req ->
            if (req.id != null && !existingEntities.containsKey(req.id)) {
                throw IllegalArgumentException("해당 범위에 존재하지 않는 요구조건 ID입니다: ${req.id}")
            }
            val entity = InterviewRequirementEntity(
                id = req.id,
                part = part,
                semester = semesterEntity,
                rubricType = req.rubricType,
                content = req.content
            )
            jpaInterviewRequirementRepository.save(entity)
        }

        return savedEntities.map { it.toDomain() }
    }
}
