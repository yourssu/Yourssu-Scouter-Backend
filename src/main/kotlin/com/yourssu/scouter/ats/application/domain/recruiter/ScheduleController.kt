package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.ScheduleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/recruiter")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @PostMapping("/schedule")
    fun createSchedules(@RequestBody schedules: List<CreateScheduleRequest>): ResponseEntity<Unit> {
        val commands = schedules.map(CreateScheduleRequest::toCommand)
        scheduleService.createSchedules(commands)

        return ResponseEntity.created(URI.create("/schedule")).build()
    }

    @GetMapping("/schedule")
    fun getSchedules(@RequestParam partId: Long) = ResponseEntity.ok(
        scheduleService.readSchedulesByPartId(partId).map(ReadScheduleResponse::from)
    )
}