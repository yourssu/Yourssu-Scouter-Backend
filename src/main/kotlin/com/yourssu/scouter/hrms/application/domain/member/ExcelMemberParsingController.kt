package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.ErrorMessagesDto
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


@Controller
class ExcelMemberParsingController(
    private val excelMemberParsingService: ExcelMemberParsingService,
) {

    @GetMapping("/members/upload")
    fun showUploadPage(model: Model): String = "member-upload.html"

    @PostMapping("/members/include-from-excel")
    fun includeFromExcel(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
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

        val errors: ErrorMessagesDto = excelMemberParsingService.processExcelFile(file)
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류 발생: ${errors.combine("\n")}")
            return redirectUri
        }
        redirectAttributes.addFlashAttribute("message", "파일 업로드 성공!")
        return redirectUri
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
