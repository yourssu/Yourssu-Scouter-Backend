package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.parser.ErrorMessages
import com.yourssu.scouter.hrms.implement.domain.member.parser.MemberExcelProcessor
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ExcelMemberParsingService(
    private val departmentReader: DepartmentReader,
    private val partReader: PartReader,
    private val processors: List<MemberExcelProcessor>,
) {

    fun processExcelFile(file: MultipartFile): ErrorMessagesDto {
        val workbook = XSSFWorkbook(file.inputStream)
        val departments: Map<String, Department> = departmentReader.readAll().associateBy { it.name }
        val parts: Map<String, Part> = partReader.readAll().associateBy { it.name }
        val errors: MutableList<String> = mutableListOf()
        for (state: MemberState in MemberState.entries) {
            val processor: MemberExcelProcessor = findProcessor(state)
            println("processor = ${processor.javaClass.simpleName}")
            val sheet: XSSFSheet? = workbook.getSheet(MemberStateConverter.convertToString(state))
            println("sheet.sheetName = ${sheet?.sheetName}")
            println("workbook.numberOfSheets = ${workbook.numberOfSheets}")
            if (sheet == null) {
                errors.add("엑셀 파일에 '${state.name}' 시트가 없습니다.")
                continue
            }


            val errorMessages: ErrorMessages = processor.parse(sheet, departments, parts)
            if (errorMessages.hasErrors()) {
                errors.addAll(errorMessages.errorMessages.map { "${state.name} 시트 오류: $it" })
            }
        }

        workbook.close()

        return ErrorMessagesDto(errors)
    }

    private fun findProcessor(state: MemberState): MemberExcelProcessor {
        println("processors = ${processors}")
        return processors.first { it.supportingState() == state }
    }

    fun createMemberExcelFile(): ExcelFileDto {
        TODO("Not yet implemented")
    }
}
