package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.part.business.PartService
import com.yourssu.scouter.masterdata.semester.business.SemesterService
import com.yourssu.scouter.recruiting.applicant.business.ApplicantService
import com.yourssu.scouter.recruiting.evaluation.business.DocumentEvaluationService
import com.yourssu.scouter.recruiting.evaluation.business.InterviewEvaluationService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate

@Controller
@RequestMapping("/admin/recruiting/evaluations")
class AdminEvaluationController(
    private val partService: PartService,
    private val semesterService: SemesterService,
    private val applicantService: ApplicantService,
    private val documentEvaluationService: DocumentEvaluationService,
    private val interviewEvaluationService: InterviewEvaluationService,
) {

    @GetMapping
    fun evaluationPage(
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false) semesterId: Long?,
        @RequestParam(required = false) applicantId: Long?,
        model: Model,
    ): String {
        val semesters = semesterService.readAllByReverseOrder()
        // 목록 최상단은 미래 학기라 기본값으로 쓰면 빈 지원자 목록이 나온다. 다른 어드민 화면과 같이 현재 학기를 기본으로 둔다.
        val resolvedSemesterId = semesterId
            ?: runCatching { semesterService.readByDate(LocalDate.now()).id }.getOrNull()
            ?: semesters.firstOrNull()?.id

        model.addAttribute("parts", partService.readAll().partDtos)
        model.addAttribute("semesters", semesters)
        model.addAttribute("partId", partId)
        model.addAttribute("semesterId", resolvedSemesterId)
        model.addAttribute("applicantId", applicantId)

        if (partId != null) {
            model.addAttribute(
                "applicants",
                applicantService.readAllByFilters(name = null, states = null, semesterId = resolvedSemesterId, partId = partId),
            )
        }

        if (applicantId != null) {
            runCatching { applicantService.readById(applicantId) }
                .onSuccess { applicant ->
                    model.addAttribute("applicant", applicant)
                    model.addAttribute("documentEvaluations", documentEvaluationService.readAllForAdmin(applicantId))
                    model.addAttribute("interviewEvaluations", interviewEvaluationService.readAllForAdmin(applicantId))
                }
                .onFailure { model.addAttribute("error", it.message ?: "지원자를 불러오지 못했습니다.") }
        }

        return "admin/evaluations"
    }

    @PostMapping("/documents/delete")
    @ResponseBody
    fun deleteDocumentEvaluation(
        @RequestParam applicantId: Long,
        @RequestParam evaluatorUserId: Long,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { documentEvaluationService.deleteByEvaluator(applicantId, evaluatorUserId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "삭제에 실패했습니다."))) },
        )
    }

    @PostMapping("/interviews/delete")
    @ResponseBody
    fun deleteInterviewEvaluation(
        @RequestParam applicantId: Long,
        @RequestParam evaluatorUserId: Long,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching { interviewEvaluationService.deleteByEvaluator(applicantId, evaluatorUserId) }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to (it.message ?: "삭제에 실패했습니다."))) },
        )
    }
}
