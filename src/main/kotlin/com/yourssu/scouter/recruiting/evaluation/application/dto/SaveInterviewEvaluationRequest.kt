package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.SaveInterviewEvaluationCommand
import com.yourssu.scouter.recruiting.evaluation.business.dto.SaveInterviewEvaluationItemCommand
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Schema(description = "면접 평가 항목별 점수 저장 요청")
data class SaveInterviewEvaluationItemRequest(
    @field:NotNull
    @field:Schema(description = "면접 루브릭 평가 항목 ID", example = "101", nullable = false)
    val itemId: Long?,

    @field:NotNull
    @field:Schema(description = "해당 평가 항목에 부여한 점수. 항목별 최대 배점을 초과할 수 없습니다.", example = "8", minimum = "1", nullable = false)
    val score: Int?,
) {
    fun toCommand(): SaveInterviewEvaluationItemCommand = SaveInterviewEvaluationItemCommand(
        evaluationItemId = itemId!!,
        score = score!!,
    )
}

@Schema(description = "면접 평가 제출 및 결과 상태, 제출 상태 변경")
data class SaveInterviewEvaluationRequest(
    @field:NotEmpty(message = "평가 항목 점수 리스트는 비어있을 수 없습니다.")
    @field:Schema(description = "루브릭 항목별 점수 목록. 비어 있을 수 없습니다.", nullable = false)
    val items: List<SaveInterviewEvaluationItemRequest> = emptyList(),

    @field:Schema(description = "면접 전반에 대한 종합 의견", example = "질문에 대한 답변이 구조적이고 문화 적합성이 높습니다.", nullable = false)
    val overallComment: String = "",

    @field:NotNull
    @field:Schema(description = "면접 평가 결과", example = "FINAL_PASS", allowableValues = ["PENDING", "FINAL_PASS", "INTERVIEW_FAIL"], nullable = false)
    val result: InterviewResult?,

    @field:NotNull
    @field:Schema(description = "최종 제출 여부. false는 기본 상태, true는 최종 제출입니다.", example = "true", nullable = false)
    val submit: Boolean?,
) {
    fun toCommand(applicantId: Long, evaluatorUserId: Long): SaveInterviewEvaluationCommand = SaveInterviewEvaluationCommand(
        applicantId = applicantId,
        evaluatorUserId = evaluatorUserId,
        items = items.map { it.toCommand() },
        overallComment = overallComment,
        result = result!!,
        submit = submit!!,
    )
}
