package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.application.support.exception.LoginRequiredException
import com.yourssu.scouter.common.business.domain.authentication.AuthenticationService
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.hrms.business.domain.member.ApplicantPassSheetResult
import com.yourssu.scouter.hrms.business.domain.member.ExcelFileDto
import com.yourssu.scouter.hrms.business.domain.member.ExcelMemberParsingService
import com.yourssu.scouter.hrms.business.domain.member.JoinDateOverrideFormEcho
import com.yourssu.scouter.hrms.business.domain.member.MemberExcelImportOverrides
import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import com.yourssu.scouter.hrms.business.support.exception.MemberAccessDeniedException
import com.yourssu.scouter.hrms.implement.support.MemberExcelToolProperties
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDate


@Controller
class ExcelMemberParsingController(
    private val excelMemberParsingService: ExcelMemberParsingService,
    private val memberPrivacyService: MemberPrivacyService,
    private val memberExcelToolProperties: MemberExcelToolProperties,
    private val authenticationService: AuthenticationService,
) {
    companion object {
        private const val MEMBER_UPLOAD_AUTHORIZED = "MEMBER_UPLOAD_AUTHORIZED"
    }

    @GetMapping("/members/upload")
    fun showUploadPage(model: Model, session: HttpSession): String {
        if (!isUploadAuthorized(session)) {
            if (resolveUploadPassword().isEmpty()) {
                model.addAttribute(
                    "error",
                    "업로드 비밀번호가 설정되지 않았습니다. MEMBER_EXCEL_UPLOAD_PASSWORD 또는 MEMBER_EXCEL_DOWNLOAD_PASSWORD를 설정해주세요.",
                )
            }
            return "member-upload-password"
        }
        model.addAttribute("allDepartments", excelMemberParsingService.getDepartmentNamesForUpload())
        return "member-upload"
    }

    @PostMapping("/members/upload/auth")
    fun authorizeUploadPage(
        @RequestParam("toolPassword") toolPassword: String?,
        redirectAttributes: RedirectAttributes,
        session: HttpSession,
    ): String {
        val configuredSecret: String = resolveUploadPassword()
        val provided: String = toolPassword?.trim().orEmpty()
        if (configuredSecret.isNotEmpty() && secureStringEquals(provided, configuredSecret)) {
            session.setAttribute(MEMBER_UPLOAD_AUTHORIZED, true)
            return "redirect:/members/upload"
        }
        if (configuredSecret.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                "error",
                "업로드 비밀번호가 설정되지 않았습니다. MEMBER_EXCEL_UPLOAD_PASSWORD 또는 MEMBER_EXCEL_DOWNLOAD_PASSWORD를 설정해주세요.",
            )
        } else {
            redirectAttributes.addFlashAttribute("error", "업로드 페이지 비밀번호가 올바르지 않습니다.")
        }
        return "redirect:/members/upload"
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
        @RequestParam(value = "joinDateMappingSheet", required = false) joinDateMappingSheet: List<String>?,
        @RequestParam(value = "joinDateMappingRaw", required = false) joinDateMappingRaw: List<String>?,
        @RequestParam(value = "joinDateMappingValue", required = false) joinDateMappingValue: List<String>?,
        @RequestParam(value = "expectedReturnMappingRaw", required = false) expectedReturnMappingRaw: List<String>?,
        @RequestParam(value = "expectedReturnMappingValue", required = false) expectedReturnMappingValue: List<String>?,
        @RequestParam(value = "inactiveActivityActiveCountMappingRaw", required = false)
        inactiveActivityActiveCountMappingRaw: List<String>?,
        @RequestParam(value = "inactiveActivityActiveCountMappingValue", required = false)
        inactiveActivityActiveCountMappingValue: List<String>?,
        @RequestParam(value = "inactiveActivityInactiveCountMappingRaw", required = false)
        inactiveActivityInactiveCountMappingRaw: List<String>?,
        @RequestParam(value = "inactiveActivityInactiveCountMappingValue", required = false)
        inactiveActivityInactiveCountMappingValue: List<String>?,
        model: Model,
        redirectAttributes: RedirectAttributes,
        session: HttpSession,
    ): String {
        val redirectUri = "redirect:/members/upload"
        if (!isUploadAuthorized(session)) {
            redirectAttributes.addFlashAttribute("error", "업로드 페이지 인증 후 다시 시도해주세요.")
            return redirectUri
        }

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

        val joinSheetList = joinDateMappingSheet ?: emptyList()
        val joinRawList = joinDateMappingRaw ?: emptyList()
        val joinValList = joinDateMappingValue ?: emptyList()
        val joinDateOverrides: Map<String, String> = joinSheetList.zip(joinRawList).zip(joinValList)
            .map { (sr, v) -> Triple(sr.first, sr.second, v) }
            .filter { (_, _, v) -> v.isNotBlank() }
            .associate { (sheet, raw, v) -> "$sheet|||$raw" to v.trim() }

        val expectedRawList = expectedReturnMappingRaw ?: emptyList()
        val expectedValList = expectedReturnMappingValue ?: emptyList()
        val expectedReturnOverrides = expectedRawList.zip(expectedValList).toMap()

        val inactiveActivityActiveRawList = inactiveActivityActiveCountMappingRaw ?: emptyList()
        val inactiveActivityActiveCountList = inactiveActivityActiveCountMappingValue ?: emptyList()
        val inactiveActivityInactiveRawList = inactiveActivityInactiveCountMappingRaw ?: emptyList()
        val inactiveActivityInactiveCountList = inactiveActivityInactiveCountMappingValue ?: emptyList()
        val inactiveActivityTotalSemestersOverrides = inactiveActivityActiveRawList.zip(inactiveActivityActiveCountList)
            .associate { (raw, value) -> raw to value.trim().toIntOrNull()?.takeIf { it > 0 } }
        val inactiveInactiveTotalSemestersOverrides = inactiveActivityInactiveRawList.zip(inactiveActivityInactiveCountList)
            .associate { (raw, value) -> raw to value.trim().toIntOrNull()?.takeIf { it > 0 } }

        val importOverrides = MemberExcelImportOverrides(
            departmentOverrides = departmentOverrides,
            completionSemesterOverrides = completionSemesterOverrides,
            joinDateOverrides = joinDateOverrides,
            expectedReturnOverrides = expectedReturnOverrides,
            inactiveActivityTotalSemestersOverrides = inactiveActivityTotalSemestersOverrides,
            inactiveInactiveTotalSemestersOverrides = inactiveInactiveTotalSemestersOverrides,
        )

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
                    model.addAttribute("joinDateMappingHints", result.joinDateMappingHints)
                    model.addAttribute("expectedReturnMappingHints", result.expectedReturnMappingHints)
                    model.addAttribute("inactiveActivitySemesterMappingHints", result.inactiveActivitySemesterMappingHints)
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

        val result = excelMemberParsingService.processExcelFile(file = file, overrides = importOverrides)
        when (result) {
            is ApplicantPassSheetResult.Success -> {
                redirectAttributes.addFlashAttribute("message", "파일 업로드 성공!")
                return redirectUri
            }
            is ApplicantPassSheetResult.MappingRequired -> {
                model.addAttribute("allDepartments", excelMemberParsingService.getDepartmentNamesForUpload())
                model.addAttribute("unknownBySheet", result.unknownBySheet)
                model.addAttribute("completionSemesterMappingHints", result.completionSemesterMappingHints)
                model.addAttribute("joinDateMappingHints", result.joinDateMappingHints)
                model.addAttribute("expectedReturnMappingHints", result.expectedReturnMappingHints)
                model.addAttribute("inactiveActivitySemesterMappingHints", result.inactiveActivitySemesterMappingHints)
                model.addAttribute("uploadType", "INFO_SHEET")
                model.addAttribute("message", mappingRequiredMessage(result))
                if (departmentOverrides.isNotEmpty()) {
                    model.addAttribute("departmentOverrideEcho", departmentOverrides)
                }
                if (completionSemesterOverrides.isNotEmpty()) {
                    model.addAttribute("completionSemesterOverrideEcho", completionSemesterOverrides)
                }
                if (joinDateOverrides.isNotEmpty()) {
                    model.addAttribute("joinDateOverrideEcho", JoinDateOverrideFormEcho.fromOverrides(joinDateOverrides))
                }
                if (expectedReturnOverrides.isNotEmpty()) {
                    model.addAttribute("expectedReturnOverrideEcho", expectedReturnOverrides)
                }
                if (inactiveActivityTotalSemestersOverrides.isNotEmpty() || inactiveInactiveTotalSemestersOverrides.isNotEmpty()) {
                    model.addAttribute("inactiveActivityActiveCountOverrideEcho", inactiveActivityTotalSemestersOverrides)
                    model.addAttribute("inactiveActivityInactiveCountOverrideEcho", inactiveInactiveTotalSemestersOverrides)
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
        val join = result.joinDateMappingHints.isNotEmpty()
        val exp = result.expectedReturnMappingHints.isNotEmpty()
        val parts = buildList {
            if (dept) add("미등록 학과명")
            if (sem) add("수료 학기(11열)")
            if (join) add("가입일/탈퇴일")
            if (exp) add("비액티브 예정복귀")
        }
        return if (parts.isEmpty()) {
            "매핑이 필요합니다. 같은 파일을 다시 업로드해주세요."
        } else {
            "다음 항목을 아래에서 보정한 뒤 같은 파일을 다시 업로드해주세요: ${parts.joinToString(", ")}."
        }
    }

    /**
     * HTML: 공유 비밀번호([MemberExcelToolProperties.downloadPassword]) 또는 Bearer + HR/스카우터 특권.
     * GET은 사용하지 않음(비밀번호를 쿼리에 넣지 않기 위함).
     */
    @PostMapping("/members/download-to-excel")
    fun downloadExcel(
        @RequestParam(value = "toolPassword", required = false) toolPassword: String?,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
        response: HttpServletResponse,
    ) {
        assertMemberExcelDownloadAllowed(toolPassword, authorization)

        val excelFileDto: ExcelFileDto = excelMemberParsingService.createMemberExcelFile()

        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.setHeader("Content-Disposition", "attachment; filename=${excelFileDto.fileName}")

        excelFileDto.workbook.use {
            it.write(response.outputStream)
        }
    }

    private fun assertMemberExcelDownloadAllowed(toolPassword: String?, authorization: String?) {
        val configuredSecret: String = memberExcelToolProperties.downloadPassword.trim()
        val provided: String = toolPassword?.trim().orEmpty()

        if (configuredSecret.isNotEmpty()) {
            if (secureStringEquals(provided, configuredSecret)) {
                return
            }
            if (authorization.isNullOrBlank()) {
                if (provided.isEmpty()) {
                    throw LoginRequiredException(
                        "다운로드 비밀번호를 입력해 주세요. 또는 앱 로그인 후 Bearer 액세스 토큰으로 요청해 주세요.",
                    )
                }
                throw LoginRequiredException(
                    "다운로드 비밀번호가 올바르지 않습니다. 관리자에게 비밀번호를 확인하거나, 앱 로그인 후 Bearer 토큰으로 요청해 주세요.",
                )
            }
        }

        if (authorization.isNullOrBlank()) {
            throw LoginRequiredException(
                "웹에서 다운로드하려면 서버에 다운로드 비밀번호를 설정하세요(MEMBER_EXCEL_DOWNLOAD_PASSWORD 또는 scouter.member-excel-tool.download-password). " +
                    "또는 API 클라이언트에서 Bearer 액세스 토큰으로 POST /members/download-to-excel 을 호출하세요.",
            )
        }

        val privateClaims = authenticationService.getValidPrivateClaims(TokenType.ACCESS, authorization)
        if (!memberPrivacyService.isPrivilegedUser(privateClaims.userId)) {
            throw MemberAccessDeniedException("멤버 전체 엑셀을 다운로드할 권한이 없습니다.")
        }
    }

    private fun secureStringEquals(a: String, b: String): Boolean {
        val ba = a.toByteArray(StandardCharsets.UTF_8)
        val bb = b.toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.isEqual(ba, bb)
    }

    private fun isUploadAuthorized(session: HttpSession): Boolean {
        val configuredSecret: String = resolveUploadPassword()
        if (configuredSecret.isEmpty()) return false
        return session.getAttribute(MEMBER_UPLOAD_AUTHORIZED) == true
    }

    private fun resolveUploadPassword(): String =
        memberExcelToolProperties.uploadPassword.trim().ifBlank { memberExcelToolProperties.downloadPassword.trim() }
}
