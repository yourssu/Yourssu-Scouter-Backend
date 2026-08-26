package com.yourssu.scouter.recruiting.applicant.application.dto

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantWebhookCommand
import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantWebhookItemCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class ApplicantWebhookRequest(

    @field:NotBlank(message = "formId를 입력하지 않았습니다.")
    val formId: String,

    @field:NotBlank(message = "responseId를 입력하지 않았습니다.")
    val responseId: String,

    @field:NotNull(message = "제출 시각을 입력하지 않았습니다.")
    val createTime: Instant,

    val respondentEmail: String?,

    @field:NotNull(message = "문항 목록을 입력하지 않았습니다.")
    @field:Valid
    val items: List<ApplicantWebhookItemRequest>,
) {

    fun toCommand(): ApplicantWebhookCommand = ApplicantWebhookCommand(
        formId = formId,
        responseId = responseId,
        createTime = createTime,
        respondentEmail = respondentEmail,
        items = items.map { it.toCommand() },
    )
}

data class ApplicantWebhookItemRequest(

    // 폼에 배치된 문항 순서 그대로 담겨 있어야 한다 (items 배열 순서를 그대로 신뢰한다).
    @field:NotBlank(message = "문항 제목을 입력하지 않았습니다.")
    val question: String,

    val answer: String,

    // 구글 폼 문항 타입이 장문형(단락)인지 여부. Apps Script의 item.getType() == PARAGRAPH_TEXT
    val isDescriptive: Boolean = false,
) {

    fun toCommand(): ApplicantWebhookItemCommand = ApplicantWebhookItemCommand(
        question = question,
        answer = answer,
        isDescriptive = isDescriptive,
    )
}
