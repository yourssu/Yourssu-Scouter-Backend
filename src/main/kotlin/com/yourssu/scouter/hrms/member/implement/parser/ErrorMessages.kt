package com.yourssu.scouter.hrms.member.implement.parser

data class ErrorMessages(
    val errorMessages: List<String>
) {

    fun hasErrors(): Boolean {
        return errorMessages.isNotEmpty()
    }
}
