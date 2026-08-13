package com.yourssu.scouter.member.excelsheet.implement

data class ErrorMessages(
    val errorMessages: List<String>
) {

    fun hasErrors(): Boolean {
        return errorMessages.isNotEmpty()
    }
}
