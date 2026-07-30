package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.business.dto.PartInterviewRequirementDto
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementReader
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.interview.application.dto.UpdatePartInterviewRequirementRequest
import com.yourssu.scouter.common.semester.implement.SemesterReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.Duration

@Service
class PartInterviewRequirementService(
    private val partInterviewRequirementReader: PartInterviewRequirementReader,
    private val partInterviewRequirementWriter: PartInterviewRequirementWriter,
    private val partReader: PartReader,
    private val semesterReader: SemesterReader,
    private val interviewRubricReader: InterviewRubricReader,
    private val interviewRubricWriter: InterviewRubricWriter,
    private val interviewEvaluationReader: InterviewEvaluationReader,
) {

    fun readByPartIdAndSemester(partId: Long, semester: Semester): PartInterviewRequirementDto {
        partReader.readById(partId)

        val requirements = partInterviewRequirementReader.readAllByPartIdAndSemester(partId, semester)
        return PartInterviewRequirementDto.from(requirements)
    }

    @Transactional
    fun saveAll(partId: Long, semester: Semester, request: UpdatePartInterviewRequirementRequest) {
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

        val domains = mutableListOf<PartInterviewRequirement>()

        request.culture.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, semesterId, RubricGroupType.CULTURE, it.content))
        }
        request.team.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, semesterId, RubricGroupType.TEAM, it.content))
        }
        request.job.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, semesterId, RubricGroupType.JOB, it.content))
        }
        request.other.forEach {
            domains.add(PartInterviewRequirement(it.id, partId, semesterId, RubricGroupType.OTHER, it.content))
        }

        val savedRequirements = partInterviewRequirementWriter.saveAll(domains, partId, semester)

        val newItems = savedRequirements.map { req ->
            InterviewEvaluationItem(
                id = null,
                keyword = req.content,
                rubricType = req.rubricType,
                maxScore = 0
            )
        }

        val rubricToSave = if (existingRubric != null) {
            InterviewRubric(
                id = existingRubric.id,
                partId = partId,
                semester = semester,
                deadline = existingRubric.deadline,
                isLocked = existingRubric.isLocked,
                items = newItems
            )
        } else {
            InterviewRubric(
                id = null,
                partId = partId,
                semester = semester,
                deadline = Instant.now().plus(Duration.ofDays(365)), // 1년 뒤 기본 데드라인
                isLocked = false,
                items = newItems
            )
        }

        interviewRubricWriter.save(rubricToSave)
    }
}
