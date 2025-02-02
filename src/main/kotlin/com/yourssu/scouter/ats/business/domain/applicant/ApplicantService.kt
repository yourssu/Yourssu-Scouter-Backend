package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.business.support.utils.ApplicantStateConverter
import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
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
        val applicantSemester: Semester = semesterReader.readById(command.applicantSemesterId)

        val toWriteApplicant: Applicant = command.toDomain(department, part, applicantSemester)
        val writtenApplicant: Applicant = applicantWriter.write(toWriteApplicant)

        return writtenApplicant.id!!
    }

    fun readById(applicantId: Long): ApplicantDto {
        val applicant: Applicant = applicantReader.readById(applicantId)

        return ApplicantDto.from(applicant)
    }

    fun readAll(): List<ApplicantDto> {
        val applicants: List<Applicant> = applicantReader.readAll()

        return applicants.map { ApplicantDto.from(it) }
    }

    fun searchByName(name: String): List<ApplicantDto> {
        val applicants: List<Applicant> = applicantReader.searchAlByName(name)

        return applicants.map { ApplicantDto.from(it) }
    }

    fun filterByState(state: String): List<ApplicantDto> {
        val applicantState: ApplicantState = ApplicantStateConverter.convertToEnum(state)
        val applicants: List<Applicant> = applicantReader.filterByState(applicantState)

        return applicants.map { ApplicantDto.from(it) }
    }

    fun filterBySemester(semesterId: Long): List<ApplicantDto> {
        val semester: Semester = semesterReader.readById(semesterId)
        val applicants: List<Applicant> = applicantReader.filterBySemester(semester)

        return applicants.map { ApplicantDto.from(it) }
    }

    fun updateById(command: UpdateApplicantCommand) {
        val target: Applicant = applicantReader.readById(command.targetApplicantId)
        val updated = Applicant(
            id = target.id,
            name = command.name ?: target.name,
            email = command.email ?: target.email,
            phoneNumber = command.phoneNumber ?: target.phoneNumber,
            age = command.age ?: target.age,
            department = command.departmentId?.let { departmentReader.readById(it) } ?: target.department,
            studentId = command.studentId ?: target.studentId,
            part = command.partId?.let { partReader.readById(it) } ?: target.part,
            state = command.state ?: target.state,
            applicationDate = command.applicationDate ?: target.applicationDate,
            applicationSemester = command.applicantSemesterId?.let { semesterReader.readById(it) }
                ?: target.applicationSemester,
            academicSemester = command.academicSemester ?: target.academicSemester,
        )

        applicantWriter.write(updated)
    }

    fun deleteById(applicantId: Long) {
        val target: Applicant = applicantReader.readById(applicantId)

        applicantWriter.delete(target)
    }
}
