package com.yourssu.scouter.common.business.domain.mail

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MailReservationScheduler(
    private val mailService: MailService,
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailReservationScheduler::class.java)
    }

    // fixedDelay 대신 cron 사용: 매 분 0초에 정확히 실행 (예: 16:00:00, 16:01:00, 16:02:00...)
    // fixedDelay는 이전 작업 완료 후 60초 후 실행이라 누적 지연 발생 가능
    @Scheduled(cron = "0 * * * * ?")
    fun processReservedMails() {
        try {
            log.info("예약 메일 스케줄러 실행")
            mailService.sendReservedMails()
            log.info("예약 메일 스케줄러 완료")
        } catch (e: Exception) {
            log.error("예약 메일 스케줄러 실행 중 예외 발생", e)
            // 예외를 다시 던지지 않아서 다음 스케줄 실행은 계속됨
        }
    }
}
