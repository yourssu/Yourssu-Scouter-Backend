package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.recruiting.applicant.business.dto.UpdateApplicantCommand

import com.yourssu.scouter.recruiting.applicant.business.dto.CreateApplicantCommand

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantDto

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantAnswerDto

import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import java.time.ZoneOffset
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswerReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSort
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantWriter
import com.yourssu.scouter.recruiting.applicant.implement.AssignmentEvaluationValidator
import com.yourssu.scouter.masterdata.department.implement.Department
import com.yourssu.scouter.masterdata.department.implement.DepartmentReader
import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationReader
import com.yourssu.scouter.recruiting.evaluation.implement.FinalEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.FinalEvaluationReader
import com.yourssu.scouter.recruiting.support.business.utils.ApplicantStateConverter
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
    private val finalEvaluationReader: FinalEvaluationReader,
    private val assignmentEvaluationValidator: AssignmentEvaluationValidator,
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
            documentAverageScore = averageSubmittedDocumentScore(documentEvaluationReader.readAllByApplicantId(applicantId)),
            interviewAverageScore = averageSubmittedInterviewScore(finalEvaluationReader.readAllByApplicantId(applicantId)),
        )
    }

    fun readAnswersByApplicantId(applicantId: Long): List<ApplicantAnswerDto> {
        applicantReader.readById(applicantId)

        return applicantAnswerReader.readAllByApplicantId(applicantId).map(ApplicantAnswerDto::from)
    }

    fun readAllByFilters(
        name: String?,
        states: List<String>?,
        semesterId: Long?,
        partId: Long?,
        sort: ApplicantSort = ApplicantSort.DEFAULT,
    ): List<ApplicantDto> {
        var applicants: List<Applicant> = applicantReader.readAll()

        if (!name.isNullOrEmpty()) {
            applicants = applicants.filter { it.name.contains(name, ignoreCase = true) }
        }
        if (!states.isNullOrEmpty()) {
            val applicantStates: List<ApplicantState> = states.map(ApplicantStateConverter::convertToEnum)
            applicants = applicants.filter { it.state in applicantStates }
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

        val applicantIds = orderedApplicants.mapNotNull { it.id }
        val documentEvaluationsByApplicantId = documentEvaluationReader
            .readAllByApplicantIdIn(applicantIds)
            .groupBy { it.applicantId }
        val finalEvaluationsByApplicantId = finalEvaluationReader
            .readAllByApplicantIdIn(applicantIds)
            .groupBy { it.applicantId }

        val dtos = orderedApplicants.map { applicant ->
            ApplicantDto.from(applicant).copy(
                documentAverageScore = averageSubmittedDocumentScore(documentEvaluationsByApplicantId[applicant.id].orEmpty()),
                interviewAverageScore = averageSubmittedInterviewScore(finalEvaluationsByApplicantId[applicant.id].orEmpty()),
            )
        }

        return when (sort) {
            ApplicantSort.DOCUMENT_SCORE_DESC -> dtos.sortedByDescending { it.documentAverageScore ?: Double.NEGATIVE_INFINITY }
            ApplicantSort.DOCUMENT_SCORE_ASC -> dtos.sortedBy { it.documentAverageScore ?: Double.POSITIVE_INFINITY }
            else -> dtos
        }
    }

    private fun averageSubmittedDocumentScore(evaluations: List<DocumentEvaluation>): Double? {
        val submittedScores = evaluations.filter { it.isSubmitted() }.map { it.totalScore() }

        return if (submittedScores.isEmpty()) null else submittedScores.average()
    }

    private fun averageSubmittedInterviewScore(evaluations: List<FinalEvaluation>): Double? {
        val submittedScores = evaluations.filter { it.isSubmitted() }.map { it.score }

        return if (submittedScores.isEmpty()) null else submittedScores.average()
    }

    fun updateById(command: UpdateApplicantCommand) {
        val target: Applicant = applicantReader.readById(command.targetApplicantId)
        val newState = command.state ?: target.state
        val newPart = command.partId?.let { partReader.readById(it) } ?: target.part

        if (newState == ApplicantState.ASSIGNMENT_ACCEPTED || newState == ApplicantState.ASSIGNMENT_REJECTED) {
            assignmentEvaluationValidator.validate(newPart)
        }

        val updated = Applicant(
            id = target.id,
            name = command.name ?: target.name,
            email = command.email ?: target.email,
            phoneNumber = command.phoneNumber ?: target.phoneNumber,
            age = command.age ?: target.age,
            department = command.departmentId?.let { departmentReader.readById(it).name } ?: target.department,
            studentId = command.studentId ?: target.studentId,
            part = newPart,
            state = newState,
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
