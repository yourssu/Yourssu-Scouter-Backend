package com.yourssu.scouter.common.business.domain.mail

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MailReservationScheduler(
    private val mailService: MailService
) {
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    fun processReservedMails() {
        mailService.sendReservedMails()
    }
} 
