package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.mail.template.MailTemplateService
import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "메일")
@RestController
@RequestMapping("/api/mails/templates")
class MailTemplateController(
    private val mailTemplateService: MailTemplateService
) {

    @Operation(
        summary = "메일 템플릿 생성",
        description = """
            제목과 양식을 지정해 새로운 메일 템플릿을 생성합니다.

            **변수 규칙:**
            - 모든 변수 키는 `var-{숫자}` 형식이어야 합니다 (예: `var-1762579979965`)
            - 메일 본문에서 변수는 `{{var-{숫자}}}` 형식으로 사용합니다

            **변수 타입:**
            - **사용자 입력 변수** (`requiresUserInput: true`): PERSON, DATE, LINK, TEXT
            - **자동 채움 변수** (`requiresUserInput: false`): APPLICANT, PARTNAME

            **주의사항:**
            - `requiresUserInput`과 `type`의 조합이 올바른지 검증됩니다
            - 사용자 입력 변수는 PERSON, DATE, LINK, TEXT만 사용 가능
            - 자동 채움 변수는 APPLICANT, PARTNAME만 사용 가능
        """.trimIndent()
    )
    @PostMapping
    fun create(
        @AuthUser authUserInfo: AuthUserInfo,
        @Valid @RequestBody request: CreateMailTemplateRequest,
    ): ResponseEntity<CreateMailTemplateResponse> {
        val domain: MailTemplate = request.toDomain(createdBy = authUserInfo.userId)
        val saved: MailTemplate = mailTemplateService.createTemplate(domain)
        return ResponseEntity.status(201).body(CreateMailTemplateResponse.from(saved))
    }

    @Operation(summary = "메일 템플릿 목록 조회", description = "현재 등록된 전체 메일 템플릿을 조회합니다.")
    @GetMapping
    fun readAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false, defaultValue = "updatedAt,desc") sort: String,
    ): ResponseEntity<PageResponse<ReadMailTemplateSummaryResponse>> {
        val sortParts = sort.split(",")
        val pageable = PageRequest.of(
            page, size, org.springframework.data.domain.Sort.by(
                if (sortParts.size >= 2 && sortParts[1].equals(
                        "desc",
                        true
                    )
                ) org.springframework.data.domain.Sort.Order.desc(sortParts[0])
                else org.springframework.data.domain.Sort.Order.asc(sortParts[0])
            )
        )

        val pageResult = mailTemplateService.readTemplates(pageable)
        val response = PageResponse(
            content = pageResult.content.map { ReadMailTemplateSummaryResponse.from(it) },
            page = pageResult.number,
            size = pageResult.size,
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
        )
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "메일 템플릿 상세 조회", description = "특정 메일 템플릿의 상세 내용을 조회합니다. (편집용)")
    @GetMapping("/{templateId}")
    fun readDetail(
        @Parameter(description = "템플릿 ID")
        @PathVariable templateId: Long,
    ): ResponseEntity<ReadMailTemplateDetailResponse> {
        val template = mailTemplateService.readTemplate(templateId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ReadMailTemplateDetailResponse.from(template))
    }

    @Operation(
        summary = "메일 템플릿 수정",
        description = """
            특정 메일 템플릿을 수정합니다. 전체 메일 템플릿의 내용을 보내야 합니다.

            **변수 규칙:**
            - 모든 변수 키는 `var-{숫자}` 형식이어야 합니다 (예: `var-1762579979965`)
            - `requiresUserInput`과 `type`의 조합이 올바른지 검증됩니다
        """.trimIndent()
    )
    @PutMapping("/{templateId}")
    fun update(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable templateId: Long,
        @Valid @RequestBody request: CreateMailTemplateRequest,
    ): ResponseEntity<CreateMailTemplateResponse> {
        val domain: MailTemplate = request.toDomain(createdBy = authUserInfo.userId)
        val updated = mailTemplateService.updateTemplate(templateId, domain) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(CreateMailTemplateResponse.from(updated))
    }

    @Operation(summary = "메일 템플릿 삭제", description = "특정 메일 템플릿을 삭제합니다. 해당 메일 템플릿이 없을 시 404를 반환합니다.")
    @ApiResponses(
        ApiResponse(description = "No Content", responseCode = "204"),
        ApiResponse(description = "Not Found - 템플릿을 찾을 수 없음", responseCode = "404")
    )
    @DeleteMapping("/{templateId}")
    fun delete(
        @PathVariable templateId: Long,
    ): ResponseEntity<Unit> {
        val deleted = mailTemplateService.deleteTemplate(templateId)
        return if (deleted) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
    }
}
