package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailFileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "메일")
@RestController
@RequestMapping("/api/mails/images")
class MailImageController(
    private val mailFileService: MailFileService,
) {
    @Operation(
        summary = "메일 이미지 조회 (302 Redirect)",
        description =
            "S3에 업로드된 이미지 파일의 presigned GET URL을 생성하고, 해당 URL로 302 리다이렉트합니다.\n\n" +
                "**공개 엔드포인트:**\n" +
                "- 인증 없이 접근 가능합니다.\n\n" +
                "**사용 방법:**\n" +
                "- `<img src=\"/api/mails/images?cid=xxx\">` 형태로 HTML에서 직접 사용 가능합니다.\n\n" +
                "**Presigned URL:**\n" +
                "- 생성된 URL은 10분간 유효합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "302", description = "Found - S3 presigned URL로 리다이렉트"),
        ApiResponse(responseCode = "404", description = "Not Found - 파일을 찾을 수 없거나 삭제된 파일"),
    )
    @GetMapping
    fun redirectToImage(
        @Parameter(description = "이미지 콘텐츠 ID") @RequestParam cid: String,
        response: HttpServletResponse,
    ) {
        val presignedGetUrl = mailFileService.createPresignedGetUrl(cid)
        response.sendRedirect(presignedGetUrl)
    }
}
