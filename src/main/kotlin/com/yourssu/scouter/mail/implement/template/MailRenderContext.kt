package com.yourssu.scouter.mail.implement.template

data class MailRenderContext(
    val recipientEmail: String,
    val ccEmails: List<String>,
    val bccEmails: List<String>,
    // RecipientAttributeResolver.resolve() 결과에서 이 수신자에 해당하는 속성 맵
    // key: "applicant.name" 등 attributeKey, value: 실제 값 (null이면 미조회)
    val recipientAttributes: Map<String, String?>,
    val sharedBindings: Map<String, String>,
    val recipientBindings: Map<String, String>,
)
