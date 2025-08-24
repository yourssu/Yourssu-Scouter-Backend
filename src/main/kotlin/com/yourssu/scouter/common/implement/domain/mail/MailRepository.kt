package com.yourssu.scouter.common.implement.domain.mail

interface MailRepository {

    fun save(mail: Mail): Mail
    fun findById(mailId: Long): Mail?
}
