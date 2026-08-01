package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.business.dto.InterviewRequirementDto
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementRequest
import com.yourssu.scouter.common.semester.implement.SemesterReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
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
    }
}
