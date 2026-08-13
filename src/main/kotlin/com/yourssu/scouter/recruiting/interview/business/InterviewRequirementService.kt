package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.business.dto.InterviewRequirementDto
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementRequest
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import java.time.Duration
import java.time.Instant
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewRequirementService(
    private val partInterviewRequirementReader: InterviewRequirementReader,
    private val partInterviewRequirementWriter: InterviewRequirementWriter,
    private val partReader: PartReader,
    private val semesterReader: SemesterReader,
    private val interviewRubricReader: InterviewRubricReader,
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val interviewRubricWriter: InterviewRubricWriter,
) {

    fun readByPartIdAndSemester(partId: Long, semester: Semester): InterviewRequirementDto {
        partReader.readById(partId)

        val requirements = partInterviewRequirementReader.readAllByPartIdAndSemester(partId, semester)
        return InterviewRequirementDto.from(requirements)
    }

    @Transactional
    fun saveAll(partId: Long, semester: Semester, request: UpdateInterviewRequirementRequest) {
        partReader.readById(partId)

        val resolvedSemester = semesterReader.read(semester)
        val semesterId = resolvedSemester.id!!

        val existingRubric = interviewRubricReader.findByPartIdAndSemester(partId, semester)
        existingRubric?.validateEditable()

        if (existingRubric != null && existingRubric.items.isNotEmpty()) {
            val itemIds = existingRubric.items.mapNotNull { it.id }
            if (interviewEvaluationReader.existsByInterviewEvaluationItemIdIn(itemIds)) {
                throw RubricLockedException("해당 파트에 면접 평가(임시저장 포함)가 존재해 수정 불가")
            }
        }

        val domains = mutableListOf<InterviewRequirement>()

        request.culture.forEach {
            domains.add(InterviewRequirement(it.id, partId, semesterId, RubricGroupType.CULTURE, it.content))
        }
        request.team.forEach {
            domains.add(InterviewRequirement(it.id, partId, semesterId, RubricGroupType.TEAM, it.content))
        }
        request.job.forEach {
            domains.add(InterviewRequirement(it.id, partId, semesterId, RubricGroupType.JOB, it.content))
        }
        request.other.forEach {
            domains.add(InterviewRequirement(it.id, partId, semesterId, RubricGroupType.OTHER, it.content))
        }

        partInterviewRequirementWriter.saveAll(domains, partId, semester)
        syncRubric(partId, semester)
    }

    fun readGlobalBySemester(semester: Semester): InterviewRequirementDto {
        val requirements = partInterviewRequirementReader.readAllGlobalBySemester(semester)
        return InterviewRequirementDto.from(requirements)
    }

    @Transactional
    fun saveAllGlobal(semester: Semester, request: UpdateInterviewRequirementRequest) {
        validateNoPartHasLockedRubric(semester)

        val resolvedSemester = semesterReader.read(semester)
        val semesterId = resolvedSemester.id!!

        val domains = mutableListOf<InterviewRequirement>()

        request.culture.forEach {
            domains.add(InterviewRequirement(it.id, null, semesterId, RubricGroupType.CULTURE, it.content))
        }
        request.team.forEach {
            domains.add(InterviewRequirement(it.id, null, semesterId, RubricGroupType.TEAM, it.content))
        }
        request.job.forEach {
            domains.add(InterviewRequirement(it.id, null, semesterId, RubricGroupType.JOB, it.content))
        }
        request.other.forEach {
            domains.add(InterviewRequirement(it.id, null, semesterId, RubricGroupType.OTHER, it.content))
        }

        partInterviewRequirementWriter.saveAllGlobal(domains, semester)
        partReader.readAll().forEach { part -> syncRubric(part.id!!, semester) }
    }

    private fun syncRubric(partId: Long, semester: Semester) {
        val requirements = partInterviewRequirementReader.readAllApplicableByPartIdAndSemester(partId, semester).filter { it.rubricType != RubricGroupType.OTHER }
        val existing = interviewRubricReader.findByPartIdAndSemester(partId, semester)
        existing?.validateEditable()
        val existingItemsByRequirementId = existing?.items?.associateBy { it.interviewRequirementId } ?: emptyMap()
        val rubric = InterviewRubric(
            id = existing?.id,
            partId = partId,
            semester = semester,
            deadline = existing?.deadline ?: Instant.now().plus(DEFAULT_DEADLINE_DURATION),
            isLocked = existing?.isLocked ?: false,
            items = requirements.map {
                val existingItem = existingItemsByRequirementId[it.id]
                InterviewEvaluationItem(
                    id = existingItem?.id,
                    interviewRequirementId = it.id,
                    keyword = it.content,
                    rubricType = it.rubricType,
                    maxScore = existingItem?.maxScore ?: 0,
                )
            },
        )
        interviewRubricWriter.save(rubric)
    }

    private fun validateNoPartHasLockedRubric(semester: Semester) {
        partReader.readAll().forEach { part ->
            val existingRubric = interviewRubricReader.findByPartIdAndSemester(part.id!!, semester) ?: return@forEach
            existingRubric.validateEditable()

            if (existingRubric.items.isNotEmpty()) {
                val itemIds = existingRubric.items.mapNotNull { it.id }
                if (interviewEvaluationReader.existsByInterviewEvaluationItemIdIn(itemIds)) {
                    throw RubricLockedException(
                        "'${part.name}' 파트에 면접 평가(임시저장 포함)가 존재해 전역 요구조건을 수정할 수 없습니다.",
                    )
                }
            }
        }
    }

    companion object {
        private val DEFAULT_DEADLINE_DURATION = Duration.ofDays(60)
    }
}
