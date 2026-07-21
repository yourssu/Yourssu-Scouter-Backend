package com.yourssu.scouter.common.support.implement.initialization

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "applicant-available-time-map")
class ApplicantAvailableTimeMap (
    val time: List<String>,
    val days: List<String>
)