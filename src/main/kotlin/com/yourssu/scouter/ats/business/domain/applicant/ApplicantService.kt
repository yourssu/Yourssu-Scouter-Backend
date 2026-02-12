package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.business.support.utils.ApplicantStateConverter
import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import java.time.ZoneOffset
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantWriter
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import org.springframework.stereotype.Service

@Service
class ApplicantService(
    private val applicantWriter: ApplicantWriter,
    private val applicantReader: ApplicantReader,
    private val departmentReader: DepartmentReader,
    private val partReader: PartReader,
    private val semesterReader: SemesterReader,
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

        return ApplicantDto.from(applicant)
    }

    fun readAllByFilters(
        name: String?,
        state: String?,
        semesterId: Long?,
        partId: Long?,
    ): List<ApplicantDto> {
        var applicants: List<Applicant> = applicantReader.readAll()

        if (!name.isNullOrEmpty()) {
            applicants = applicants.filter { it.name.contains(name, ignoreCase = true) }
        }
        if (!state.isNullOrEmpty()) {
            val applicantState: ApplicantState = ApplicantStateConverter.convertToEnum(state)
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

        return applicants.sorted().map { ApplicantDto.from(it) }
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
        val customOrder = listOf(
            ApplicantState.UNDER_REVIEW,
            ApplicantState.DOCUMENT_REJECTED,
            ApplicantState.INTERVIEW_REJECTED,
            ApplicantState.INCUBATING_REJECTED,
            ApplicantState.FINAL_ACCEPTED,
        )

        return customOrder.map { ApplicantStateConverter.convertToString(it) }
    }
}
