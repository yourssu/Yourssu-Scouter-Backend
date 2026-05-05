package com.yourssu.scouter.common.implement.domain.mail.template

interface RecipientAttributeResolver {

    // "applicant.name", "applicant.email" 등 사용 가능한 속성 키 목록
    // MailTemplateValidator 에서 ApplicantBound 변수의 attributeKey 검증에 사용
    fun availableKeys(): Set<String>

    // email 목록 → 수신자별 속성 맵 (email → (attributeKey → value))
    // email이 resolve 불가한 경우 반환 map에서 누락됨 (호출자가 누락 감지)
    fun resolve(emails: List<String>): Map<String, Map<String, String?>>
}
