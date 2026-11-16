package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.department.business.DepartmentService
import com.yourssu.scouter.masterdata.part.business.PartService
import com.yourssu.scouter.masterdata.semester.business.SemesterService
import com.yourssu.scouter.recruiting.applicant.business.AdminApplicantService
import com.yourssu.scouter.recruiting.applicant.business.ApplicantService
import com.yourssu.scouter.recruiting.applicant.business.dto.CreateApplicantCommand
import com.yourssu.scouter.recruiting.applicant.business.dto.UpdateApplicantCommand
import com.yourssu.scouter.recruiting.support.business.utils.ApplicantStateConverter
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate

@Controller
@RequestMapping("/admin/recruiting/applicants")
class AdminApplicantController(
    private val partService: PartService,
    private val semesterService: SemesterService,
    private val departmentService: DepartmentService,
    private val applicantService: ApplicantService,
    private val adminApplicantService: AdminApplicantService,
) {

    @GetMapping
    fun applicantPage(
        @RequestParam(required = false, defaultValue = "false") filtered: Boolean,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) semesterId: Long?,
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false) applicantId: Long?,
        model: Model,
    ): String {
        val semesters = semesterService.readAllByReverseOrder()
        // 필터를 한 번도 적용하지 않은 첫 진입에서만 현재 학기를 기본값으로 넣는다.
        // 목록 최상단은 미래 학기라 기본값으로 쓰면 빈 목록이 나오고, 필터를 거친 뒤의 null 은 "전체 학기"라는 뜻이다.
        val resolvedSemesterId = if (filtered) {
            semesterId
        } else {
            runCatching { semesterService.readByDate(LocalDate.now()).id }.getOrNull() ?: semesters.firstOrNull()?.id
        }

        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("semesters", semesters)
        model.addAttribute("departments", departmentService.readAll().departmentDtos)
        model.addAttribute("states", applicantService.readAllStates())
        model.addAttribute("name", name)
        model.addAttribute("state", state)
        model.addAttribute("semesterId", resolvedSemesterId)
        model.addAttribute("partId", partId)
        model.addAttribute("applicantId", applicantId)

        runCatching {
            applicantService.readAllByFilters(
                name = name?.takeIf(String::isNotBlank),
                states = state?.takeIf(String::isNotBlank)?.let(::listOf),
                semesterId = resolvedSemesterId,
                partId = partId,
            )
        }.onSuccess { model.addAttribute("applicants", it) }
            .onFailure { model.addAttribute("error", it.message ?: "지원자 목록을 불러오지 못했습니다.") }

        if (applicantId != null) {
            runCatching {
                val applicant = applicantService.readById(applicantId)
                model.addAttribute("applicant", applicant)
                model.addAttribute("answers", adminApplicantService.readAnswers(applicantId))
                model.addAttribute("sections", adminApplicantService.readSections(applicant.part.id))
            }.onFailure { model.addAttribute("error", it.message ?: "지원자를 불러오지 못했습니다.") }
        }

        return "admin/applicants"
    }

    @PostMapping("/create")
    @ResponseBody
    fun create(
        @RequestParam name: String,
        @RequestParam email: String,
        @RequestParam phoneNumber: String,
        @RequestParam age: String,
        @RequestParam departmentId: Long,
        @RequestParam studentId: String,
        @RequestParam partId: Long,
        @RequestParam state: String,
        @RequestParam applicationDate: String,
        @RequestParam applicationSemesterId: Long,
        @RequestParam academicSemester: String,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching {
            applicantService.create(
                CreateApplicantCommand(
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    age = age,
                    departmentId = departmentId,
                    studentId = studentId,
                    partId = partId,
                    state = ApplicantStateConverter.convertToEnum(state),
                    applicationDate = LocalDate.parse(applicationDate),
                    applicationSemesterId = applicationSemesterId,
                    academicSemester = academicSemester,
                    // 어드민에서 만든 지원자는 면접 가능 시간이 없다. 스케줄 화면에서 따로 넣는다.
                    availableTimes = emptyList(),
                ),
            )
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true, "applicantId" to it)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "추가에 실패했습니다."))) },
        )
    }

    /**
     * 빈 값은 "그대로 둔다"는 뜻이다. UpdateApplicantCommand 가 null 을 그렇게 다룬다.
     * applicationDate 와 availableTimes 는 폼에서 받지 않는다.
     * 전자는 Command 가 날짜만 받아 동기화된 지원 시각을 UTC 자정으로 잘라내고, 후자는 빈 값이 곧 전체 삭제라서 실수 한 번에 데이터가 사라진다.
     */
    @PostMapping("/update")
    @ResponseBody
    fun update(
        @RequestParam applicantId: Long,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) phoneNumber: String?,
        @RequestParam(required = false) age: String?,
        @RequestParam(required = false) departmentId: Long?,
        @RequestParam(required = false) studentId: String?,
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) applicationSemesterId: Long?,
        @RequestParam(required = false) academicSemester: String?,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching {
            applicantService.updateById(
                UpdateApplicantCommand(
                    targetApplicantId = applicantId,
                    name = name?.takeIf(String::isNotBlank),
                    email = email?.takeIf(String::isNotBlank),
                    phoneNumber = phoneNumber?.takeIf(String::isNotBlank),
                    age = age?.takeIf(String::isNotBlank),
                    departmentId = departmentId,
                    studentId = studentId?.takeIf(String::isNotBlank),
                    partId = partId,
                    state = state?.takeIf(String::isNotBlank)?.let(ApplicantStateConverter::convertToEnum),
                    applicationDate = null,
                    applicationSemesterId = applicationSemesterId,
                    academicSemester = academicSemester?.takeIf(String::isNotBlank),
                    availableTimes = null,
                ),
            )
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "수정에 실패했습니다."))) },
        )
    }

    @PostMapping("/delete")
    @ResponseBody
    fun delete(
        @RequestParam applicantId: Long,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { adminApplicantService.deleteWithRelatedData(applicantId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "삭제에 실패했습니다."))) },
        )
    }

    @PostMapping("/answers/create")
    @ResponseBody
    fun createAnswer(
        @RequestParam applicantId: Long,
        @RequestParam question: String,
        @RequestParam answer: String,
        @RequestParam(required = false) sectionId: Long?,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { adminApplicantService.addAnswer(applicantId, question, answer, sectionId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true, "answerId" to it.id)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "답변 추가에 실패했습니다."))) },
        )
    }

    @PostMapping("/answers/update")
    @ResponseBody
    fun updateAnswer(
        @RequestParam answerId: Long,
        @RequestParam question: String,
        @RequestParam answer: String,
        @RequestParam(required = false) sectionId: Long?,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { adminApplicantService.updateAnswer(answerId, question, answer, sectionId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "답변 수정에 실패했습니다."))) },
        )
    }

    @PostMapping("/answers/delete")
    @ResponseBody
    fun deleteAnswer(
        @RequestParam answerId: Long,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { adminApplicantService.deleteAnswer(answerId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "답변 삭제에 실패했습니다."))) },
        )
    }
}
