package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
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
    private val applicationReader: ApplicantReader,
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
        val applicant: Applicant = applicationReader.readById(applicantId)

        return ApplicantDto.from(applicant)
    }

    fun readAll(): List<ApplicantDto> {
        val applicants: List<Applicant> = applicationReader.readAll()

        return applicants.map { ApplicantDto.from(it) }
    }

    fun deleteById(applicantId: Long) {
        val target: Applicant = applicationReader.readById(applicantId)

        applicantWriter.delete(target)
    }
}
