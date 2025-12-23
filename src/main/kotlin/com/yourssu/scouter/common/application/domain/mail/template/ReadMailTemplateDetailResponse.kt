package com.yourssu.scouter.common.application.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "메일 템플릿 상세 정보")
data class ReadMailTemplateDetailResponse(
    @field:Schema(description = "템플릿 ID", example = "1")
    val id: Long,
    @field:Schema(description = "템플릿 제목", example = "합격 안내 메일")
    val title: String,
    @field:Schema(
        description = "메일 본문 HTML",
        example = "<p>안녕하세요 {{var-1762579979965}}님</p>"
    )
    val bodyHtml: String,
    @field:Schema(description = "템플릿 변수 목록")
    val variables: List<DetailVariable>,
    @field:Schema(description = "최종 수정 시간")
    val updatedAt: java.time.LocalDateTime,
) {
    @Schema(description = "템플릿 변수 정보")
    data class DetailVariable(
        @field:Schema(
            description = "변수 키. var-{숫자} 형식",
            example = "var-1762579979965"
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
    ) {
        companion object {
            fun from(variable: TemplateVariable): DetailVariable = DetailVariable(
                key = variable.key,
                type = variable.type,
                displayName = variable.displayName,
                perRecipient = variable.perRecipient,
                requiresUserInput = variable.requiresUserInput,
            )
        }
    }

    companion object {
        fun from(template: MailTemplate): ReadMailTemplateDetailResponse = ReadMailTemplateDetailResponse(
            id = template.id!!,
            title = template.title,
            bodyHtml = template.bodyHtml,
            variables = template.variables.map { DetailVariable.from(it) },
            updatedAt = template.updatedAt!!,
        )
    }
}
