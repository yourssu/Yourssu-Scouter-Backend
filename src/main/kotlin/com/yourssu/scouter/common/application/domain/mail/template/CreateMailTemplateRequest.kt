package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateMailTemplateRequest(
    @field:NotBlank
    @field:Schema(description = "메일 템플릿 제목", example = "합격 안내 메일")
    val title: String,
    @field:NotBlank
    @field:Size(max = 100_000, message = "메일 템플릿 본문은 최대 100000자까지 입력 가능합니다")
    @field:Schema(
        description = "메일 본문 HTML. 변수는 {{var-{UUID}}} 형식으로 사용",
        example = "<p>안녕하세요 {{var-550e8400-e29b-41d4-a716-446655440000}}님</p>"
    )
    val bodyHtml: String,
    @field:NotNull
    @field:Schema(description = "템플릿 변수 목록")
    val variables: List<TemplateVariableRequest> = emptyList(),
) {
    @Schema(description = "템플릿 변수 정보")
    data class TemplateVariableRequest(
        @field:Schema(
            description = "변수 키. var-{UUID} 형식이어야 함 (예: var-550e8400-e29b-41d4-a716-446655440000)",
            example = "var-550e8400-e29b-41d4-a716-446655440000",
            pattern = "^var-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        )
        val key: String,
        @field:Schema(
            description = "변수 타입. PERSON, DATE, LINK, TEXT는 사용자 입력 변수. APPLICANT, PARTNAME은 자동 채움 변수",
            example = "TEXT",
            allowableValues = ["PERSON", "DATE", "LINK", "TEXT", "APPLICANT", "PARTNAME"]
        )
        val type: VariableType,
        @field:Schema(description = "변수 표시 이름", example = "면접 일시")
        val displayName: String,
        @field:Schema(description = "수신자별로 다른 값 입력 여부", example = "true")
        val perRecipient: Boolean,
    )

    fun toDomain(createdBy: Long): MailTemplate {
        return MailTemplate(
            title = title,
            bodyHtml = bodyHtml,
            variables = variables.map { TemplateVariable(it.key, it.type, it.displayName, it.perRecipient) },
            createdBy = createdBy,
        )
    }
}
