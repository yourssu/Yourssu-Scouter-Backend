package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

/**
 * 예약 시간이 지나지 않은 메일에 대한 작업(재전송 등)을 시도할 때 발생.
 * MailReservationAlreadyProcessedException(SENT)과 구분하기 위해 별도 errorCode 사용.
 */
class MailReservationNotYetDueException(
    message: String,
) : CustomException(message, "MailReservation-004", HttpStatus.BAD_REQUEST)
