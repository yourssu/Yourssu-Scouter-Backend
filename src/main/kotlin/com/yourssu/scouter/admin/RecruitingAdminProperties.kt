package com.yourssu.scouter.admin

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "scouter.recruiting-admin")
data class RecruitingAdminProperties(
    val password: String = "",
)
