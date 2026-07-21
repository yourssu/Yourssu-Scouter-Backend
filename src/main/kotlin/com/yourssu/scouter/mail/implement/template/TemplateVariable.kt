package com.yourssu.scouter.mail.implement.template

sealed class TemplateVariable {
    abstract val key: String
    abstract val displayName: String

    // 사용자가 예약 시점에 직접 값을 입력하는 변수 (PERSON, DATE, LINK, TEXT)
    data class UserInput(
        override val key: String,
        override val displayName: String,
        val type: VariableType,       // VariableType.isUserInputType() == true 이어야 함
        val perRecipient: Boolean,
    ) : TemplateVariable()

    // Applicant 도메인 속성에서 자동으로 채워지는 변수
    // attributeKey: RecipientAttributeResolver.availableKeys() 에서 제공하는 키
    data class ApplicantBound(
        override val key: String,
        override val displayName: String,
        val attributeKey: String,
    ) : TemplateVariable()

    // 파트명을 자동으로 채워지는 변수
    data class PartName(
        override val key: String,
        override val displayName: String,
    ) : TemplateVariable()
}
