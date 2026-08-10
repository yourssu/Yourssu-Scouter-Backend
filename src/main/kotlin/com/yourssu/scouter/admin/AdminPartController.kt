package com.yourssu.scouter.admin

import com.yourssu.scouter.common.part.business.PartService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/admin/recruiting")
class AdminPartController(
    private val partService: PartService,
) {

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
}
