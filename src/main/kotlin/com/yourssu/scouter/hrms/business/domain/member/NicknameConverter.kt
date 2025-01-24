package com.yourssu.scouter.hrms.business.domain.member

object NicknameConverter {

    fun combine(nicknameEnglish: String, nicknameKorean: String) = "${nicknameEnglish}(${nicknameKorean})"
}
