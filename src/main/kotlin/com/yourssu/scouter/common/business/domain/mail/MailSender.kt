package com.yourssu.scouter.common.business.domain.mail

interface MailSender {

    fun send(
        mailData: MailData,
        accessToken: String,
    )
}
