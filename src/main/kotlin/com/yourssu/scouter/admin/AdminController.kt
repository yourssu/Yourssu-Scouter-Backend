package com.yourssu.scouter.admin

import jakarta.servlet.http.HttpSession
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin/recruiting")
@EnableConfigurationProperties(RecruitingAdminProperties::class)
class AdminController(
    private val properties: RecruitingAdminProperties,
) {

    @GetMapping
    fun home(session: HttpSession): String {
        if (session.getAttribute(AdminConstants.RECRUITING_ADMIN_SESSION_KEY) == true) {
            return "redirect:/admin/recruiting/parts"
        }
        return "admin/recruiting-login"
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
            return "redirect:/admin/recruiting"
        }
        if (secureEquals(password.trim(), configured)) {
            session.setAttribute(AdminConstants.RECRUITING_ADMIN_SESSION_KEY, true)
            return "redirect:/admin/recruiting"
        }
        redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.")
        return "redirect:/admin/recruiting"
    }

    private fun secureEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(StandardCharsets.UTF_8), b.toByteArray(StandardCharsets.UTF_8))
}
