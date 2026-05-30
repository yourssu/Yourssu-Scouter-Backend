package com.yourssu.scouter.common.implement.domain.mail.message

interface MailRepository {

    fun save(mail: Mail): Mail
    fun findById(mailId: Long): Mail?
}
