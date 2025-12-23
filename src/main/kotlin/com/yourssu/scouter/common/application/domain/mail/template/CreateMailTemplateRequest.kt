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
        description = "메일 본문 HTML. 변수는 {{var-{숫자}}} 형식으로 사용",
        example = "<p>안녕하세요 {{var-1762579979965}}님</p>"
    )
    val bodyHtml: String,
    @field:NotNull
    @field:Schema(description = "템플릿 변수 목록")
    val variables: List<TemplateVariableRequest> = emptyList(),
) {
    @Schema(description = "템플릿 변수 정보")
    data class TemplateVariableRequest(
        @field:Schema(
            description = "변수 키. var-{숫자} 형식이어야 함 (예: var-1762579979965)",
            example = "var-1762579979965",
            pattern = "^var-\\d+$"
        )
        val key: String,
        @field:Schema(
            description = "변수 타입. requiresUserInput=true일 때는 PERSON, DATE, LINK, TEXT 중 하나. requiresUserInput=false일 때는 APPLICANT, PARTNAME 중 하나",
            example = "TEXT",
            allowableValues = ["PERSON", "DATE", "LINK", "TEXT", "APPLICANT", "PARTNAME"]
        )
        val type: VariableType,
        @field:Schema(description = "변수 표시 이름", example = "면접 일시")
        val displayName: String,
        @field:Schema(description = "수신자별로 다른 값 입력 여부", example = "true")
        val perRecipient: Boolean,
        @field:Schema(
            description = "사용자 입력 필요 여부. true면 사용자가 직접 입력, false면 시스템이 자동으로 채움",
            example = "true"
        )
        val requiresUserInput: Boolean,
    )

    fun toDomain(createdBy: Long): MailTemplate {
        return MailTemplate(
            title = title,
            bodyHtml = bodyHtml,
            variables = variables.map { TemplateVariable(it.key, it.type, it.displayName, it.perRecipient, it.requiresUserInput) },
            createdBy = createdBy,
        )
    }
}
