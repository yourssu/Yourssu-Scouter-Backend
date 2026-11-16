package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.part.business.PartService
import com.yourssu.scouter.member.core.business.AdminMemberPartService
import com.yourssu.scouter.member.core.implement.MemberState
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/admin/recruiting/member-parts")
class AdminMemberPartController(
    private val partService: PartService,
    private val adminMemberPartService: AdminMemberPartService,
) {

    @GetMapping
    fun memberPartPage(
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
        model: Model,
    ): String {
        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("states", MemberState.entries.map { it.name })
        model.addAttribute("search", search)
        model.addAttribute("partId", partId)

        val resolvedState = runCatching { MemberState.valueOf(state ?: MemberState.ACTIVE.name) }
            .getOrElse {
                model.addAttribute("error", "허용되지 않는 상태 값입니다: $state")
                MemberState.ACTIVE
            }
        model.addAttribute("state", resolvedState.name)

        runCatching { adminMemberPartService.readAllByFilters(resolvedState, search, partId) }
            .onSuccess { model.addAttribute("members", it) }
            .onFailure { model.addAttribute("error", it.message ?: "멤버 목록을 불러오지 못했습니다.") }

        return "admin/member-parts"
    }

    @PostMapping("/assign")
    @ResponseBody
    fun assign(
        @RequestParam memberId: Long,
        @RequestParam partId: Long,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { adminMemberPartService.assignPart(memberId, partId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "배정에 실패했습니다."))) },
        )
    }

    @PostMapping("/unassign")
    @ResponseBody
    fun unassign(
        @RequestParam memberId: Long,
        @RequestParam partId: Long,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { adminMemberPartService.unassignPart(memberId, partId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "해제에 실패했습니다."))) },
        )
    }
}
