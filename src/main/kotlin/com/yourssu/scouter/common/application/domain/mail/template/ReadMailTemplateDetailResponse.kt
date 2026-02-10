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
        example = "<p>안녕하세요 {{var-550e8400-e29b-41d4-a716-446655440000}}님</p>"
    )
    val bodyHtml: String,
    @field:Schema(description = "템플릿 변수 목록")
    val variables: List<DetailVariable>,
    @field:Schema(description = "최종 수정 시간")
    val updatedAt: java.time.Instant,
) {
    @Schema(description = "템플릿 변수 정보")
    data class DetailVariable(
        @field:Schema(
            description = "변수 키. var-{UUID} 형식",
            example = "var-550e8400-e29b-41d4-a716-446655440000"
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
    ) {
        companion object {
            fun from(variable: TemplateVariable): DetailVariable = DetailVariable(
                key = variable.key,
                type = variable.type,
                displayName = variable.displayName,
                perRecipient = variable.perRecipient,
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
