package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.part.business.PartService
import com.yourssu.scouter.masterdata.support.converter.SemesterConverter
import com.yourssu.scouter.recruiting.deadline.business.PartDocumentDeadlineService
import com.yourssu.scouter.recruiting.rubric.business.InterviewRubricService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/admin/recruiting/deadlines")
class AdminDeadlineController(
    private val partService: PartService,
    private val partDocumentDeadlineService: PartDocumentDeadlineService,
    private val interviewRubricService: InterviewRubricService,
) {

    data class DeadlineDetail(
        val rawInstant: Instant?,
        val formattedInput: String?,
    )

    data class PartDeadlineItem(
        val partId: Long,
        val partName: String,
        val divisionName: String,
        val documentDeadline: DeadlineDetail,
        val interviewDeadline: DeadlineDetail,
        val interviewError: String? = null,
    )

    @GetMapping
    fun deadlinePage(
        @RequestParam(required = false) semester: String?,
        model: Model,
    ): String {
        val resolvedSemester = semester ?: SemesterConverter.convertToIntString(LocalDate.now())
        val parts = partService.readAll().partDtos

        val items = parts.map { part ->
            val docDeadlineInstant = runCatching {
                partDocumentDeadlineService.readByPartId(part.id).deadline
            }.getOrNull()

            var interviewErrorMsg: String? = null
            val intDeadlineInstant = runCatching {
                interviewRubricService.readDeadline(part.id, resolvedSemester)
            }.onFailure {
                interviewErrorMsg = it.message ?: "면접 루브릭 미생성"
            }.getOrNull()

            PartDeadlineItem(
                partId = part.id,
                partName = part.name,
                divisionName = part.division.name,
                documentDeadline = DeadlineDetail(docDeadlineInstant, formatForInput(docDeadlineInstant)),
                interviewDeadline = DeadlineDetail(intDeadlineInstant, formatForInput(intDeadlineInstant)),
                interviewError = interviewErrorMsg,
            )
        }

        model.addAttribute("semester", resolvedSemester)
        model.addAttribute("items", items)

        return "admin/deadlines"
    }

    @PostMapping("/documents")
    @ResponseBody
    fun updateDocumentDeadline(
        @RequestParam partId: Long,
        @RequestParam deadline: String,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching {
            val instant = parseToInstant(deadline)
            partDocumentDeadlineService.upsert(partId, instant)
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "오류가 발생했습니다."))) },
        )
    }

    @PostMapping("/interviews")
    @ResponseBody
    fun updateInterviewDeadline(
        @RequestParam partId: Long,
        @RequestParam semester: String,
        @RequestParam deadline: String,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching {
            val instant = parseToInstant(deadline)
            interviewRubricService.updateDeadline(partId, semester, instant)
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "오류가 발생했습니다."))) },
        )
    }

    private fun formatForInput(instant: Instant?): String? {
        if (instant == null) return null
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            .withZone(SEOUL_ZONE)
            .format(instant)
    }

    private fun parseToInstant(dateTimeStr: String): Instant {
        val trimmed = dateTimeStr.trim()
        return runCatching { Instant.parse(trimmed) }
            .getOrElse {
                val localDateTime = LocalDateTime.parse(trimmed)
                localDateTime.atZone(SEOUL_ZONE).toInstant()
            }
    }

    companion object {
        private val SEOUL_ZONE = ZoneId.of("Asia/Seoul")
    }
}
