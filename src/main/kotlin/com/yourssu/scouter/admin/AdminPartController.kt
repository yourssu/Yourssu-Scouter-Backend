package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.part.business.PartService
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
    fun parts(model: Model): String {
        model.addAttribute("parts", partService.readAll().partDtos)
        return "admin/part-assignments"
    }

    @ResponseBody
    @PostMapping("/parts/{partId}/assignments/toggle")
    fun toggleAssignment(@PathVariable partId: Long): ResponseEntity<Unit> {
        partService.toggleAssignment(partId)
        return ResponseEntity.ok().build()
    }
}
