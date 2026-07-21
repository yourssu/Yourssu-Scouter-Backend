package com.yourssu.scouter.mail.support.implement.mail

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mail.sender")
data class MailSenderProperties(
    val email: String,
    val appPassword: String,
)
