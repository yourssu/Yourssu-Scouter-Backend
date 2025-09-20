package com.yourssu.scouter.common.implement.support.initialization

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "applicant-available-time-map")
class ApplicantAvailableTimeMap (
    val time: Map<String, List<String>>
)