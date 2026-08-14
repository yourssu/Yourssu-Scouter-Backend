package com.yourssu.scouter.common.initializer

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncMapping
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncMappingRepository
import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.part.implement.PartRepository
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.SemesterRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Order(4)
@Transactional
class ApplicantSyncMappingInitializer(
    private val partRepository: PartRepository,
    private val semesterRepository: SemesterRepository,
    private val applicantSyncMappingRepository: ApplicantSyncMappingRepository,
    private val applicantSyncMappingData: ApplicantSyncMappingData,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val semesters: List<Semester> = semesterRepository.findAll()
        val parts: List<Part> = partRepository.findAll()

        for (mappingData in applicantSyncMappingData.datas) {
            val targetSemester = Semester.of(mappingData.semester)
            val semester: Semester = semesters.find { it.year == targetSemester.year && it.term == targetSemester.term }
                ?: throw IllegalArgumentException("Semester not found: ${targetSemester.year} ${targetSemester.term}")
            val part: Part = parts.find { it.name == mappingData.part }
                ?: throw IllegalArgumentException("Part not found: ${mappingData.part}")

            val semesterId = semester.id ?: continue
            if (applicantSyncMappingRepository.existsByApplicationSemesterIdAndPartId(semesterId, part.id!!)) {
                continue
            }

            val applicantSyncMapping = ApplicantSyncMapping(
                applicationSemester = semester,
                part = part,
                formId = mappingData.formId,
                nameQuestion = mappingData.nameQuestion,
                emailQuestion = mappingData.emailQuestion,
                phoneNumberQuestion = mappingData.phoneNumberQuestion,
                ageQuestion = mappingData.ageQuestion,
                departmentQuestion = mappingData.departmentQuestion,
                studentIdQuestion = mappingData.studentIdQuestion,
                academicSemesterQuestion = mappingData.academicSemesterQuestion,
                availableTimeQuestion = mappingData.availableTimeQuestion,
            )

            applicantSyncMappingRepository.save(applicantSyncMapping)
        }
    }
}
