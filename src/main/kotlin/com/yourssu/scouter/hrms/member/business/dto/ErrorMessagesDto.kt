package com.yourssu.scouter.hrms.member.business.dto

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
