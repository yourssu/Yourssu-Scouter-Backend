package com.yourssu.scouter.mail.application.template.dto

import com.yourssu.scouter.mail.implement.message.MailAttachmentReference
import com.yourssu.scouter.mail.implement.template.MailTemplate
import com.yourssu.scouter.mail.implement.template.TemplateVariable
import com.yourssu.scouter.mail.implement.template.VariableType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "메일 템플릿 상세 정보")
data class ReadMailTemplateDetailResponse(
    @field:Schema(description = "템플릿 ID", example = "1")
    val id: Long,
    @field:Schema(description = "템플릿 제목", example = "합격 안내 메일")
    val title: String,
    @field:Schema(
        description = "메일 제목. 변수는 {{var-{UUID}}} 형식으로 사용",
        example = "[유어슈] {{var-550e8400-e29b-41d4-a716-446655440000}}님 면접 안내",
    )
    val subject: String,
    @field:Schema(
        description = "메일 본문 HTML",
        example = "<p>안녕하세요 {{var-550e8400-e29b-41d4-a716-446655440000}}님</p>",
    )
    val bodyHtml: String,
    @field:Schema(description = "템플릿 변수 목록")
    val variables: List<DetailVariable>,
    @field:Schema(description = "첨부파일 참조 목록")
    val attachmentReferences: List<AttachmentReference>,
    @field:Schema(description = "최종 수정 시간")
    val updatedAt: java.time.Instant,
) {
    @Schema(description = "템플릿 변수 정보")
    data class DetailVariable(
        @field:Schema(
            description = "변수 키. var-{UUID} 형식",
            example = "var-550e8400-e29b-41d4-a716-446655440000",
        )
        val key: String,
        @field:Schema(
            description = "변수 타입. PERSON, DATE, LINK, TEXT는 사용자 입력 변수. APPLICANT, PARTNAME은 자동 채움 변수",
            example = "TEXT",
            allowableValues = ["PERSON", "DATE", "LINK", "TEXT", "APPLICANT", "PARTNAME"],
        )
        val type: VariableType,
        @field:Schema(description = "변수 표시 이름", example = "면접 일시")
        val displayName: String,
        @field:Schema(description = "수신자별로 다른 값 입력 여부. UserInput 타입에만 유효", example = "true")
        val perRecipient: Boolean,
        @field:Schema(
            description = "APPLICANT 타입 변수의 속성 키",
            example = "applicant.name",
            nullable = true,
        )
        val attributeKey: String? = null,
    ) {
        companion object {
            fun from(variable: TemplateVariable): DetailVariable = when (variable) {
                is TemplateVariable.UserInput -> DetailVariable(
                    key = variable.key,
                    type = variable.type,
                    displayName = variable.displayName,
                    perRecipient = variable.perRecipient,
                    attributeKey = null,
                )
                is TemplateVariable.ApplicantBound -> DetailVariable(
                    key = variable.key,
                    type = VariableType.APPLICANT,
                    displayName = variable.displayName,
                    perRecipient = false,
                    attributeKey = variable.attributeKey,
                )
                is TemplateVariable.PartName -> DetailVariable(
                    key = variable.key,
                    type = VariableType.PARTNAME,
                    displayName = variable.displayName,
                    perRecipient = false,
                    attributeKey = null,
                )
            }
        }
    }

    data class AttachmentReference(
        @field:Schema(description = "업로드된 파일 ID", example = "11", nullable = true)
        val fileId: Long?,
        @field:Schema(description = "파일명", example = "guide.pdf")
        val fileName: String,
        @field:Schema(description = "파일 MIME 타입", example = "application/pdf")
        val contentType: String,
        @field:Schema(description = "S3 저장 키", example = "mail-files/attachment/uuid-guide.pdf")
        val storageKey: String,
    ) {
        companion object {
            fun from(reference: MailAttachmentReference): AttachmentReference {
                return AttachmentReference(
                    fileId = reference.fileId,
                    fileName = reference.fileName,
                    contentType = reference.contentType,
                    storageKey = reference.storageKey,
                )
            }
        }
    }

    companion object {
        fun from(template: MailTemplate): ReadMailTemplateDetailResponse =
            ReadMailTemplateDetailResponse(
                id = template.id!!,
                title = template.title,
                subject = template.subject.raw,
                bodyHtml = template.bodyHtml,
                variables = template.variables.map { DetailVariable.from(it) },
                attachmentReferences = template.attachmentReferences.map { AttachmentReference.from(it) },
                updatedAt = template.updatedAt!!,
            )
    }
}
