package com.yourssu.scouter.member.excel.business.dto

import org.apache.poi.xssf.usermodel.XSSFWorkbook

data class ExcelFileDto(
    val workbook: XSSFWorkbook,
    val fileName: String,
)
