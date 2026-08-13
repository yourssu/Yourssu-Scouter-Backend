package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.part.business.PartService
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementItemRequest
import com.yourssu.scouter.common.support.business.utils.SemesterConverter
import java.time.LocalDate
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementRequest
import com.yourssu.scouter.recruiting.interview.business.InterviewRequirementService
import com.yourssu.scouter.recruiting.interview.business.dto.InterviewRequirementItemDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/admin/recruiting/requirements")
class AdminRequirementController(
    private val partService: PartService,
    private val interviewRequirementService: InterviewRequirementService,
) {

    @GetMapping("/culture-fit")
    fun cultureFit(
        @RequestParam(required = false) semester: String?,
        model: Model,
    ): String {
        val resolvedSemester = semester ?: SemesterConverter.convertToIntString(LocalDate.now())
        model.addAttribute("semester", resolvedSemester)
        runCatching { interviewRequirementService.readGlobalBySemester(Semester.of(resolvedSemester)) }
            .onSuccess { model.addAttribute("requirements", it) }
            .onFailure { model.addAttribute("error", it.message) }
        return "admin/requirements/culture-fit"
    }

    @PostMapping("/culture-fit")
    @ResponseBody
    fun saveCultureFit(
        @RequestParam semester: String,
        @ModelAttribute form: RequirementsForm,
    ): ResponseEntity<Map<String, Any?>> {
        val sem = Semester.of(semester)
        return runCatching {
            val existing = interviewRequirementService.readGlobalBySemester(sem)
            val request = UpdateInterviewRequirementRequest(
                culture = form.items.map { UpdateInterviewRequirementItemRequest(it.id, it.content) },
                team = existing.team.toUpdateItems(),
                job = existing.job.toUpdateItems(),
                other = existing.other.toUpdateItems(),
            )
            interviewRequirementService.saveAllGlobal(sem, request)
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to it.message)) },
        )
    }

    @GetMapping("/team-fit")
    fun teamFit(
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false) semester: String?,
        model: Model,
    ): String {
        val resolvedSemester = semester ?: SemesterConverter.convertToIntString(LocalDate.now())
        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("partId", partId)
        model.addAttribute("semester", resolvedSemester)
        if (partId != null) {
            runCatching { interviewRequirementService.readByPartIdAndSemester(partId, Semester.of(resolvedSemester)) }
                .onSuccess { model.addAttribute("requirements", it) }
                .onFailure { model.addAttribute("error", it.message) }
        }
        return "admin/requirements/team-fit"
    }

    @PostMapping("/team-fit")
    @ResponseBody
    fun saveTeamFit(
        @RequestParam partId: Long,
        @RequestParam semester: String,
        @ModelAttribute form: RequirementsForm,
    ): ResponseEntity<Map<String, Any?>> {
        val sem = Semester.of(semester)
        return runCatching {
            val existing = interviewRequirementService.readByPartIdAndSemester(partId, sem)
            val request = UpdateInterviewRequirementRequest(
                culture = existing.culture.toUpdateItems(),
                team = form.items.map { UpdateInterviewRequirementItemRequest(it.id, it.content) },
                job = existing.job.toUpdateItems(),
                other = existing.other.toUpdateItems(),
            )
            interviewRequirementService.saveAll(partId, sem, request)
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to it.message)) },
        )
    }

    @GetMapping("/job-fit")
    fun jobFit(
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false) semester: String?,
        model: Model,
    ): String {
        val resolvedSemester = semester ?: SemesterConverter.convertToIntString(LocalDate.now())
        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("partId", partId)
        model.addAttribute("semester", resolvedSemester)
        if (partId != null) {
            runCatching { interviewRequirementService.readByPartIdAndSemester(partId, Semester.of(resolvedSemester)) }
                .onSuccess { model.addAttribute("requirements", it) }
                .onFailure { model.addAttribute("error", it.message) }
        }
        return "admin/requirements/job-fit"
    }

    @PostMapping("/job-fit")
    @ResponseBody
    fun saveJobFit(
        @RequestParam partId: Long,
        @RequestParam semester: String,
        @ModelAttribute form: RequirementsForm,
    ): ResponseEntity<Map<String, Any?>> {
        val sem = Semester.of(semester)
        return runCatching {
            val existing = interviewRequirementService.readByPartIdAndSemester(partId, sem)
            val request = UpdateInterviewRequirementRequest(
                culture = existing.culture.toUpdateItems(),
                team = existing.team.toUpdateItems(),
                job = form.items.map { UpdateInterviewRequirementItemRequest(it.id, it.content) },
                other = existing.other.toUpdateItems(),
            )
            interviewRequirementService.saveAll(partId, sem, request)
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to it.message)) },
        )
    }

    private fun List<InterviewRequirementItemDto>.toUpdateItems() =
        map { UpdateInterviewRequirementItemRequest(it.id, it.content) }

    data class RequirementItemParam(val id: Long? = null, val content: String = "")

    data class RequirementsForm(val items: List<RequirementItemParam> = emptyList())
}
