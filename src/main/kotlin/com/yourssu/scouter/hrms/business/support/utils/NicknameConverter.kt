package com.yourssu.scouter.hrms.business.support.utils

import java.util.regex.Pattern

object NicknameConverter {

    private const val COMBINED_NICKNAME_REGEX = "^[a-zA-Z]+\\([가-힣]+\\)\$"

    fun combine(nicknameEnglish: String, nicknameKorean: String) = "${nicknameEnglish}(${nicknameKorean})"

    fun extractNickname(combinedNickname: String): String {
        val validCombinedNickname: String = getValidBlankRemoved(combinedNickname)

        return validCombinedNickname.substringBefore("(")
    }

    fun extractPronunciation(combinedNickname: String): String {
        val validCombinedNickname: String = getValidBlankRemoved(combinedNickname)

        return validCombinedNickname.substringAfter("(").substringBefore(")")
    }

    private fun getValidBlankRemoved(combinedNickname: String): String {
        val blankRemoved = combinedNickname.replace(" ", "")
        require(Pattern.matches(COMBINED_NICKNAME_REGEX, blankRemoved)) { "닉네임은 \\{ 영어(발음) \\} 형식이어야 합니다." }

        return blankRemoved
    }
}
