package com.yourssu.scouter.admin

import com.yourssu.scouter.auth.authorization.business.AuthorizationService
import com.yourssu.scouter.auth.authorization.implement.Role
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/admin/recruiting/roles")
class AdminRoleController(
    private val authorizationService: AuthorizationService,
) {

    @GetMapping
    fun rolePage(model: Model): String {
        runCatching { authorizationService.readHolders(Role.SCOUTER_ADMIN) }
            .onSuccess { model.addAttribute("holders", it) }
            .onFailure { model.addAttribute("error", it.message ?: "권한 목록을 불러오지 못했습니다.") }

        return "admin/roles"
    }

    @PostMapping("/grant")
    @ResponseBody
    fun grant(
        @RequestParam email: String,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { authorizationService.grant(email.trim(), Role.SCOUTER_ADMIN) }.fold(
            onSuccess = { granted -> ResponseEntity.ok(mapOf("success" to true, "changed" to granted)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "부여에 실패했습니다."))) },
        )
    }

    @PostMapping("/revoke")
    @ResponseBody
    fun revoke(
        @RequestParam email: String,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { authorizationService.revoke(email.trim(), Role.SCOUTER_ADMIN) }.fold(
            onSuccess = { revoked -> ResponseEntity.ok(mapOf("success" to true, "changed" to revoked)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "회수에 실패했습니다."))) },
        )
    }
}
