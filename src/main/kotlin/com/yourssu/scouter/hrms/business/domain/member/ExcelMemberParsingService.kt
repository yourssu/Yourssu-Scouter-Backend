package com.yourssu.scouter.hrms.business.domain.member

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ExcelMemberParsingService(
) {

    fun processExcelFile(file: MultipartFile): ErrorMessagesDto {
        TODO("Not yet implemented")
    }

    fun createMemberExcelFile(): ExcelFileDto {
        TODO("Not yet implemented")
    }
}
