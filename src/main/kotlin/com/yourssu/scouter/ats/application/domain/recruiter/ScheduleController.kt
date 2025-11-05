package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.CreateScheduleCommand
import com.yourssu.scouter.ats.business.domain.recruiter.ScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@Tag(name = "면접 일정")
@RestController
@RequestMapping("/recruiter")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @Operation(summary = "면접 스케줄 추가 API", description = "면접 스케줄을 추가하는 API 입니다.")
    @ApiResponse(description = "CREATED", responseCode = "201")
    @PostMapping("/schedule")
    fun createSchedules(@RequestBody schedules: List<CreateScheduleRequest>): ResponseEntity<Unit> {
        val commands = schedules.map(CreateScheduleRequest::toCommand)
        scheduleService.createSchedules(commands)

        return ResponseEntity.created(URI.create("/recruiter/schedule")).build()
    }

    @Operation(
        summary = "면접 스케줄 조회 API",
        description = "면접 스케줄 조회 API 입니다. 추후 partID를 입력하지 않으면 전체 조회가 되도록 변경 예정"
    )
    @GetMapping("/schedule")
    fun getSchedules(@RequestParam partId: Long) = ResponseEntity.ok(
        scheduleService.readSchedulesByPartId(partId).map(ReadScheduleResponse::from)
    )

    @Operation(
        summary = "면접 스케줄 자동 생성 API",
        description = """
            백트래킹 알고리즘을 사용하여 면접 스케줄을 자동 생성합니다.
            - 같은 파트의 같은 시간에는 중복 배정하지 않습니다.
            - 모든 지원자를 배정할 수 없는 경우 400 에러를 반환합니다.
            - 지원자가 많을 경우 응답 시간이 길어질 수 있습니다.
            - **주의**: 저장은 되지 않으며, 미리보기 용도입니다.
        """)
    @GetMapping("/schedule/auto/{partId}")
    fun getAutoSchedules(@PathVariable partId: Long, @RequestParam strategy: String) = ResponseEntity.ok(
        scheduleService.autoGenerateSchedules(partId, strategy).map {
            it.map(AutoScheduleResponse::from)
        }
    )

    @Operation(
        summary = "파트별 스케줄 삭제 API",
        description = "특정 파트의 모든 면접 스케줄을 삭제합니다."
    )
    @ApiResponse(description = "OK", responseCode = "200")
    @ApiResponse(description = "파트를 찾을 수 없음", responseCode = "404")
    @DeleteMapping("/schedule/part/{partId}")
    fun deleteByPart(@PathVariable partId: Long): ResponseEntity<DeleteByPartResponse> {
        val response = DeleteByPartResponse(scheduleService.deleteByPart(partId))
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "파트 스케줄 수정 API",
        description = "특정 파트의 스케줄을 전체 수정합니다."
    )
    @ApiResponse(description = "OK", responseCode = "200")
    @ApiResponse(description = "파트/지원자를 찾을 수 없음", responseCode = "404")
    @PutMapping("/schedule/part/{partId}")
    fun updateByPart(
        @PathVariable partId: Long,
        @RequestBody schedules: List<CreateScheduleCommand>
    ): ResponseEntity<Unit> {
        scheduleService.updateByPart(partId, schedules)
        return ResponseEntity.ok().build()
    }
}