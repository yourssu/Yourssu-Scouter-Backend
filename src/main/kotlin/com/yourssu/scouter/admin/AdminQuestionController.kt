package com.yourssu.scouter.admin

import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
import com.yourssu.scouter.masterdata.support.converter.SemesterConverter
import com.yourssu.scouter.recruiting.interview.business.InterviewRequirementService
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.CreateQuestionsRequest
import com.yourssu.scouter.recruiting.interviewQuestion.business.QuestionService
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementReader
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.time.LocalDate

@Controller
@RequestMapping("/admin/recruiting/questions/global")
class AdminQuestionController(
    private val questionService: QuestionService,
    private val questionReader: QuestionReader,
    private val semesterReader: SemesterReader,
    private val interviewRequirementService: InterviewRequirementService,
) {

    @GetMapping
    fun globalQuestions(
        @RequestParam(required = false) semester: String?,
        model: Model,
    ): String {
        val resolvedSemester = semester ?: SemesterConverter.convertToIntString(LocalDate.now())
        model.addAttribute("semester", resolvedSemester)

        // 1. Fetch culture requirements for the semester
        val requirementDto = runCatching {
            interviewRequirementService.readGlobalBySemester(Semester.of(resolvedSemester))
        }.getOrElse {
            model.addAttribute("error", it.message)
            null
        }
        val cultureRequirements = requirementDto?.culture ?: emptyList()
        model.addAttribute("cultureRequirements", cultureRequirements)

        // 2. Fetch global questions
        // INTRO / OUTRO: 학기에 무관
        val allQuestions = questionReader.readAll().filter { it.partId == null }
        val introQuestions = allQuestions.filter { it.category == QuestionCategory.INTRO }
        val outroQuestions = allQuestions.filter { it.category == QuestionCategory.OUTRO }

        // CULTURE: 해당 학기에 귀속된 질문만 조회
        val resolvedSemesterObj = runCatching { semesterReader.readByString(resolvedSemester) }.getOrNull()
        val cultureQuestions = if (resolvedSemesterObj?.id != null) {
            questionReader.readAllBySemesterId(resolvedSemesterObj.id!!)
                .filter { it.partId == null && it.category == QuestionCategory.CULTURE }
        } else {
            allQuestions.filter { it.category == QuestionCategory.CULTURE }
        }

        model.addAttribute("introQuestions", introQuestions)
        model.addAttribute("outroQuestions", outroQuestions)
        model.addAttribute("cultureQuestions", cultureQuestions)

        return "admin/requirements/global-questions"
    }

    @PostMapping
    @ResponseBody
    fun saveGlobalQuestions(
        @RequestParam semester: String,
        @RequestBody request: CreateQuestionsRequest,
    ): ResponseEntity<Map<String, Any?>> {
        return runCatching {
            questionService.upsert(semester, request)
        }.fold(
            onSuccess = { ResponseEntity.ok(mapOf("success" to true)) },
            onFailure = { ResponseEntity.badRequest().body(mapOf("error" to it.message)) }
        )
    }
}

