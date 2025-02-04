package com.yourssu.scouter.dummy

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantRepository
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.implement.domain.department.DepartmentRepository
import com.yourssu.scouter.common.implement.domain.part.PartRepository
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import java.time.LocalDateTime
import org.springframework.stereotype.Component

@Component
class ApplicantDummyForMemberSyncTest(
    private val departmentRepository: DepartmentRepository,
    private val partRepository: PartRepository,
    private val semesterRepository: SemesterRepository,
    private val applicantRepository: ApplicantRepository,
) {

    fun run() {
        val parts = partRepository.findAll()
        val departments = departmentRepository.findAllByOrderByNameAsc()
        val semesters = semesterRepository.findAll()

        applicantRepository.save(Applicant(
            name = "합격자1",
            email = "kim1@gmail.com",
            phoneNumber = "010-1111-1111",
            age = "21",
            department = departments[0].name,
            studentId = "101",
            part = parts[1],
            state = ApplicantState.FINAL_ACCEPTED,
            applicationDateTime = LocalDateTime.now(),
            applicationSemester = semesters.random(),
            academicSemester = (1..5).random().toString() + "-" + (1..2).random().toString(),
        ))

        applicantRepository.save(Applicant(
            name = "합격자2",
            email = "kim2@gmail.com",
            phoneNumber = "010-2222-2222",
            age = "22",
            department = departments[0].name,
            studentId = "102",
            part = parts[2],
            state = ApplicantState.FINAL_ACCEPTED,
            applicationDateTime = LocalDateTime.now(),
            applicationSemester = semesters.random(),
            academicSemester = (1..5).random().toString() + "-" + (1..2).random().toString(),
        ))

        applicantRepository.save(Applicant(
            name = "비정상",
            email = "kim3@gmail.com",
            phoneNumber = "010-3333-3333",
            age = "23",
            department = "없는학과",
            studentId = "103",
            part = parts[2],
            state = ApplicantState.FINAL_ACCEPTED,
            applicationDateTime = LocalDateTime.now(),
            applicationSemester = semesters.random(),
            academicSemester = (1..5).random().toString() + "-" + (1..2).random().toString(),
        ))
    }
}
