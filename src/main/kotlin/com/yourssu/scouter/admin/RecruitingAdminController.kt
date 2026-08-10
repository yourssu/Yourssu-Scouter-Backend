package com.yourssu.scouter.admin

import com.yourssu.scouter.common.part.business.PartService
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementItemRequest
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementRequest
import com.yourssu.scouter.recruiting.interview.business.InterviewRequirementService
import com.yourssu.scouter.recruiting.interview.business.dto.InterviewRequirementItemDto
import jakarta.servlet.http.HttpSession
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin/recruiting")
@EnableConfigurationProperties(RecruitingAdminProperties::class)
class RecruitingAdminController(
    private val partService: PartService,
    private val interviewRequirementService: InterviewRequirementService,
    private val properties: RecruitingAdminProperties,
) {

    @GetMapping("")
    fun home(session: HttpSession): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        return "redirect:/admin/recruiting/parts"
    }

    @PostMapping("/auth")
    fun auth(
        @RequestParam password: String,
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        val configured = properties.password.trim()
        if (configured.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "비밀번호가 설정되지 않았습니다. scouter.recruiting-admin.password를 설정해주세요.")
            return "redirect:/admin/recruiting/parts"
        }
        if (secureEquals(password.trim(), configured)) {
            session.setAttribute(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
            return "redirect:/admin/recruiting/parts"
        }
        redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.")
        return "redirect:/admin/recruiting/parts"
    }

    @GetMapping("/parts")
    fun parts(model: Model, session: HttpSession): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        model.addAttribute("parts", partService.readAll().partDtos)
        return "admin/part-assignments"
    }

    @ResponseBody
    @PostMapping("/parts/{partId}/assignments/toggle")
    fun toggleAssignment(
        @PathVariable partId: Long,
        session: HttpSession,
    ): ResponseEntity<Unit> {
        if (!isAuthorized(session)) return ResponseEntity.status(401).build()
        partService.toggleAssignment(partId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/requirements/culture-fit")
    fun cultureFit(
        @RequestParam(required = false) semester: String?,
        model: Model,
        session: HttpSession,
    ): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        model.addAttribute("semester", semester)
        if (semester != null) {
            runCatching { interviewRequirementService.readGlobalBySemester(Semester.of(semester)) }
                .onSuccess { model.addAttribute("requirements", it) }
                .onFailure { model.addAttribute("error", it.message) }
        }
        return "admin/requirements/culture-fit"
    }

    @PostMapping("/requirements/culture-fit")
    fun saveCultureFit(
        @RequestParam semester: String,
        @RequestParam(required = false, defaultValue = "") items: List<RequirementItemParam>,
        model: Model,
        session: HttpSession,
    ): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        val sem = Semester.of(semester)
        runCatching {
            val existing = interviewRequirementService.readGlobalBySemester(sem)
            val request = UpdateInterviewRequirementRequest(
                culture = items.map { UpdateInterviewRequirementItemRequest(it.id, it.content) },
                team = existing.team.toUpdateItems(),
                job = existing.job.toUpdateItems(),
                other = existing.other.toUpdateItems(),
            )
            interviewRequirementService.saveAllGlobal(sem, request)
        }.onSuccess {
            model.addAttribute("success", true)
            model.addAttribute("requirements", interviewRequirementService.readGlobalBySemester(sem))
        }.onFailure {
            model.addAttribute("error", it.message)
            runCatching { interviewRequirementService.readGlobalBySemester(sem) }
                .onSuccess { r -> model.addAttribute("requirements", r) }
        }
        model.addAttribute("semester", semester)
        return "admin/requirements/culture-fit"
    }

    @GetMapping("/requirements/team-fit")
    fun teamFit(
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false) semester: String?,
        model: Model,
        session: HttpSession,
    ): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("partId", partId)
        model.addAttribute("semester", semester)
        if (partId != null && semester != null) {
            runCatching { interviewRequirementService.readByPartIdAndSemester(partId, Semester.of(semester)) }
                .onSuccess { model.addAttribute("requirements", it) }
                .onFailure { model.addAttribute("error", it.message) }
        }
        return "admin/requirements/team-fit"
    }

    @PostMapping("/requirements/team-fit")
    fun saveTeamFit(
        @RequestParam partId: Long,
        @RequestParam semester: String,
        @RequestParam(required = false, defaultValue = "") items: List<RequirementItemParam>,
        model: Model,
        session: HttpSession,
    ): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        val sem = Semester.of(semester)
        runCatching {
            val existing = interviewRequirementService.readByPartIdAndSemester(partId, sem)
            val request = UpdateInterviewRequirementRequest(
                culture = existing.culture.toUpdateItems(),
                team = items.map { UpdateInterviewRequirementItemRequest(it.id, it.content) },
                job = existing.job.toUpdateItems(),
                other = existing.other.toUpdateItems(),
            )
            interviewRequirementService.saveAll(partId, sem, request)
        }.onSuccess {
            model.addAttribute("success", true)
            model.addAttribute("requirements", interviewRequirementService.readByPartIdAndSemester(partId, sem))
        }.onFailure {
            model.addAttribute("error", it.message)
            runCatching { interviewRequirementService.readByPartIdAndSemester(partId, sem) }
                .onSuccess { r -> model.addAttribute("requirements", r) }
        }
        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("partId", partId)
        model.addAttribute("semester", semester)
        return "admin/requirements/team-fit"
    }

    @GetMapping("/requirements/job-fit")
    fun jobFit(
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false) semester: String?,
        model: Model,
        session: HttpSession,
    ): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("partId", partId)
        model.addAttribute("semester", semester)
        if (partId != null && semester != null) {
            runCatching { interviewRequirementService.readByPartIdAndSemester(partId, Semester.of(semester)) }
                .onSuccess { model.addAttribute("requirements", it) }
                .onFailure { model.addAttribute("error", it.message) }
        }
        return "admin/requirements/job-fit"
    }

    @PostMapping("/requirements/job-fit")
    fun saveJobFit(
        @RequestParam partId: Long,
        @RequestParam semester: String,
        @RequestParam(required = false, defaultValue = "") items: List<RequirementItemParam>,
        model: Model,
        session: HttpSession,
    ): String {
        if (!isAuthorized(session)) return "admin/recruiting-login"
        val sem = Semester.of(semester)
        runCatching {
            val existing = interviewRequirementService.readByPartIdAndSemester(partId, sem)
            val request = UpdateInterviewRequirementRequest(
                culture = existing.culture.toUpdateItems(),
                team = existing.team.toUpdateItems(),
                job = items.map { UpdateInterviewRequirementItemRequest(it.id, it.content) },
                other = existing.other.toUpdateItems(),
            )
            interviewRequirementService.saveAll(partId, sem, request)
        }.onSuccess {
            model.addAttribute("success", true)
            model.addAttribute("requirements", interviewRequirementService.readByPartIdAndSemester(partId, sem))
        }.onFailure {
            model.addAttribute("error", it.message)
            runCatching { interviewRequirementService.readByPartIdAndSemester(partId, sem) }
                .onSuccess { r -> model.addAttribute("requirements", r) }
        }
        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("partId", partId)
        model.addAttribute("semester", semester)
        return "admin/requirements/job-fit"
    }

    private fun isAuthorized(session: HttpSession): Boolean =
        session.getAttribute(AdminConstants.RECRUITING_ADMIN_SESSION_KEY) == true

    private fun secureEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(StandardCharsets.UTF_8), b.toByteArray(StandardCharsets.UTF_8))

    private fun List<InterviewRequirementItemDto>.toUpdateItems() =
        map { UpdateInterviewRequirementItemRequest(it.id, it.content) }

    data class RequirementItemParam(val id: Long?, val content: String)
}
