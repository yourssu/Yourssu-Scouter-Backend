package com.yourssu.scouter.common.implement.support.initialization

import com.yourssu.scouter.common.implement.domain.college.College
import com.yourssu.scouter.common.implement.domain.college.CollegeRepository
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Suppress("NonAsciiCharacters", "FunctionName")
@Component
@Transactional
class CollegesAndDepartmentsInitializer(
    private val collegeRepository: CollegeRepository,
    private val departmentRepository: DepartmentRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (alreadyInitialized()) {
            return
        }

        initialize_차세대반도체학과()
        initialize_IT대학()
        initialize_경영대학()
        initialize_경제통상대학()
        initialize_공과대학()
        initialize_법과대학()
        initialize_베어드학부대학()
        initialize_사회과학대학()
        initialize_인문대학()
        initialize_자연과학대학()
    }

    private fun alreadyInitialized() = collegeRepository.count() != 0L

    private fun initialize_차세대반도체학과() {
        val college: College = collegeRepository.save(College(name = "차세대반도체학과"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "차세대반도체학과"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_IT대학() {
        val college: College = collegeRepository.save(College(name = "IT대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "AI융합학부"))
        departments.add(Department(collegeId = college.id, name = "글로벌미디어학부"))
        departments.add(Department(collegeId = college.id, name = "미디어경영학과"))
        departments.add(Department(collegeId = college.id, name = "소프트웨어학부"))
        departments.add(Department(collegeId = college.id, name = "전자정보공학부 IT융합전공"))
        departments.add(Department(collegeId = college.id, name = "전자정보공학부 전자공학전공"))
        departments.add(Department(collegeId = college.id, name = "정보보호학과"))
        departments.add(Department(collegeId = college.id, name = "컴퓨터학부"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_경영대학() {
        val college: College = collegeRepository.save(College(name = "경영대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "경영학부"))
        departments.add(Department(collegeId = college.id, name = "금융학부"))
        departments.add(Department(collegeId = college.id, name = "벤처경영학과"))
        departments.add(Department(collegeId = college.id, name = "벤처중소기업학과"))
        departments.add(Department(collegeId = college.id, name = "복지경영학과"))
        departments.add(Department(collegeId = college.id, name = "혁신경영학과"))
        departments.add(Department(collegeId = college.id, name = "회계세무학과"))
        departments.add(Department(collegeId = college.id, name = "회계학과"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_경제통상대학() {
        val college: College = collegeRepository.save(College(name = "경제통상대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "경제학과"))
        departments.add(Department(collegeId = college.id, name = "국제무역학과"))
        departments.add(Department(collegeId = college.id, name = "글로벌통상학과"))
        departments.add(Department(collegeId = college.id, name = "금융경제학과"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_공과대학() {
        val college: College = collegeRepository.save(College(name = "공과대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "건축학부 건축공학전공"))
        departments.add(Department(collegeId = college.id, name = "건축학부 건축학부"))
        departments.add(Department(collegeId = college.id, name = "건축학부 건축학전공"))
        departments.add(Department(collegeId = college.id, name = "건축학부 실내건축전공"))
        departments.add(Department(collegeId = college.id, name = "기계공학부"))
        departments.add(Department(collegeId = college.id, name = "산업정보시스템공학과"))
        departments.add(Department(collegeId = college.id, name = "신소재공학과"))
        departments.add(Department(collegeId = college.id, name = "전기공학부"))
        departments.add(Department(collegeId = college.id, name = "화학공학과"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_법과대학() {
        val college: College = collegeRepository.save(College(name = "법과대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "국제법무학과"))
        departments.add(Department(collegeId = college.id, name = "법학과"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_베어드학부대학() {
        val college: College = collegeRepository.save(College(name = "베어드학부대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "자유전공학부"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_사회과학대학() {
        val college: College = collegeRepository.save(College(name = "사회과학대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "사회복지학부"))
        departments.add(Department(collegeId = college.id, name = "언론홍보학과"))
        departments.add(Department(collegeId = college.id, name = "정보사회학과"))
        departments.add(Department(collegeId = college.id, name = "정치외교학과"))
        departments.add(Department(collegeId = college.id, name = "평생교육학과"))
        departments.add(Department(collegeId = college.id, name = "행정학부"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_인문대학() {
        val college: College = collegeRepository.save(College(name = "인문대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "국어국문학과"))
        departments.add(Department(collegeId = college.id, name = "기독교학과"))
        departments.add(Department(collegeId = college.id, name = "독어독문학과"))
        departments.add(Department(collegeId = college.id, name = "불어불문학과"))
        departments.add(Department(collegeId = college.id, name = "사학과"))
        departments.add(Department(collegeId = college.id, name = "스포츠학부"))
        departments.add(Department(collegeId = college.id, name = "영어영문학과"))
        departments.add(Department(collegeId = college.id, name = "예술창작학부 문예창작전공"))
        departments.add(Department(collegeId = college.id, name = "예술창작학부 영화예술전공"))
        departments.add(Department(collegeId = college.id, name = "일어일문학과"))
        departments.add(Department(collegeId = college.id, name = "중어중문학과"))
        departments.add(Department(collegeId = college.id, name = "철학과"))

        departmentRepository.saveAll(departments)
    }

    private fun initialize_자연과학대학() {
        val college: College = collegeRepository.save(College(name = "자연과학대학"))

        val departments = mutableListOf<Department>()
        departments.add(Department(collegeId = college.id!!, name = "물리학과"))
        departments.add(Department(collegeId = college.id, name = "수학과"))
        departments.add(Department(collegeId = college.id, name = "의생명시스템학부"))
        departments.add(Department(collegeId = college.id, name = "정보통계보험수리학과"))
        departments.add(Department(collegeId = college.id, name = "화학과"))

        departmentRepository.saveAll(departments)
    }
}
