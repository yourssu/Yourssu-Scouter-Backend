package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.parser.ApplicantPassSheetProcessor
import com.yourssu.scouter.hrms.implement.domain.member.parser.ErrorMessages
import com.yourssu.scouter.hrms.implement.domain.member.export.MemberInfoExcelWorkbookExporter
import com.yourssu.scouter.hrms.implement.domain.member.parser.MemberExcelProcessor
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class ExcelMemberParsingService(
    private val departmentReader: DepartmentReader,
    private val partReader: PartReader,
    private val processors: List<MemberExcelProcessor>,
    private val applicantPassSheetProcessor: ApplicantPassSheetProcessor,
    private val memberInfoExcelWorkbookExporter: MemberInfoExcelWorkbookExporter,
    private val infoSheetImportPreflightOrchestrator: InfoSheetImportPreflightOrchestrator,
) {

    fun processExcelFile(
        file: MultipartFile,
        overrides: MemberExcelImportOverrides = MemberExcelImportOverrides.EMPTY,
    ): ApplicantPassSheetResult {
        val workbook = XSSFWorkbook(file.inputStream)
        val departments: Map<String, Department> = departmentReader.readAll().associateBy { it.name }
        val parts: Map<String, Part> = partReader.readAll().associateBy { it.name }
        workbook.use {
            val preflight = infoSheetImportPreflightOrchestrator.run(it, departments, overrides)
            if (preflight.needsMapping()) {
                return ApplicantPassSheetResult.MappingRequired(
                    unknownBySheet = preflight.unknownBySheet,
                    completionSemesterMappingHints = preflight.completionSemesterMappingHints,
                    joinDateMappingHints = preflight.joinDateMappingHints,
                    expectedReturnMappingHints = preflight.expectedReturnMappingHints,
                )
            }

            val errors: MutableList<String> = mutableListOf()
            for (state: MemberState in MemberState.entries) {
                val processor: MemberExcelProcessor = findProcessor(state)
                val sheet: XSSFSheet? = workbook.getSheet(MemberStateConverter.convertToString(state))
                if (sheet == null) {
                    errors.add("엑셀 파일에 '${state.name}' 시트가 없습니다.")
                    continue
                }
                val errorMessages: ErrorMessages = processor.parse(sheet, departments, parts, overrides)
                if (errorMessages.hasErrors()) {
                    errors.addAll(errorMessages.errorMessages.map { "${state.name} 시트 오류: $it" })
                }
            }
            return when {
                errors.isNotEmpty() -> ApplicantPassSheetResult.Errors(errors)
                else -> ApplicantPassSheetResult.Success
            }
        }
    }

    private fun findProcessor(state: MemberState): MemberExcelProcessor =
        processors.first { it.supportingState() == state }

    /** 업로드 페이지 학과 드롭다운용: DB에 등록된 학과 이름 목록 (이름 순). */
    fun getDepartmentNamesForUpload(): List<String> =
        departmentReader.readAll().map { it.name }

    fun processApplicantPassSheet(
        file: MultipartFile,
        joinDate: LocalDate,
        departmentOverrides: Map<String, String> = emptyMap(),
    ): ApplicantPassSheetResult {
        val workbook = XSSFWorkbook(file.inputStream)
        val departments: Map<String, Department> = departmentReader.readAll().associateBy { it.name }
        val parts: Map<String, Part> = partReader.readAll().associateBy { it.name }
        val firstSheet: XSSFSheet? = workbook.getSheetAt(0)
        workbook.use {
            if (firstSheet == null) {
                return ApplicantPassSheetResult.Errors(listOf("엑셀 파일에 시트가 없습니다."))
            }
            if (departmentOverrides.isEmpty()) {
                val unknown = applicantPassSheetProcessor.collectUnknownDepartments(firstSheet, departments)
                if (unknown.isNotEmpty()) {
                    return ApplicantPassSheetResult.MappingRequired(
                        unknownBySheet = mapOf("지원자 합격시트" to unknown),
                    )
                }
            }
            val errorMessages: ErrorMessages = applicantPassSheetProcessor.parse(
                sheet = firstSheet,
                departments = departments,
                parts = parts,
                joinDate = joinDate,
                departmentOverrides = departmentOverrides,
            )
            return if (errorMessages.hasErrors()) {
                ApplicantPassSheetResult.Errors(errorMessages.errorMessages)
            } else {
                ApplicantPassSheetResult.Success
            }
        }
    }

    fun createMemberExcelFile(): ExcelFileDto {
        val workbook: XSSFWorkbook = memberInfoExcelWorkbookExporter.buildWorkbook()
        val stamp =
            ZonedDateTime.now(SEOUL)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName: String = "members_$stamp.xlsx"
        return ExcelFileDto(workbook = workbook, fileName = fileName)
    }

    companion object {
        private val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
