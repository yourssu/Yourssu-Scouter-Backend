package com.yourssu.scouter.hrms.implement.domain.member.parser

data class ErrorMessages(
    val errorMessages: List<String>
) {

    fun hasErrors(): Boolean {
        return errorMessages.isNotEmpty()
    }
}
