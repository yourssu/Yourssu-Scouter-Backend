package com.yourssu.scouter.admin

import com.yourssu.scouter.common.part.business.PartService
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
    private val properties: RecruitingAdminProperties,
) {
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

    private fun isAuthorized(session: HttpSession): Boolean =
        session.getAttribute(AdminConstants.RECRUITING_ADMIN_SESSION_KEY) == true

    private fun secureEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(StandardCharsets.UTF_8), b.toByteArray(StandardCharsets.UTF_8))
}
