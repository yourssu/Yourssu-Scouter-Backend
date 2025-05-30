package com.yourssu.scouter.hrms.business.domain.member

import org.apache.poi.xssf.usermodel.XSSFWorkbook

data class ExcelFileDto(
    val workbook: XSSFWorkbook,
    val fileName: String,
)
