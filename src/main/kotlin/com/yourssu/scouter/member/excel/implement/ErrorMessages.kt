package com.yourssu.scouter.member.excel.implement

data class ErrorMessages(
    val errorMessages: List<String>
) {

    fun hasErrors(): Boolean {
        return errorMessages.isNotEmpty()
    }
}
