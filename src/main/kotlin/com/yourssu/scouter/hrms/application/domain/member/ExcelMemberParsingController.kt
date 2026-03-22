package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.ApplicantPassSheetResult
import com.yourssu.scouter.hrms.business.domain.member.ExcelFileDto
import com.yourssu.scouter.hrms.business.domain.member.ExcelMemberParsingService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDate


@Controller
class ExcelMemberParsingController(
    private val excelMemberParsingService: ExcelMemberParsingService,
) {

    @GetMapping("/members/upload")
    fun showUploadPage(model: Model): String {
        model.addAttribute("allDepartments", excelMemberParsingService.getDepartmentNamesForUpload())
        return "member-upload"
    }

    @PostMapping("/members/include-from-excel")
    fun includeFromExcel(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(value = "uploadType", required = false, defaultValue = "INFO_SHEET") uploadType: String,
        @RequestParam(value = "joinDate", required = false) joinDateParam: String?,
        @RequestParam(value = "departmentMappingRaw", required = false) departmentMappingRaw: List<String>?,
        @RequestParam(value = "departmentMappingValue", required = false) departmentMappingValue: List<String>?,
        @RequestParam(value = "completionSemesterMappingRaw", required = false) completionSemesterMappingRaw: List<String>?,
        @RequestParam(value = "completionSemesterMappingValue", required = false) completionSemesterMappingValue: List<String>?,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        val redirectUri = "redirect:/members/upload"

        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("error", "파일을 선택해주세요.")
            return redirectUri
        }
        if (file.contentType != "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") {
            redirectAttributes.addFlashAttribute("error", "엑셀 파일만 업로드 가능합니다.")
            return redirectUri
        }

        val rawList = departmentMappingRaw ?: emptyList()
        val valueList = departmentMappingValue ?: emptyList()
        val departmentOverrides = rawList.zip(valueList)
            .filter { (_, v) -> v.isNotBlank() }
            .toMap()

        val completionRawList = completionSemesterMappingRaw ?: emptyList()
        val completionValueList = completionSemesterMappingValue ?: emptyList()
        val completionSemesterOverrides = completionRawList.zip(completionValueList)
            .filter { (_, v) -> v.isNotBlank() }
            .toMap()

        if (uploadType == "APPLICANT_PASS_SHEET") {
            val joinDate = when {
                joinDateParam.isNullOrBlank() -> {
                    redirectAttributes.addFlashAttribute("error", "지원자 합격시트 업로드 시 가입일을 선택해주세요.")
                    return redirectUri
                }
                else -> runCatching { LocalDate.parse(joinDateParam) }.getOrElse {
                    redirectAttributes.addFlashAttribute("error", "가입일 형식이 올바르지 않습니다. (yyyy-MM-dd)")
                    return redirectUri
                }
            }
            val result = excelMemberParsingService.processApplicantPassSheet(file, joinDate, departmentOverrides)
            when (result) {
                is ApplicantPassSheetResult.Success -> {
                    redirectAttributes.addFlashAttribute("message", "지원자 합격시트 업로드 성공!")
                    return redirectUri
                }
                is ApplicantPassSheetResult.MappingRequired -> {
                    model.addAttribute("allDepartments", excelMemberParsingService.getDepartmentNamesForUpload())
                    model.addAttribute("unknownBySheet", result.unknownBySheet)
                    model.addAttribute("completionSemesterMappingHints", result.completionSemesterMappingHints)
                    model.addAttribute("joinDate", joinDateParam)
                    model.addAttribute("uploadType", "APPLICANT_PASS_SHEET")
                    model.addAttribute("message", mappingRequiredMessage(result))
                    if (departmentOverrides.isNotEmpty()) {
                        model.addAttribute("departmentOverrideEcho", departmentOverrides)
                    }
                    return "member-upload"
                }
                is ApplicantPassSheetResult.Errors -> {
                    redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류 발생: ${result.messages.joinToString("\n")}")
                    return redirectUri
                }
            }
        }

        val result = excelMemberParsingService.processExcelFile(
            file = file,
            departmentOverrides = departmentOverrides,
            completionSemesterOverrides = completionSemesterOverrides,
        )
        when (result) {
            is ApplicantPassSheetResult.Success -> {
                redirectAttributes.addFlashAttribute("message", "파일 업로드 성공!")
                return redirectUri
            }
            is ApplicantPassSheetResult.MappingRequired -> {
                model.addAttribute("allDepartments", excelMemberParsingService.getDepartmentNamesForUpload())
                model.addAttribute("unknownBySheet", result.unknownBySheet)
                model.addAttribute("completionSemesterMappingHints", result.completionSemesterMappingHints)
                model.addAttribute("uploadType", "INFO_SHEET")
                model.addAttribute("message", mappingRequiredMessage(result))
                if (departmentOverrides.isNotEmpty()) {
                    model.addAttribute("departmentOverrideEcho", departmentOverrides)
                }
                return "member-upload"
            }
            is ApplicantPassSheetResult.Errors -> {
                redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류 발생: ${result.messages.joinToString("\n")}")
                return redirectUri
            }
        }
    }

    private fun mappingRequiredMessage(result: ApplicantPassSheetResult.MappingRequired): String {
        val dept = result.unknownBySheet.isNotEmpty()
        val sem = result.completionSemesterMappingHints.isNotEmpty()
        return when {
            dept && sem ->
                "시트에 등록되지 않은 학과명이 있거나, 수료 시트의 수료 학기(11열)를 DB 학기(yy-s)로 해석할 수 없는 값이 있습니다. 아래에서 매핑 후 같은 파일을 다시 업로드해주세요."
            dept ->
                "시트에 등록되지 않은 학과명이 있습니다. 아래에서 매핑 후 같은 파일을 다시 업로드해주세요."
            sem ->
                "수료 시트 11열에 학기로 해석되지 않는 값이 있습니다. 아래에 yy-s(예: 25-1)로 매핑한 뒤 같은 파일을 다시 업로드해주세요."
            else ->
                "매핑이 필요합니다. 같은 파일을 다시 업로드해주세요."
        }
    }

    @GetMapping("/members/download-to-excel")
    fun downloadExcel(response: HttpServletResponse) {
        val excelFileDto: ExcelFileDto = excelMemberParsingService.createMemberExcelFile()

        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.setHeader("Content-Disposition", "attachment; filename=${excelFileDto.fileName}")

        excelFileDto.workbook.use {
            it.write(response.outputStream)
        }
    }
}
