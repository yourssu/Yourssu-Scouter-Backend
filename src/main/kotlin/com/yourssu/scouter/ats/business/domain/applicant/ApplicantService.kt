package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import java.time.ZoneOffset
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantAnswerReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSort
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantWriter
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.document.implement.domain.evaluation.DocumentEvaluation
import com.yourssu.scouter.document.implement.domain.evaluation.DocumentEvaluationReader
import org.springframework.stereotype.Service

@Service
class ApplicantService(
    private val applicantWriter: ApplicantWriter,
    private val applicantReader: ApplicantReader,
    private val applicantAnswerReader: ApplicantAnswerReader,
    private val departmentReader: DepartmentReader,
    private val partReader: PartReader,
    private val semesterReader: SemesterReader,
    private val documentEvaluationReader: DocumentEvaluationReader,
) {

    fun create(command: CreateApplicantCommand): Long {
        val department: Department = departmentReader.readById(command.departmentId)
        val part: Part = partReader.readById(command.partId)
        val applicationSemester: Semester = semesterReader.readById(command.applicationSemesterId)

        val toWriteApplicant: Applicant = command.toDomain(department, part, applicationSemester)
        val writtenApplicant: Applicant = applicantWriter.write(toWriteApplicant)

        return writtenApplicant.id!!
    }

    fun readById(applicantId: Long): ApplicantDto {
        val applicant: Applicant = applicantReader.readById(applicantId)

        return ApplicantDto.from(applicant).copy(
            documentAverageScore = averageSubmittedScore(documentEvaluationReader.readAllByApplicantId(applicantId)),
        )
    }

    fun readAnswersByApplicantId(applicantId: Long): List<ApplicantAnswerDto> {
        applicantReader.readById(applicantId)

        return applicantAnswerReader.readAllByApplicantId(applicantId).map(ApplicantAnswerDto::from)
    }

    fun readAllByFilters(
        name: String?,
        state: String?,
        semesterId: Long?,
        partId: Long?,
        sort: ApplicantSort = ApplicantSort.DEFAULT,
    ): List<ApplicantDto> {
        var applicants: List<Applicant> = applicantReader.readAll()

        if (!name.isNullOrEmpty()) {
            applicants = applicants.filter { it.name.contains(name, ignoreCase = true) }
        }
        if (!state.isNullOrEmpty()) {
            val applicantState: ApplicantState = runCatching { ApplicantState.valueOf(state) }
                .getOrElse { throw IllegalArgumentException("허용되지 않는 state 값입니다: $state") }
            applicants = applicants.filter { it.state == applicantState }
        }
        if (semesterId != null) {
            val semester: Semester = semesterReader.readById(semesterId)
            applicants = applicants.filter { it.applicationSemester == semester }
        }
        if (partId != null) {
            val part: Part = partReader.readById(partId)
            applicants = applicants.filter { it.part == part }
        }

        val orderedApplicants = when (sort) {
            ApplicantSort.SEMESTER_DESC -> applicants.sortedByDescending { it.applicationSemester }
            else -> applicants.sorted()
        }

        val evaluationsByApplicantId = documentEvaluationReader
            .readAllByApplicantIdIn(orderedApplicants.mapNotNull { it.id })
            .groupBy { it.applicantId }

        val dtos = orderedApplicants.map { applicant ->
            ApplicantDto.from(applicant).copy(
                documentAverageScore = averageSubmittedScore(evaluationsByApplicantId[applicant.id].orEmpty()),
                // 면접 평가 도메인이 아직 없어 항상 null — interview 도메인 구현 후 채워질 예정
                interviewAverageScore = null,
            )
        }

        return when (sort) {
            ApplicantSort.DOCUMENT_SCORE_DESC -> dtos.sortedByDescending { it.documentAverageScore ?: Double.NEGATIVE_INFINITY }
            ApplicantSort.DOCUMENT_SCORE_ASC -> dtos.sortedBy { it.documentAverageScore ?: Double.POSITIVE_INFINITY }
            else -> dtos
        }
    }

    private fun averageSubmittedScore(evaluations: List<DocumentEvaluation>): Double? {
        val submittedScores = evaluations.filter { it.isSubmitted() }.map { it.totalScore() }

        return if (submittedScores.isEmpty()) null else submittedScores.average()
    }

    fun updateById(command: UpdateApplicantCommand) {
        val target: Applicant = applicantReader.readById(command.targetApplicantId)
        val updated = Applicant(
            id = target.id,
            name = command.name ?: target.name,
            email = command.email ?: target.email,
            phoneNumber = command.phoneNumber ?: target.phoneNumber,
            age = command.age ?: target.age,
            department = command.departmentId?.let { departmentReader.readById(it).name } ?: target.department,
            studentId = command.studentId ?: target.studentId,
            part = command.partId?.let { partReader.readById(it) } ?: target.part,
            state = command.state ?: target.state,
            applicationDateTime = command.applicationDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC) ?: target.applicationDateTime,
            applicationSemester = command.applicationSemesterId?.let { semesterReader.readById(it) }
                ?: target.applicationSemester,
            academicSemester = command.academicSemester ?: target.academicSemester,
            availableTimes = command.availableTimes ?: target.availableTimes
        )

        applicantWriter.write(updated)
    }

    fun deleteById(applicantId: Long) {
        val target: Applicant = applicantReader.readById(applicantId)

        applicantWriter.delete(target)
    }

    fun readAllStates(): List<String> {
        return ApplicantState.entries.map { it.name }
    }
}
