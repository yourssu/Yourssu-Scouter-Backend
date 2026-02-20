package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailFileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
            "업로드된 이미지 파일의 S3 presigned GET URL을 생성하고, 해당 URL로 302 리다이렉트합니다.\n\n" +
                "**공개 엔드포인트:**\n" +
                "- 인증 없이 접근 가능합니다.\n" +
                "- storageKey를 통해 파일 접근을 검증합니다.\n\n" +
                "**사용 방법:**\n" +
                "- `<img src=\"/api/mails/images/{fileId}?storageKey=xxx\">` 형태로 HTML에서 직접 사용 가능합니다.\n" +
                "- 브라우저 주소창에서 직접 호출하면 이미지가 표시됩니다.\n\n" +
                "**Swagger UI 제한:**\n" +
                "- Swagger UI는 302 응답을 자동으로 따라가며, S3 도메인과의 CORS 차이로 인해 정상 동작하더라도 에러로 표시될 수 있습니다.\n" +
                "- 테스트 시에는 브라우저 주소창에서 직접 호출하거나 curl을 사용해 주세요.\n\n" +
                "**Presigned URL:**\n" +
                "- 생성된 URL은 10분간 유효합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "302", description = "Found - S3 presigned URL로 리다이렉트"),
        ApiResponse(responseCode = "403", description = "Forbidden - storageKey가 일치하지 않음"),
        ApiResponse(responseCode = "404", description = "Not Found - 파일을 찾을 수 없거나 삭제된 파일"),
    )
    @GetMapping("/{fileId}")
    fun redirectToImage(
        @Parameter(description = "파일 ID") @PathVariable fileId: Long,
        @Parameter(description = "S3 스토리지 키") @RequestParam storageKey: String,
        response: HttpServletResponse,
    ) {
        val presignedGetUrl = mailFileService.createPresignedGetUrlByStorageKey(fileId, storageKey)
        response.sendRedirect(presignedGetUrl)
    }
}
