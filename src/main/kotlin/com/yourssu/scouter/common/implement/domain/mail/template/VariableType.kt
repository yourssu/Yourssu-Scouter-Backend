package com.yourssu.scouter.common.implement.domain.mail.template

enum class VariableType {
    // 사용자 입력 변수 타입
    PERSON,
    DATE,
    LINK,
    TEXT,

    // 자동 채움 변수 타입
    APPLICANT,
    PARTNAME,
    // 필요시 여기에 새로운 자동 채움 변수 타입 추가 가능
    ;

    /**
     * 이 타입이 자동 채움 변수 타입인지 확인
     */
    fun isAutoFillType(): Boolean {
        return this == APPLICANT || this == PARTNAME
    }

    /**
     * 이 타입이 사용자 입력 변수 타입인지 확인
     */
    fun isUserInputType(): Boolean {
        return this == PERSON || this == DATE || this == LINK || this == TEXT
    }

    companion object {
        /**
         * 자동 채움 변수 타입 목록
         */
        val AUTO_FILL_TYPES: Set<VariableType> = setOf(APPLICANT, PARTNAME)

        /**
         * 사용자 입력 변수 타입 목록
         */
        val USER_INPUT_TYPES: Set<VariableType> = setOf(PERSON, DATE, LINK, TEXT)
    }
}
