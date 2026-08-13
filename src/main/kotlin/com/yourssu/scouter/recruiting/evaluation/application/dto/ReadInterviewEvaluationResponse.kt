package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.InterviewEvaluationDto
import com.yourssu.scouter.recruiting.evaluation.business.dto.InterviewEvaluationItemDto
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "면접 평가 루브릭 그룹별 점수 정보")
data class ReadInterviewEvaluationGroupResponse(
    @field:Schema(description = "평가 그룹", example = "CULTURE_FIT", allowableValues = ["CULTURE_FIT", "TEAM_FIT", "JOB_FIT"], nullable = false)
    val group: String,
    @field:Schema(description = "그룹에 속한 평가 항목과 점수 목록", nullable = false)
    val items: List<ReadInterviewEvaluationItemResponse>
)

@Schema(description = "면접 평가 항목별 조회 정보")
data class ReadInterviewEvaluationItemResponse(
    @field:Schema(description = "면접 루브릭 평가 항목 ID", example = "101", nullable = false)
    val itemId: Long,
    @field:Schema(description = "평가 항목명", example = "주도적인 실행력", nullable = false)
    val itemTitle: String,
    @field:Schema(description = "평가 항목의 최대 배점", example = "10", nullable = false)
    val maxScore: Int,
    @field:Schema(description = "평가자가 부여한 점수", example = "8", nullable = false)
    val score: Int,
)

@Schema(description = "로그인한 면접관 본인의 면접 평가 조회 응답")
data class ReadInterviewEvaluationResponse(
    @field:Schema(description = "모든 평가 항목 점수의 합계", example = "85", nullable = false)
    val totalScore: Int,
    @field:Schema(description = "루브릭 그룹별 평가 항목과 점수", nullable = false)
    val groups: List<ReadInterviewEvaluationGroupResponse>,
    @field:Schema(description = "면접 전반에 대한 종합 의견", example = "질문에 대한 답변이 구조적이고 문화 적합성이 높습니다.", nullable = false)
    val overallComment: String,
    @field:Schema(description = "면접 평가 결과", example = "FINAL_PASS", allowableValues = ["PENDING", "FINAL_PASS", "INTERVIEW_FAIL"], nullable = false)
    val result: InterviewResult,
    @field:Schema(description = "최종 제출 시각. 임시저장 상태이면 null입니다.", example = "2026-08-01T09:30:00", type = "string", format = "date-time", nullable = true)
    val submittedAt: LocalDateTime?,
) {
    companion object {
        fun from(dto: InterviewEvaluationDto): ReadInterviewEvaluationResponse {
            val itemsByGroup = dto.items.groupBy { it.rubricType }
            val groups = listOf(RubricGroupType.CULTURE, RubricGroupType.TEAM, RubricGroupType.JOB).map { groupType ->
                val items = itemsByGroup[groupType] ?: emptyList()
                ReadInterviewEvaluationGroupResponse(
                    group = when (groupType) {
                        RubricGroupType.CULTURE -> "CULTURE_FIT"
                        RubricGroupType.TEAM -> "TEAM_FIT"
                        RubricGroupType.JOB -> "JOB_FIT"
                        RubricGroupType.OTHER -> "OTHER"
                    },
                    items = items.map {
                        ReadInterviewEvaluationItemResponse(
                            itemId = it.evaluationItemId,
                            itemTitle = it.keyword,
                            maxScore = it.maxScore,
                            score = it.score
                        )
                    }
                )
            }

            return ReadInterviewEvaluationResponse(
                totalScore = dto.totalScore,
                groups = groups,
                overallComment = dto.overallComment,
                result = dto.result,
                submittedAt = dto.submittedAt
            )
        }
    }
}

