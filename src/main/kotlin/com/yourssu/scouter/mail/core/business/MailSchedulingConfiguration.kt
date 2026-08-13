package com.yourssu.scouter.mail.core.business

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = ["app.mail.scheduler.enabled"], havingValue = "true")
class MailSchedulingConfiguration
