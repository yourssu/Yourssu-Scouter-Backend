package com.yourssu.scouter.hrms.application.domain.me

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.business.domain.me.MeResult
import com.yourssu.scouter.hrms.business.domain.me.MeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "내 정보")
@RestController
class MeController(
    private val meService: MeService,
) {

    @Operation(
        summary = "내 정보 조회",
        description = "액세스 토큰으로 현재 로그인한 사용자의 멤버 정보와 프로필 이미지를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "Member-005 (등록된 멤버가 아닙니다)"),
        ]
    )
    @GetMapping("/members/me")
    fun getMe(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<MeResponse> {
        val meResult: MeResult = meService.getMe(authUserInfo.userId)
        val response: MeResponse = MeResponse.from(meResult)

        return ResponseEntity.ok(response)
    }
}
