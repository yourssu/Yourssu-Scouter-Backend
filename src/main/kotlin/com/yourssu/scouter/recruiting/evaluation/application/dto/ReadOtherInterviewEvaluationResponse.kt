package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherInterviewEvaluationDto
import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherInterviewEvaluationItemDto
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "다른 면접관의 항목별 점수 정보")
data class ReadOtherInterviewEvaluationItemResponse(
    @field:Schema(description = "면접 루브릭 평가 항목 ID", example = "101", nullable = false)
    val itemId: Long,
    @field:Schema(description = "다른 면접관이 부여한 점수. 평가 미제출 시 마스킹될 수 있습니다.", example = "8", nullable = false)
    val score: Int,
) {
    companion object {
        fun from(dto: OtherInterviewEvaluationItemDto): ReadOtherInterviewEvaluationItemResponse =
            ReadOtherInterviewEvaluationItemResponse(dto.evaluationItemId, dto.score)
    }
}

@Schema(description = "다른 면접관의 면접 평가 조회 응답")
data class ReadOtherInterviewEvaluationResponse(
    @field:Schema(description = "평가자 사용자 ID", example = "23", nullable = false)
    val evaluatorId: Long,
    @field:Schema(description = "평가자 이름", example = "홍길동", nullable = false)
    val evaluatorName: String,
    @field:Schema(description = "평가 항목 점수의 합계", example = "85", nullable = false)
    val totalScore: Int,
    @field:Schema(description = "면접 평가 결과", example = "FINAL_PASS", allowableValues = ["PENDING", "FINAL_PASS", "INTERVIEW_FAIL"], nullable = false)
    val result: InterviewResult,
    @field:Schema(description = "면접 전반에 대한 종합 의견", example = "기술 이해도가 높고 협업 역량이 좋습니다.", nullable = false)
    val overallComment: String,
    @field:Schema(description = "평가 항목별 점수 목록", nullable = false)
    val items: List<ReadOtherInterviewEvaluationItemResponse>,
) {
    companion object {
        fun from(dto: OtherInterviewEvaluationDto): ReadOtherInterviewEvaluationResponse = ReadOtherInterviewEvaluationResponse(
            evaluatorId = dto.evaluatorId,
            evaluatorName = dto.evaluatorName,
            totalScore = dto.totalScore,
            result = dto.result,
            overallComment = dto.overallComment,
            items = dto.items.map(ReadOtherInterviewEvaluationItemResponse::from),
        )
    }
}
