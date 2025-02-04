package com.yourssu.scouter.dummy

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantRepository
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.implement.domain.department.DepartmentRepository
import com.yourssu.scouter.common.implement.domain.part.PartRepository
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class ApplicantDummy(
    private val departmentRepository: DepartmentRepository,
    private val partRepository: PartRepository,
    private val semesterRepository: SemesterRepository,
    private val applicantRepository: ApplicantRepository,
) {

    val names = listOf(
        "김지훈", "박수진", "이서준", "정혜진", "최민석", "강지은", "조한별", "임지영", "한윤호", "배유진",
        "오승현", "윤다영", "장민지", "송하윤", "차은지", "문정호", "홍지훈", "배정민", "서윤아", "양찬호"
    )

    val phoneNumbers = listOf(
        "010-3333-3333", "010-4444-4444", "010-5555-5555", "010-6666-6666", "010-7777-7777",
        "010-8888-8888", "010-9999-9999", "010-1234-5678", "010-2345-6789", "010-3456-7890",
        "010-4567-8901", "010-5678-9012", "010-6789-0123", "010-7890-1234", "010-8901-2345",
        "010-9012-3456", "010-0123-4567", "010-1234-5679", "010-2345-6780", "010-3456-7891"
    )

    val studentIds = listOf(
        "20203333", "20204444", "20205555", "20206666", "20207777",
        "20208888", "20209999", "20201234", "20202345", "20203456",
        "20204567", "20205678", "20206789", "20207890", "20208901",
        "20209012", "20210123", "20211234", "20212345", "20213456"
    )

    val applicantStates = listOf(
        ApplicantState.UNDER_REVIEW,
        ApplicantState.DOCUMENT_REJECTED,
        ApplicantState.INTERVIEW_REJECTED,
        ApplicantState.INCUBATING_REJECTED,
        ApplicantState.FINAL_ACCEPTED
    )

    val ages = listOf(
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
        "만 20세", "만 21세", "만 22세", "만 23세", "만 24세", "만 25세", "만 26세", "만 27세", "만 28세", "만 29세",
    )

    fun run() {
        val parts = partRepository.findAll()
        val departments = departmentRepository.findAllByOrderByNameAsc()
        val semesters = semesterRepository.findAll()

        for (i in 0 until 20) {
            applicantRepository.save(
                Applicant(
                    name = names.random(),
                    email = names.random() + ".applicants@gmail.com",
                    phoneNumber = phoneNumbers.random(),
                    age = ages.random(),
                    department = departments.random().name,
                    studentId = studentIds.random(),
                    part = parts.random(),
                    state = applicantStates.random(),
                    applicationDateTime = LocalDate.of((2018..2024).random(), (1..12).random(), (1..28).random())
                        .atStartOfDay(),
                    applicationSemester = semesters.random(),
                    academicSemester = (1..5).random().toString() + "-" + (1..2).random().toString(),
                )
            )
        }
    }
}
