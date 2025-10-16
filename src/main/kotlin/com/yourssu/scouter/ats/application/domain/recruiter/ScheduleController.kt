package com.yourssu.scouter.ats.application.domain.recruiter

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
}