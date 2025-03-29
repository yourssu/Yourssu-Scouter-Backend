package com.yourssu.scouter.common.implement.support.initialization

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncMapping
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncMappingRepository
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartRepository
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Order(3)
@Transactional
class ApplicantSyncMappingInitializer(
    private val partRepository: PartRepository,
    private val semesterRepository: SemesterRepository,
    private val applicantSyncMappingRepository: ApplicantSyncMappingRepository,
    private val applicantSyncMappingData: ApplicantSyncMappingData,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (alreadyInitialized()) {
            return
        }
        val semesters: List<Semester> = semesterRepository.findAll()
        val parts: List<Part> = partRepository.findAll()

        for (mappingData in applicantSyncMappingData.datas) {
            val targetSemester = Semester.of(mappingData.semester)
            val semester: Semester = semesters.find { it.year == targetSemester.year && it.term == targetSemester.term }
                ?: throw IllegalArgumentException("Semester not found: $targetSemester")
            val part: Part = parts.find { it.name == mappingData.part }
                ?: throw IllegalArgumentException("Part not found: ${mappingData.part}")

            val applicantSyncMapping = ApplicantSyncMapping(
                applicantSemester = semester,
                part = part,
                formId = mappingData.formId,
                nameQuestion = mappingData.nameQuestion,
                emailQuestion = mappingData.emailQuestion,
                phoneNumberQuestion = mappingData.phoneNumberQuestion,
                ageQuestion = mappingData.ageQuestion,
                departmentQuestion = mappingData.departmentQuestion,
                studentIdQuestion = mappingData.studentIdQuestion,
                academicSemesterQuestion = mappingData.academicSemesterQuestion,
            )

            applicantSyncMappingRepository.save(applicantSyncMapping)
        }
    }

    private fun alreadyInitialized() = applicantSyncMappingRepository.count() != 0L
}
