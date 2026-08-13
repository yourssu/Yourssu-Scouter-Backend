package com.yourssu.scouter.member.excelsheet.business.dto

data class ErrorMessagesDto(
    val errors: List<String>
) {
    fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

    fun combine(abc: String = "\n"): String {
        return errors.joinToString(abc)
    }
}
