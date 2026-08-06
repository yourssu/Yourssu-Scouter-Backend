package com.yourssu.scouter.recruiting.rubric.application

import com.yourssu.scouter.recruiting.rubric.application.dto.InterviewRubricResponse
import com.yourssu.scouter.recruiting.rubric.application.dto.InterviewRubricUpdateRequest
import com.yourssu.scouter.recruiting.rubric.application.dto.InterviewRubricDeadlineUpdateRequest
import com.yourssu.scouter.recruiting.rubric.application.dto.InterviewRubricDeadlineResponse
import com.yourssu.scouter.recruiting.rubric.business.InterviewRubricService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "면접 평가 루브릭", description = "파트와 학기별 면접 평가 루브릭 관리 API")
@RestController
@Validated
class InterviewRubricController(
    private val interviewRubricService: InterviewRubricService,
) {

    @Operation(summary = "면접 평가 마감일 조회")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "마감일 조회 성공", content = [Content(schema = Schema(implementation = InterviewRubricDeadlineResponse::class))]),
        ApiResponse(responseCode = "404", description = "파트 또는 루브릭을 찾을 수 없음"),
    ])
    @GetMapping("/parts/{partId}/interviews/{semester}/deadline")
    fun readDeadline(
        @Parameter(description = "파트 ID", example = "3") @PathVariable partId: Long,
        @Parameter(description = "학기 식별자 YYYY-1 또는 YYYY-2", example = "2026-2") @PathVariable @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
    ): ResponseEntity<InterviewRubricDeadlineResponse> = ResponseEntity.ok(
        InterviewRubricDeadlineResponse(interviewRubricService.readDeadline(partId, semester)),
    )

    @Operation(
        summary = "학기별 면접 평가 루브릭 조회",
        description = "파트와 학기에 해당하는 면접 평가 루브릭을 조회합니다. " +
            "아직 저장된 루브릭이 없으면 해당 파트·학기의 면접 요구조건을 바탕으로 배점 미설정(maxScore=0) 상태의 미저장 템플릿을 반환합니다.",
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공(저장된 루브릭 또는 미저장 템플릿)", content = [Content(schema = Schema(implementation = InterviewRubricResponse::class))]),
        ApiResponse(responseCode = "400", description = "요청 값이 올바르지 않음"),
        ApiResponse(responseCode = "404", description = "파트를 찾을 수 없음"),
    ])
    @GetMapping("/parts/{partId}/interviews/rubrics/{semester}")
    fun readByPartIdAndSemester(
        @Parameter(description = "파트 ID", example = "3") @PathVariable partId: Long,
        @Parameter(description = "학기 식별자. YYYY-1 또는 YYYY-2 형식", example = "2026-1") @PathVariable @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
    ): ResponseEntity<InterviewRubricResponse> = ResponseEntity.ok(
        InterviewRubricResponse.from(interviewRubricService.readByPartIdAndSemester(partId, semester)),
    )

    @Operation(
        summary = "학기별 면접 평가 루브릭 등록 또는 수정/ 배점설정(어드민)",
        description = "해당 파트·학기에 루브릭이 없으면 생성하고, 있으면 항목 전체를 교체합니다. 잠긴 루브릭은 수정할 수 없으며 모든 항목의 maxScore 합계는 100이어야 합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "학기는 URL 경로에서 지정합니다.",
            content = [Content(
                schema = Schema(implementation = InterviewRubricUpdateRequest::class),
                examples = [ExampleObject(value = """{
  "deadline": "2026-08-01T09:00:00Z",
  "groups": [
    {
      "group": "CULTURE_FIT",
      "items": [
        { "title": "주도성, 실행력", "maxScore": 10 }
      ]
    },
    {
      "group": "TEAM_FIT",
      "items": [
        { "title": "팀 목표 이해", "maxScore": 10 }
      ]
    },
    {
      "group": "JOB_FIT",
      "items": [
        { "title": "파트 핵심 역량", "maxScore": 80 }
      ]
    }
  ]
}""")],
            )],
        ),
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "생성 또는 수정 성공", content = [Content(schema = Schema(implementation = InterviewRubricResponse::class))]),
        ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않거나 배점 합계가 100이 아님"),
        ApiResponse(responseCode = "404", description = "파트를 찾을 수 없음"),
    ])
    @PutMapping("/parts/{partId}/interviews/rubrics/{semester}")
    fun upsert(
        @Parameter(description = "파트 ID", example = "3") @PathVariable partId: Long,
        @Parameter(description = "학기 식별자. YYYY-1 또는 YYYY-2 형식", example = "2026-2") @PathVariable @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
        @RequestBody @Valid request: InterviewRubricUpdateRequest,
    ): ResponseEntity<InterviewRubricResponse> = ResponseEntity.ok(
        InterviewRubricResponse.from(interviewRubricService.upsert(request.toCommand(partId, semester))),
    )

    @Operation(summary = "면접 평가 루브릭 마감일 수정")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "마감일 수정 성공", content = [Content(schema = Schema(implementation = InterviewRubricResponse::class))]),
        ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않거나 잠긴 루브릭"),
        ApiResponse(responseCode = "404", description = "파트 또는 루브릭을 찾을 수 없음"),
    ])
    @PatchMapping("/parts/{partId}/interviews/{semester}/deadline")
    fun updateDeadline(
        @Parameter(description = "파트 ID", example = "3") @PathVariable partId: Long,
        @Parameter(description = "학기 식별자 YYYY-1 또는 YYYY-2", example = "2026-2") @PathVariable @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
        @RequestBody @Valid request: InterviewRubricDeadlineUpdateRequest,
    ): ResponseEntity<InterviewRubricResponse> = ResponseEntity.ok(
        InterviewRubricResponse.from(interviewRubricService.updateDeadline(partId, semester, request.deadline!!)),
    )
}
